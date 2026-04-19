package com.auction.client.views;

import javafx.application.Application;
import javafx.stage.Stage;

public class AuctionApp extends Application {
    @Override
    public void init() throws Exception {
        System.out.println("Before start");
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hello World!");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("After stop");
    }
}
