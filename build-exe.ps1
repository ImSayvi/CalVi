# Przebudowuje CalVi.exe od zera po zmianach w kodzie.
# Uzycie: .\build-exe.ps1

$ErrorActionPreference = "Stop"

Write-Output "Zamykam CalVi.exe jesli dziala..."
Get-Process -Name CalVi -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Output "Usuwam stary target\dist..."
Remove-Item -Path "target\dist" -Recurse -Force -ErrorAction SilentlyContinue

Write-Output "Buduje jar i kopiuje zaleznosci (mvnw clean package)..."
& .\mvnw.cmd -q clean package
if ($LASTEXITCODE -ne 0) { throw "Blad Mavena - sprawdz kod, exe nie zostal zbudowany." }

Write-Output "Buduje CalVi.exe (jpackage)..."
# Bez jawnego --add-modules jlink pomija jdk.localedata (nie jest wykrywany przez jdeps,
# bo jest ladowany dynamicznie przez ServiceLoader) - w spakowanym .exe nazwy dni/miesiecy
# po polsku cicho wracaly do angielskich mimo new Locale("pl") w kodzie. Lista ponizej to
# domyslny zestaw wykrywany przez jdeps + doklejony jdk.localedata (uzyty JDK nie ma
# katalogu jmods, wiec ALL-MODULE-PATH nie zadziala - trzeba wymienic moduly z nazwy).
$modules = "java.base,java.compiler,java.datatransfer,java.xml,java.prefs,java.desktop," +
    "java.instrument,java.logging,java.management,java.security.sasl,java.naming,java.rmi," +
    "java.management.rmi,java.net.http,java.scripting,java.security.jgss,java.smartcardio," +
    "java.transaction.xa,java.sql,java.sql.rowset,java.xml.crypto,jdk.accessibility," +
    "jdk.internal.jvmstat,jdk.attach,jdk.internal.opt,jdk.zipfs,jdk.compiler,jdk.dynalink," +
    "jdk.httpserver,jdk.incubator.vector,jdk.internal.ed,jdk.internal.le,jdk.internal.md," +
    "jdk.jartool,jdk.javadoc,jdk.management,jdk.management.agent,jdk.jconsole,jdk.jdwp.agent," +
    "jdk.jdi,jdk.jfr,jdk.jshell,jdk.jsobject,jdk.management.jfr,jdk.net,jdk.nio.mapmode," +
    "jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.unsupported.desktop," +
    "jdk.xml.dom,jdk.localedata"
jpackage --type app-image --input target --dest target\dist --name CalVi --main-jar calvi-1.0-SNAPSHOT.jar --main-class com.calvi.Launcher --add-modules $modules
if ($LASTEXITCODE -ne 0) { throw "Blad jpackage - exe nie zostal zbudowany." }

# Podpisujemy lokalnym certyfikatem (CN=Calvi Local Dev, w CurrentUser\My + zaufany w
# CurrentUser\Root i CurrentUser\TrustedPublisher) - bez tego "Inteligentna kontrola
# aplikacji" w Windows 11 blokuje uruchomienie niepodpisanego .exe w ogole.
Write-Output "Podpisuje CalVi.exe lokalnym certyfikatem..."
$signCert = Get-ChildItem "Cert:\CurrentUser\My" | Where-Object { $_.Subject -eq "CN=Calvi Local Dev" } | Select-Object -First 1
if (-not $signCert) {
    Write-Output "UWAGA: brak certyfikatu 'CN=Calvi Local Dev' w Cert:\CurrentUser\My - .exe zostanie niepodpisany i Windows moze go zablokowac."
} else {
    Set-ItemProperty -Path "target\dist\CalVi\CalVi.exe" -Name IsReadOnly -Value $false
    $signResult = Set-AuthenticodeSignature -FilePath "target\dist\CalVi\CalVi.exe" -Certificate $signCert -TimestampServer "http://timestamp.digicert.com"
    if ($signResult.Status -ne "Valid") { throw "Podpisywanie nie powiodlo sie: $($signResult.StatusMessage)" }
}

Write-Output ""
Write-Output "Gotowe: target\dist\CalVi\CalVi.exe"
Write-Output "Skroty na pulpicie i w Startup wskazuja na ten sam plik - nie trzeba ich odtwarzac."
