package com.example.spendwise;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class LoginStage extends Application {
    private final String[] messages = {"Welcome back!", "Login to continue.", "Secure your account."};
    private int currentMessageIndex = 0;
    private Text messageText;
    private Stage loginStage;
    TextField emailField = new TextField();
    PasswordField passwordField = new PasswordField();
    Label messageLabel = new Label();
    // JDBC URL, username, and password for MySQL
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loginStage = primaryStage;
        primaryStage.setTitle("Login to SpendWise");

        // Create a VBox to hold UI elements
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(50));
        vbox.setPrefWidth(300);

        Image logo = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png");
        ImageView imageView = new ImageView();
        imageView.setImage(logo);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        // Create and style the text with a login message
        messageText = new Text(messages[currentMessageIndex]);
        messageText.setFont(Font.font("Arial", 18));
        messageText.setFill(Color.WHITE);

        emailField.setPromptText("Email");
        emailField.setMaxWidth(200);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);

        // Create and style the login button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        loginButton.setPrefWidth(150);
        Button closeLoginButton = new Button("back");
        closeLoginButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        closeLoginButton.setPrefWidth(100);
        closeLoginButton.setOnAction(e ->closeLogin());

        // Add event
        loginButton.setOnAction(e -> performLogin());

        //Message for validation
        messageLabel.setStyle("-fx-font-size: 14px; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: transparent; " +
                "-fx-padding: 8px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-alignment: center;");



        // Add elements to the VBox
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(imageView,messageText,emailField,passwordField,loginButton,closeLoginButton,messageLabel);

        // Set modern purple color as the background for the scene
        BackgroundFill backgroundFillLight = new BackgroundFill(Color.rgb(144, 101, 211), CornerRadii.EMPTY, Insets.EMPTY);
        BackgroundFill backgroundFillDark = new BackgroundFill(Color.rgb(44, 44, 44), CornerRadii.EMPTY, Insets.EMPTY);
        StackPane stackPane = new StackPane();
        if(checkForDarkMode()){
        stackPane.setBackground(new Background(backgroundFillDark));
        }
        else {
            stackPane.setBackground(new Background(backgroundFillLight));
        }
        stackPane.getChildren().add(vbox);

        Scene scene = new Scene(stackPane, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.show();


        startMessageTransition();
    }

    private void startMessageTransition() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), messageText);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(e -> {
                currentMessageIndex = (currentMessageIndex + 1) % messages.length;
                messageText.setText(messages[currentMessageIndex]);
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), messageText);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeTransition.play();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void closeLogin(){
        WelcomeStage welcomeStage = new WelcomeStage();
        welcomeStage.start(new Stage());
        loginStage.close();
    }
    private void performLogin() {
            try {
                if (passwordField.getText().isEmpty() || emailField.getText().isEmpty()) {
                    throw new CustomException("Fill out the fields - Email or Password is empty");
                }else {
                    // Attempt to establish a connection to the MySQL database
                    try {
                        Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                        if (connection != null) {
                            System.out.println("Connected to MySQL database!");
                            try {
                                String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
                                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                                // Set parameters for the PreparedStatement
                                preparedStatement.setString(1, emailField.getText());
                                preparedStatement.setString(2, passwordField.getText());

                                ResultSet resultSet = preparedStatement.executeQuery();
                                if (resultSet.next()) {
                                    // Successful login
                                    User user = new User(resultSet.getInt(1),resultSet.getNString(2), resultSet.getNString(3));
                                    UserSession.setLoggedInUser(user);
                                    int role = resultSet.getInt("role");
                                    switch (role){
                                        case 0:
                                            DashBoardStage dashBoardStage = new DashBoardStage();
                                            dashBoardStage.start(new Stage());
                                            loginStage.close();
                                            break;
                                        case 1:
                                            Admin admin = new Admin();
                                            admin.start(new Stage());
                                            loginStage.close();
                                            break;
                                    }
                                } else {
                                    // Invalid credentials
                                    CustomException customException = new CustomException("Invalid Credentials");
                                    customException.logException();
                                    messageLabel.setStyle("-fx-font-size: 14px; " +
                                            "-fx-text-fill: white; " +
                                            "-fx-background-color: #ff3333;" +
                                            "-fx-padding: 8px; " +
                                            "-fx-border-radius: 5px; " +
                                            "-fx-background-radius: 5px; " +
                                            "-fx-alignment: center;");
                                    messageLabel.setText("Invalid email or password");

                                }


                            } catch (SQLException e) {
                                CustomException customException = new CustomException("SQL Exception occurred");
                                customException.logException();
                                e.printStackTrace();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            System.out.println("Failed to connect to MySQL database!");
                        }
                    } catch (SQLException e) {
                        System.out.println("SQL Exception: " + e.getMessage());
                        CustomException customException = new CustomException("SQL Exception occurred");
                        customException.logException();
                        e.printStackTrace();
                    }
                }
            } catch (CustomException e) {
                // Log the CustomException using logException() method
                e.logException();
                // Set the message label style and text
                messageLabel.setStyle("-fx-font-size: 14px; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: #ff3333;" +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-alignment: center;");
                messageLabel.setText("Fill out the fields");
            }
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
}
