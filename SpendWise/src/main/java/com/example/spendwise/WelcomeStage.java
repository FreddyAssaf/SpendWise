package com.example.spendwise;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class WelcomeStage extends Application {
    private final String[] quotes = {"Save money, live better!", "Spend wisely, live happily!", "Financial freedom, here we come!"};
    private int currentQuoteIndex = 0;
    private Text quoteText;
    private Stage welcomeStage;
    @Override
    public void start(Stage primaryStage) {
        // Load the image for the loading screen
        Image loadingImage = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"); // Replace with your image path

        // Create an ImageView to display the loading image
        ImageView loadingImageView = new ImageView(loadingImage);
        loadingImageView.setFitWidth(350); // Set the width of the image
        loadingImageView.setPreserveRatio(true); // Maintain the image's aspect ratio


        Task<Void> initializationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate loading process
                Thread.sleep(800); // Simulate a 0.8-second loading time

                return null;
            }
        };

        initializationTask.setOnSucceeded(event -> {
            // When the loading is completed, transition to the main application
            transitionToMainApp(primaryStage);
        });

        // Start the loading task
        new Thread(initializationTask).start();

        // Show the loading screen with the image while the initialization task is running
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);

        Text loadingText = new Text("Loading...");
        loadingText.setFont(Font.font("Arial", 18));
        loadingText.setFill(Color.WHITE);

        vBox.getChildren().addAll(loadingImageView,loadingText);

        StackPane loadingPane = new StackPane(vBox);
        BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(144, 101, 211), CornerRadii.EMPTY, Insets.EMPTY);
        loadingPane.setBackground(new Background(backgroundFill));
        Scene loadingScene = new Scene(loadingPane, 400,400);

        primaryStage.setScene(loadingScene);
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.setTitle("SpendWise");
        primaryStage.show();

    }
    private void transitionToMainApp(Stage primaryStage) {
        welcomeStage = primaryStage;
        primaryStage.setTitle("Welcome to SpendWise");

        // Create a VBox to hold UI elements
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(50));
        vbox.setPrefWidth(300);

        // Load an image
        Image logo = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png");
        ImageView imageView = new ImageView();
        imageView.setImage(logo);
        imageView.setFitWidth(200); // Set the width of the image
        imageView.setPreserveRatio(true); // Maintain the image's aspect ratio

        // Create and style the text with a cute quote
        quoteText = new Text(quotes[currentQuoteIndex]);
        quoteText.setFont(Font.font("Arial", 18));
        quoteText.setFill(Color.WHITE);

        // Create and style the login button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        loginButton.setPrefWidth(150);

        // Create and style the signup button
        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        signUpButton.setPrefWidth(150);

        // Add events
        loginButton.setOnAction(e -> openLoginStage());
        signUpButton.setOnAction(e -> openSignUpStage());

        // Add elements to the VBox
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(imageView,quoteText, loginButton, signUpButton);

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

        Scene scene = new Scene(stackPane,400,400);
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        startQuoteTransition();

        // Fade out the loading screen
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Set the main application scene after fade-out
            primaryStage.setScene(scene);

            // Fade in the main application screen
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), primaryStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
    private void startQuoteTransition() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), quoteText);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(e -> {
                currentQuoteIndex = (currentQuoteIndex + 1) % quotes.length;
                quoteText.setText(quotes[currentQuoteIndex]);
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), quoteText);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeTransition.play();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    // Method to open the login stage
    private void openLoginStage() {
        LoginStage loginStage = new LoginStage();
        loginStage.start(new Stage());
        welcomeStage.close(); // Hide the WelcomeStage
    }
    // Method to open the sign up stage
    private void openSignUpStage() {
        SignupStage signupStage = new SignupStage();
        signupStage.start(new Stage());
        welcomeStage.close();
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

    public void launchWelcomeStage() {
        launch();
    }
}
