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
jpackage --type app-image --input target --dest target\dist --name CalVi --main-jar calvi-1.0-SNAPSHOT.jar --main-class com.calvi.Launcher
if ($LASTEXITCODE -ne 0) { throw "Blad jpackage - exe nie zostal zbudowany." }

Write-Output ""
Write-Output "Gotowe: target\dist\CalVi\CalVi.exe"
Write-Output "Skroty na pulpicie i w Startup wskazuja na ten sam plik - nie trzeba ich odtwarzac."
