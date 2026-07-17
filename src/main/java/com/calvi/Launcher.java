package com.calvi;

public class Launcher {
    // klasa main, która dziedziczy po javafx.application.Application (czyli Main),
    // nie może być bezpośrednio uruchomiona jako .jar/.exe - Launcher to obchodzi,
    // po prostu wywołując jej main() z zewnątrz
    public static void main(String[] args) {
        Main.main(args);
    }
}
