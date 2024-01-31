package com.example.spendwise;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.Objects;

public class SignupStage extends Application {
    private final String[] messages = {"Welcome to SpendWise!", "Sign up to continue.", "Secure your account."};
    private int currentMessageIndex = 0;
    private Text messageText;
    private Stage signupStage;
    Label messageLabel = new Label();
    TextField usernameField = new TextField();
    TextField emailField = new TextField();
    PasswordField passwordField = new PasswordField();
    PasswordField passwordConfirmField = new PasswordField();
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        signupStage = primaryStage;
        primaryStage.setTitle("Sign up for SpendWise");

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


        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);

        emailField.setPromptText("Email");
        emailField.setMaxWidth(200);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);

        passwordConfirmField.setPromptText("Confirm password");
        passwordConfirmField.setMaxWidth(200);

        // Create and style the login button
        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        signupButton.setPrefWidth(150);
        Button closeSignUpButton = new Button("back");
        closeSignUpButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        closeSignUpButton.setPrefWidth(100);
        closeSignUpButton.setOnAction(e -> closeSignUp());

        // Add event
        signupButton.setOnAction(e -> {
            try {
                performSignUp();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        messageLabel.setStyle("-fx-font-size: 14px; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: transparent; " +
                "-fx-padding: 8px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-alignment: center;");

        // Add elements to the VBox
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(imageView, messageText, usernameField, emailField, passwordField, passwordConfirmField, signupButton, closeSignUpButton, messageLabel);

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

    private void closeSignUp() {
        WelcomeStage welcomeStage = new WelcomeStage();
        welcomeStage.start(new Stage());
        signupStage.close();
    }

    private void performSignUp() throws SQLException {
        try {
            if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty() || passwordConfirmField.getText().isEmpty()) {
                throw new CustomException("Fill out the fields - Email or Password is empty");
            } else if (!Objects.equals(passwordConfirmField.getText(), passwordField.getText())) {
                messageLabel.setStyle("-fx-font-size: 14px; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: #ff3333;" +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-alignment: center;");
                messageLabel.setText("Passwords mismatch");
                throw new CustomException("Passwords mismatch");
            }else if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
                messageLabel.setStyle("-fx-font-size: 14px; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-color: #ff3333;" +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-alignment: center;");
                messageLabel.setText("Invalid email");
                throw new CustomException("Wrong Email Format");
            } else {
                try {
                    Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                    if (connection != null) {
                        System.out.println("Connected to MySQL database!");


                        try {
                            String sql = "SELECT * FROM user WHERE email = ? AND username = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            // Set parameters for the PreparedStatement
                            preparedStatement.setString(1, emailField.getText());
                            preparedStatement.setString(2, usernameField.getText());

                            ResultSet resultSet = preparedStatement.executeQuery();
                            if (resultSet.next()) {
                                // Email and username already exist
                                messageLabel.setStyle("-fx-font-size: 14px; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-background-color: #ff3333;" +
                                        "-fx-padding: 8px; " +
                                        "-fx-border-radius: 5px; " +
                                        "-fx-background-radius: 5px; " +
                                        "-fx-alignment: center;");
                                messageLabel.setText("Username and email already exist");
                                CustomException c = new CustomException("Username And Email Already Exist");
                                c.logException();
                            } else {
                                // Check if email exists
                                sql = "SELECT * FROM user WHERE email = ?";
                                preparedStatement = connection.prepareStatement(sql);
                                preparedStatement.setString(1, emailField.getText());

                                resultSet = preparedStatement.executeQuery();
                                if (resultSet.next()) {
                                    // Email exists
                                    messageLabel.setStyle("-fx-font-size: 14px; " +
                                            "-fx-text-fill: white; " +
                                            "-fx-background-color: #ff3333;" +
                                            "-fx-padding: 8px; " +
                                            "-fx-border-radius: 5px; " +
                                            "-fx-background-radius: 5px; " +
                                            "-fx-alignment: center;");
                                    messageLabel.setText("Email already exists");
                                    CustomException c = new CustomException("Email Already Exist");
                                    c.logException();
                                } else {
                                    // Check if username exists
                                    sql = "SELECT * FROM user WHERE username = ?";
                                    preparedStatement = connection.prepareStatement(sql);
                                    preparedStatement.setString(1, usernameField.getText());

                                    resultSet = preparedStatement.executeQuery();
                                    if (resultSet.next()) {
                                        // Username exists
                                        messageLabel.setStyle("-fx-font-size: 14px; " +
                                                "-fx-text-fill: white; " +
                                                "-fx-background-color: #ff3333;" +
                                                "-fx-padding: 8px; " +
                                                "-fx-border-radius: 5px; " +
                                                "-fx-background-radius: 5px; " +
                                                "-fx-alignment: center;");
                                        messageLabel.setText("Username already exists");
                                        CustomException c = new CustomException("Username Already Exist");
                                        c.logException();
                                    } else {
                                        // Both are unique
                                        try {
                                            sql = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
                                            preparedStatement = connection.prepareStatement(sql);

                                            // Set parameters for the PreparedStatement
                                            preparedStatement.setString(1, usernameField.getText()); // Set username as "JohnDoe"
                                            preparedStatement.setString(2, emailField.getText()); // Set email as "johndoe@example.com"
                                            preparedStatement.setString(3, passwordField.getText()); // Set password as "password123"

                                            // Execute the insert query
                                            int rowsInserted = preparedStatement.executeUpdate();
                                            int userId=0;
                                            if (rowsInserted > 0) {
                                                System.out.println("A new user was inserted successfully!");
                                                WelcomeStage welcomeStage = new WelcomeStage();
                                                welcomeStage.start(new Stage());
                                                signupStage.close();
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            CustomException c = new CustomException("SQL ERROR");
                                            c.logException();
                                        }
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            CustomException c = new CustomException("SQL ERROR");
                            c.logException();
                            // Handle SQL exception
                        }
                    } else {
                        System.out.println("Failed to connect to MySQL database!");
                    }
                } catch (SQLException e) {
                    System.out.println("SQL Exception: " + e.getMessage());
                    e.printStackTrace();
                    CustomException c = new CustomException("SQL ERROR");
                    c.logException();
                    // Handle SQL exception
                }
            }
        } catch (CustomException e) {
            e.logException();
            // Set the message label style and text
            messageLabel.setStyle("-fx-font-size: 14px; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-color: #ff3333;" +
                    "-fx-padding: 8px; " +
                    "-fx-border-radius: 5px; " +
                    "-fx-background-radius: 5px; " +
                    "-fx-alignment: center;");
            messageLabel.setText(e.getMessage()); // Display the CustomException message
        }


    }
    private void openSignUpStage() {
        LoginStage loginStage = new LoginStage();
        loginStage.start(new Stage());
        signupStage.close();
    }
    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Alert");
        alert.setHeaderText(null);
        alert.setContentText("New Account has been registered");
        alert.show();
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