package com.example.video_game_analytics;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {
    DatabaseConnector databaseConnector = new DatabaseConnector();
    boolean isTableView = true;
    FlowPane flowPane = new FlowPane();
    VBox vBox = new VBox();
    String selectedOption = "Genre";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton radioButton1 = new RadioButton("Genre");
        RadioButton radioButton2 = new RadioButton("Year");
        RadioButton radioButton3 = new RadioButton("Platform");

        radioButton1.setToggleGroup(toggleGroup);
        radioButton2.setToggleGroup(toggleGroup);
        radioButton3.setToggleGroup(toggleGroup);

        radioButton1.setSelected(true);

        flowPane.setPadding(new Insets(10));
        flowPane.setHgap(10);
        flowPane.getChildren().addAll(radioButton1, radioButton2, radioButton3);


        Button viewTableButton = new Button("View Table");
        Button loadAllSales = new Button("Load All Sales");

        loadAllSales.setVisible(false);

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(loadAllSales, viewTableButton);

        loadAllSales.setOnAction(e -> {
            loadAllSales.setVisible(false);
            handleViewToggle(true, -1);
        });



        viewTableButton.setOnAction(e -> {
            // Toggle between "View Table" and "Graph View"
            if (isTableView) {
                loadAllSales.setVisible(true);
                viewTableButton.setText("Graph View");
            } else {
                loadAllSales.setVisible(false);
                viewTableButton.setText("View Table");
            }

            handleViewToggle(isTableView, 20);

            isTableView = !isTableView;

        });

        BorderPane borderPane = new BorderPane();

        borderPane.setCenter(flowPane);
        BorderPane.setAlignment(flowPane, Pos.CENTER);

        borderPane.setRight(hbox);
        BorderPane.setAlignment(hbox, Pos.CENTER_RIGHT);


        // Create chart with initial data
        BarChart<String, Number> barChart = createChart("Genre");

        // Creating a VBox to hold the chart
        vBox.getChildren().addAll(barChart, borderPane);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selectedRadioButton = (RadioButton) newToggle;
                selectedOption = selectedRadioButton.getText();
                handleToggleGroupChange(selectedOption);
            }
        });

        // Setting up the scene
        Scene scene = new Scene(vBox, 800, 530);
        scene.getRoot().getStyleClass().add("container");

        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setHeight(530);
        primaryStage.setWidth(800);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("console-controller-icon.png")));
        primaryStage.setTitle("Video Game Sales");
        primaryStage.show();
    }

    private BarChart<String, Number> createChart(String category) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFill(Color.WHITE);

        // Creating the bar chart
        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Video Game Sales By " + category);

        // Defining the series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(category);

        try (Connection connection = databaseConnector.connect()) {
            Statement statement = connection.createStatement();
            String orderBy = "";
            if(category.equals("Year")){
                orderBy = " ORDER BY Year";
            }
            ResultSet resultSet = statement.executeQuery("SELECT " + category + ", SUM(Global_Sales) AS Global_Sales from gamesales GROUP BY " + category + orderBy);
            while (resultSet.next()) {
                String cat = resultSet.getString(category);
                double sales = resultSet.getDouble("Global_Sales");
                series.getData().add(new XYChart.Data<>(cat, sales));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Adding series to chart
        barChart.getData().addAll(series);

        return barChart;
    }

    private void handleToggleGroupChange(String selectedOption) {
        vBox.getChildren().remove(0); // Remove the existing chart (at index 1) from the VBox
        if (selectedOption.equals("Platform")) {
            // Generate pie chart
            PieChart pieChart = new PieChart();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            try (Connection connection = databaseConnector.connect()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT Platform, SUM(Global_Sales) AS Global_Sales FROM gamesales GROUP BY Platform");
                while (resultSet.next()) {
                    String platform = resultSet.getString("Platform");
                    double sales = resultSet.getDouble("Global_Sales");
                    pieChartData.add(new PieChart.Data(platform, sales));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            pieChart.setTitle("Video Game Sales By Platform");
            pieChart.setData(pieChartData);
            vBox.getChildren().add(0, pieChart);
        } else {
            // Generate bar chart
            BarChart<String, Number> barChart = createChart(selectedOption);
            vBox.getChildren().add(0, barChart);
        }
    }

    private void handleViewToggle(boolean isTableView, int limit) {
        if (isTableView) {
            vBox.getChildren().remove(0);
            // Hide the toggle group
            flowPane.setVisible(false);
            TableView<GameSales> tableView = new TableView<>();
            TableColumn<GameSales, Integer> rankColumn = new TableColumn<>("Rank");
            TableColumn<GameSales, String> nameColumn = new TableColumn<>("Name");
            TableColumn<GameSales, String> platformColumn = new TableColumn<>("Platform");
            TableColumn<GameSales, Integer> yearColumn = new TableColumn<>("Year");
            TableColumn<GameSales, String> genreColumn = new TableColumn<>("Genre");
            TableColumn<GameSales, String> publisherColumn = new TableColumn<>("Publisher");
            TableColumn<GameSales, Double> naSalesColumn = new TableColumn<>("NA Sales");
            TableColumn<GameSales, Double> euSalesColumn = new TableColumn<>("EU Sales");
            TableColumn<GameSales, Double> jpSalesColumn = new TableColumn<>("JP Sales");
            TableColumn<GameSales, Double> otherSalesColumn = new TableColumn<>("Other Sales");
            TableColumn<GameSales, Double> globalSalesColumn = new TableColumn<>("Global Sales");

            rankColumn.setCellValueFactory(new PropertyValueFactory<>("gameRank"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
            platformColumn.setCellValueFactory(new PropertyValueFactory<>("gamePlatform"));
            yearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
            genreColumn.setCellValueFactory(new PropertyValueFactory<>("gameGenre"));
            publisherColumn.setCellValueFactory(new PropertyValueFactory<>("gamePublisher"));
            naSalesColumn.setCellValueFactory(new PropertyValueFactory<>("naSales"));
            euSalesColumn.setCellValueFactory(new PropertyValueFactory<>("euSales"));
            jpSalesColumn.setCellValueFactory(new PropertyValueFactory<>("jpSales"));
            otherSalesColumn.setCellValueFactory(new PropertyValueFactory<>("otherSales"));
            globalSalesColumn.setCellValueFactory(new PropertyValueFactory<>("globalSales"));

            tableView.getColumns().addAll(rankColumn, nameColumn, platformColumn, yearColumn, genreColumn, publisherColumn, naSalesColumn, euSalesColumn, jpSalesColumn, otherSalesColumn, globalSalesColumn);

            try (Connection connection = databaseConnector.connect()) {
                Statement statement = connection.createStatement();
                String limitClause = "";
                if(limit != -1){
                    limitClause = " Limit " + limit;
                }
                ResultSet resultSet = statement.executeQuery("SELECT * FROM gamesales" + limitClause);
                while (resultSet.next()) {
                    GameSales gameSales = new GameSales(
                            resultSet.getInt("Rank"),
                            resultSet.getString("Name"),
                            resultSet.getString("Platform"),
                            resultSet.getInt("Year"),
                            resultSet.getString("Genre"),
                            resultSet.getString("Publisher"),
                            resultSet.getDouble("NA_Sales"),
                            resultSet.getDouble("EU_Sales"),
                            resultSet.getDouble("JP_Sales"),
                            resultSet.getDouble("Other_Sales"),
                            resultSet.getDouble("Global_Sales")
                    );
                    tableView.getItems().add(gameSales);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            // Create VBox to hold title label and table
            VBox tableContainer = new VBox();
            tableContainer.setAlignment(Pos.CENTER);
            tableContainer.setSpacing(10);

            // Add title label above the table
            Label titleLabel = new Label("Video Game Sales");
            titleLabel.getStyleClass().add("label-style");

            tableContainer.getChildren().addAll(titleLabel, tableView);

            vBox.getChildren().add(0, tableContainer);
        } else {
            // Show the toggle group
            flowPane.setVisible(true);
            handleToggleGroupChange(selectedOption);
        }
    }
}
