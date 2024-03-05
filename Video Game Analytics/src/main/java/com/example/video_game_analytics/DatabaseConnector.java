package com.example.video_game_analytics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DATABASE_URL = "jdbc:mysql://sql.freedb.tech:3306/freedb_testDB";
    private static final String DATABASE_USER = "freedb_jfxroot";
    private static final String DATABASE_PASSWORD = "9uwd!*r%mFN*bQ8";
    public Connection connect() {
        try {
            return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }
}
