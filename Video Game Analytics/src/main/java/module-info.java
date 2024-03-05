module com.example.video_game_analytics {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.video_game_analytics to javafx.fxml;
    exports com.example.video_game_analytics;
}