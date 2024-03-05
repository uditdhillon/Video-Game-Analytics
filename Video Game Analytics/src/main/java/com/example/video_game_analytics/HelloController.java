package com.example.video_game_analytics;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeLabel;

    @FXML
    protected void onHelloButtonClick() {
        welcomeLabel.setText("Welcome to JavaFX Application!");
    }
}
