package com.auction.server.db;

import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseManager {

    public static void initializeTables() throws SQLException {
        try {
            Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();


            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        username    TEXT NOT NULL UNIQUE,
                        password    TEXT NOT NULL,
                        email       TEXT NOT NULL UNIQUE,
                        role        TEXT NOT NULL CHECK(role IN('BIDDER', 'SELLER', 'AADMIN')),
                        active      INTEGER NOT NULL DEFAULT 1,
                        created_at  TEXT NOT NULL
                    )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS items (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        name            TEXT NOT NULL,
                        description     TEXT,
                        category        TEXT NOT NULL CHECK(category IN ('ELECTRONICS','ART','VEHICLE'),
                        seller_id       INTEGER NOT NULL REFERENCES users(id),
                        created_at      TEXT NOT NULL
                        )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS auctions (
                        id               INTEGER PRIMARY KEY AUTOINCREMENT,
                        item_id          INTEGER NOT NULL REFERENCES items(id),
                        starting_price   REAL    NOT NULL,
                        current_price    REAL    NOT NULL,
                        start_time       TEXT    NOT NULL,
                        end_time         TEXT    NOT NULL,
                        status           TEXT    NOT NULL DEFAULT 'OPEN',
                        seller_id        INTEGER NOT NULL REFERENCES users(id),
                        winner_id        INTEGER REFERENCES users(id),
                        created_at       TEXT    NOT NULL
                    )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bid_transactions (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        auction_id  INTEGER NOT NULL REFERENCES auctions(id),
                        bidder_id   INTEGER NOT NULL REFERENCES users(id),
                        amount      REAL    NOT NULL,
                        is_auto_bid INTEGER NOT NULL DEFAULT 0,
                        created_at  TEXT    NOT NULL
                    )
            """);    
            
    
            stmt.close();
            System.out.println("Database tables initialized successfully! Yeah!!!");
        } catch (SQLException e) {
            throw new RuntimeException("Creating tables failed!");
        }
    }
}