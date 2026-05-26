package com.auction.client;

import javafx.application.Application;

/**
 * Plain JVM entry point for the executable client jar.
 *
 * Keeping the manifest main class separate from the JavaFX Application subclass
 * avoids the Java launcher treating the jar as a modular JavaFX application.
 */
public final class ClientLauncher {

    private ClientLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(ClientMain.class, args);
    }
}
