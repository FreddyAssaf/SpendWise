package com.example.spendwise;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class AddCash extends Application {
    private Stage addCash;
    Button back = new Button();
    Text amountBalance = new Text("0");
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";
    public void start(Stage primaryStage) throws Exception {
        addCash= primaryStage;

        StackPane balance = createStackPane("Balance", amountBalance, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\cash.png", Color.rgb(51, 153, 102));
        VBox addCashVBox = new VBox();
        addCashVBox.setAlignment(Pos.CENTER);
        addCashVBox.setSpacing(10);
        addCashVBox.setPadding(new Insets(20));


        Label addCashLabel = new Label("Add to Cash:");
        addCashLabel.setTextFill(Color.WHITE);

        TextField addCashTextField = new TextField();
        addCashTextField.setPromptText("Enter amount");

        Button addCashButton = new Button("Add");
        addCashButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Image icon = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\back.png"); // Replace with your icon path
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(34); // Set the width of the icon
        imageView.setFitHeight(34); // Set the height of the icon

        // Create a button and set the icon on it
        back.setGraphic(imageView); // Set the icon as the button's graphic
        back.setStyle("-fx-background-color: white;"); // Set the button background color
        back.setOnAction(e->{
            try {
                goBack();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        addCashVBox.getChildren().addAll(balance,addCashLabel, addCashTextField, addCashButton,back);
        if (checkForDarkMode()) {
            // Set dark mode background color for the BorderPane
            addCashVBox.setStyle("-fx-background-color: rgb(44, 44, 44);"); // Dark mode color
            back.setStyle("-fx-background-color: rgb(44, 44, 44);");
        } else {
            // Set light mode background color for the BorderPane
            addCashVBox.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-border-width: 2px;");
        }

        updateTextFieldValue(UserSession.getLoggedInUser().getId(), amountBalance);

        addCashButton.setOnAction(event -> {
            String amountText = addCashTextField.getText().trim();
            try {
                if (amountText.isEmpty() || Double.parseDouble(amountText) < 0) {
                    showAlert("Please enter a valid positive amount.");
                    throw new CustomException("Entered amount Not valid");
                }
                else{
                    try {
                        double amountToAdd = Double.parseDouble(amountText);

                        Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                        if (connection != null) {
                            // Update balance in the user table
                            String updateQuery = "UPDATE user SET balance = balance + ? WHERE user_id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                            preparedStatement.setDouble(1, amountToAdd);
                            preparedStatement.setInt(2, UserSession.getLoggedInUser().getId());

                            int rowsUpdated = preparedStatement.executeUpdate();
                            if (rowsUpdated > 0) {
                                showSuccess("Cash added successfully.");
                                // Update the displayed balance
                                amountBalance.setText(String.valueOf(Double.parseDouble(amountBalance.getText()) + amountToAdd));
                                addCashTextField.clear();
                            }
                        } else {
                            throw new CustomException("Failed to connect to the database!");
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Please enter a valid numeric amount.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new CustomException("Failed to update cash. Please try again.");
                    } catch (CustomException e) {
                        e.logException();
                        showAlert(e.getMessage());
                    }
                }
            }catch (CustomException c){
                c.logException();
            }



        });

        Scene scene = new Scene(addCashVBox,400,400);
        primaryStage.setScene(scene);
        primaryStage.setMaxWidth(400);
        primaryStage.setMaxHeight(400);
        primaryStage.setTitle("Add Cash");
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }
    private StackPane createStackPane(String categoryName, Text amountText, String iconPath, Color squareColor) {
        // Creating a StackPane for the category
        StackPane stackPane = new StackPane();

        // Creating a square (Rectangle) for the category
        Rectangle square = new Rectangle(150, 150); // Adjust size as needed
        square.setFill(squareColor); // Set square color
        square.setArcWidth(20); // Rounded corners
        square.setArcHeight(20);

        // Creating text for the category name
        Text categoryText = new Text(categoryName);
        categoryText.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Set font and size
        categoryText.setFill(Color.WHITE); // Set text color

        amountText.setFont(Font.font("Arial", 20)); // Set font and size
        amountText.setFill(Color.WHITE); // Set text color

        // Creating an icon for the category
        Image iconImage = new Image(iconPath); // Replace with your icon path
        ImageView iconView = new ImageView(iconImage);
        iconView.setFitWidth(40); // Set icon width
        iconView.setFitHeight(40); // Set icon height

        // Creating a VBox to arrange the elements vertically for the category
        VBox vBox = new VBox(categoryText, amountText, iconView);
        vBox.setAlignment(Pos.CENTER);

        // Adding the square and VBox to the StackPane
        stackPane.getChildren().addAll(square, vBox);

        return stackPane;
    }
    private void goBack() throws Exception {
        DashBoardStage dashBoardStage = new DashBoardStage();
        dashBoardStage.start(new Stage());
        addCash.close();
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean checkForDarkMode() {
        String darkModeFilePath = "darkmode.txt";
        File file = new File(darkModeFilePath);

        if (file.exists()) {
            try {
                String fileContent = new String(Files.readAllBytes(Paths.get(darkModeFilePath))).trim();
                return Boolean.parseBoolean(fileContent);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            // Create the file if it doesn't exist
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return false; // Return false by default if file reading fails or file doesn't exist
    }
    private void updateTextFieldValue(int userId, Text amountBalanceField) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            if (connection != null) {
                String amountBalanceQuery = "SELECT balance AS totalBalance FROM user WHERE user_id = ?";
                PreparedStatement balanceStatement = connection.prepareStatement(amountBalanceQuery);
                balanceStatement.setInt(1, userId);
                ResultSet balanceResult = balanceStatement.executeQuery();

                if (balanceResult.next()) {
                    double amountBalance = balanceResult.getDouble("totalBalance");
                    amountBalanceField.setText(String.valueOf(amountBalance));
                }

                System.out.println("Amount balance field updated successfully!");
            } else {
                System.out.println("Failed to connect to the database!");
                CustomException c = new CustomException("Error connecting to database");
                c.logException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exception
        }
    }

}
