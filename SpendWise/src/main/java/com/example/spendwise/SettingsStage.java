package com.example.spendwise;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DecimalFormat;

public class SettingsStage extends Application {
    private static final String SELECTED_CURRENCY_FILE = "selected_currency.txt";
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";
    CheckBox darkModeCheckBox = new CheckBox("Enable");

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Settings");

        checkForDarkMode();

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(10);

        Label showPasswordLabel = new Label("Show password:");
        showPasswordLabel.setTextFill(Color.WHITE);
        TextField passwordTextField = new TextField();
        passwordTextField.setEditable(false);
        Button showPasswordButton = new Button("Show");
        showPasswordButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        showPasswordButton.setOnAction(e -> {
            int userID = UserSession.getLoggedInUser().getId();
            String retrievedPassword = retrievePasswordFromDatabase(userID);
            passwordTextField.setText(retrievedPassword);
        });

        vbox.getChildren().addAll(showPasswordLabel, passwordTextField, showPasswordButton);

        Label changePasswordLabel = new Label("Change password:");
        changePasswordLabel.setTextFill(Color.WHITE);
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        Button applyPasswordButton = new Button("Apply");
        applyPasswordButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        applyPasswordButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            int userID = UserSession.getLoggedInUser().getId();
            changePasswordInDatabase(userID, newPassword);
            newPasswordField.clear();
        });

        vbox.getChildren().addAll(changePasswordLabel, newPasswordField, applyPasswordButton);

        Label darkModeLabel = new Label("Dark mode:");
        darkModeLabel.setTextFill(Color.WHITE);

        darkModeCheckBox.setOnAction(e -> {
            boolean isDarkModeEnabled = darkModeCheckBox.isSelected();
            String darkModeFilePath = "darkmode.txt";

            // Write the updated dark mode status to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(darkModeFilePath))) {
                writer.write(String.valueOf(isDarkModeEnabled));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            showSuccess("Dark mode settings applied");
            SettingsStage settingsStage = new SettingsStage();
            try {
                settingsStage.start(new Stage());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            primaryStage.close();
        });

        vbox.getChildren().addAll(darkModeLabel, darkModeCheckBox);

        Label changeCurrencyLabel = new Label("Change currency:");
        ComboBox<String> currencyComboBox = new ComboBox<>();
        currencyComboBox.getItems().addAll("USD", "EUR"); // Add more currencies as needed
        String savedCurrency = readSelectedCurrencyFromFile();
        if (!savedCurrency.isEmpty()) {
            currencyComboBox.setValue(savedCurrency);
        } else {
            currencyComboBox.setValue("USD"); // Default value if file doesn't exist or is empty
        }

        currencyComboBox.setOnAction(e -> {
            String selectedCurrency = currencyComboBox.getValue();
            System.out.println("Currency changed to: " + selectedCurrency);

            // Calculate new balance based on conversion rate
            double conversionRate = 0.92;
            double newBalance = calculateNewBalance(selectedCurrency, conversionRate);

            // Update balance in the user table
            int userID = UserSession.getLoggedInUser().getId();
            updateUserBalance(userID, newBalance);

            // Update amount in the expense table for the specific user
            updateExpenseAmounts(userID, selectedCurrency);

            // Save the selected currency to the file
            saveSelectedCurrencyToFile(selectedCurrency);
        });

        vbox.getChildren().addAll(changeCurrencyLabel, currencyComboBox);

        Image icon = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\back.png");
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(34); // Set the width of the icon
        imageView.setFitHeight(34); // Set the height of the icon
        // Create a button and set the icon on it
        Button backButton = new Button();
        backButton.setGraphic(imageView); // Set the icon as the button's graphic
        backButton.setStyle("-fx-background-color:rgb(144, 101, 211) ;");
        backButton.setOnAction(e -> {
            DashBoardStage dashBoardStage = new DashBoardStage();
            try {
                dashBoardStage.start(new Stage());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            primaryStage.close(); // Close the current stage

        });
        Button download = new Button("Download Expense PDF");
        download.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        showPasswordButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        download.setOnAction(e->{downloadPDF(UserSession.getLoggedInUser().getId());});
        vbox.getChildren().addAll(download,backButton);
        vbox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(vbox);
        stackPane.setAlignment(Pos.CENTER);
        BackgroundFill backgroundFillLight = new BackgroundFill(Color.rgb(144, 101, 211), CornerRadii.EMPTY, Insets.EMPTY);
        BackgroundFill backgroundFillDark = new BackgroundFill(Color.rgb(44, 44, 44), CornerRadii.EMPTY, Insets.EMPTY);
        if(checkForDarkMode_2()){
            stackPane.setBackground(new Background(backgroundFillDark));
            backButton.setStyle("-fx-background-color:rgb(44,44,44);");
            darkModeCheckBox.setTextFill(Color.WHITE);
            changeCurrencyLabel.setTextFill(Color.WHITE);
            darkModeCheckBox.setText("Disable");
        }
        else {
            stackPane.setBackground(new Background(backgroundFillLight));
            darkModeCheckBox.setText("Enable");
        }

        Scene scene = new Scene(stackPane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaxHeight(600);
        primaryStage.setMinHeight(600);
        primaryStage.setMaxWidth(600);
        primaryStage.setMinWidth(600);

        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String retrievePasswordFromDatabase(int userID) {
        String retrievedPassword = "";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT password FROM user WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                retrievedPassword = resultSet.getString("password");
            } else {
                retrievedPassword = "Password not found";
                CustomException c = new CustomException("Password not found");
                c.logException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomException c = new CustomException("SQL error");
            c.logException();
        }
        return retrievedPassword;
    }

    private void changePasswordInDatabase(int userID, String newPassword) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String updateQuery = "UPDATE user SET password = ? WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userID);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showSuccess("Password changed successfully.");
            } else {
                showAlert("Failed to change the password.");
                CustomException c = new CustomException("Failed to change the password");
                c.logException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomException c = new CustomException("SQL error");
            c.logException();
        }
    }

    private void checkForDarkMode() {
        String darkModeFilePath = "darkmode.txt";
        File file = new File(darkModeFilePath);
        if (file.exists()) {
            try {
                String fileContent = new String(Files.readAllBytes(Paths.get(darkModeFilePath))).trim();
                boolean isDarkModeEnabled = Boolean.parseBoolean(fileContent);
                darkModeCheckBox.setSelected(isDarkModeEnabled);
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
    private boolean checkForDarkMode_2() {
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
    private String readSelectedCurrencyFromFile() {
        File file = new File(SELECTED_CURRENCY_FILE);
        if (file.exists()) {
            try {
                return new String(Files.readAllBytes(Paths.get(SELECTED_CURRENCY_FILE))).trim();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    private void saveSelectedCurrencyToFile(String selectedCurrency) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SELECTED_CURRENCY_FILE))) {
            writer.write(selectedCurrency);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // Method to retrieve user's balance from the database
    private double retrieveUserBalance(int userID) {
        double userBalance = 0.0;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT balance FROM user WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userBalance = resultSet.getDouble("balance");
            } else {
                System.out.println("User balance not found for userID: " + userID);
                CustomException c = new CustomException("User balance not found for userID: " + userID);
                c.logException();

            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomException c = new CustomException("SQL error");
            c.logException();
            // Handle the SQL exception as per your application's requirements
        }

        return userBalance;
    }

    // Method to update amount in the expense table for the specific user
    private void updateExpenseAmounts(int userID, String selectedCurrency) {
        double conversionRate = 0.92; // Default conversion rate (1 USD = 0.92 EUR)
        if ("USD".equals(selectedCurrency)) {
            conversionRate = 0.92; // 1 USD = 0.92 EUR
        } else if ("EUR".equals(selectedCurrency)) {
            conversionRate = 1 / 0.92; // 1 EUR = 1.08695652 USD (approximately)
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String updateQuery = "UPDATE expense SET amount = ROUND(amount * ?, 2) WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setDouble(1, conversionRate);
            preparedStatement.setInt(2, userID);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Expense amounts updated based on currency change.");
            } else {
                showAlert("Failed to update expense amounts.");
                CustomException c = new CustomException("Failed to update expense amounts for user id " + userID);
                c.logException();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Method to calculate new balance based on the selected currency and conversion rate
    private double calculateNewBalance(String selectedCurrency, double conversionRate) {
        double initialBalance = retrieveUserBalance(UserSession.getLoggedInUser().getId());

        // Set the decimal format to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);

        if ("EUR".equals(selectedCurrency)) {
            // Conversion rate: 1 USD = 0.92 EUR
            return Double.parseDouble(df.format(initialBalance / 0.92)); // Convert balance from USD to Euro
        } else if ("USD".equals(selectedCurrency)) {
            // Conversion rate: 1 EUR = 1.08 USD
            return Double.parseDouble(df.format(initialBalance * 0.92)); // Convert balance from Euro to USD
        }
        return Double.parseDouble(df.format(initialBalance)); // Return the original balance if the currency is neither USD nor EUR
    }


    // Method to update balance in the user table
    private void updateUserBalance(int userID, double newBalance) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String updateQuery = "UPDATE user SET balance = ? WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setDouble(1, newBalance);
            preparedStatement.setInt(2, userID);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User's balance updated to " + newBalance);
            } else {
                showAlert("Failed to update user's balance.");
                CustomException c = new CustomException("Failed to update balance for user id " + userID);
                c.logException();

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void downloadPDF(int specificUserId) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            String query = "SELECT expense_id, description, date, category FROM expense WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, specificUserId); // Set the specific user_id
            ResultSet resultSet = statement.executeQuery();

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            int y = 700; // Initial y-coordinate for content

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, y);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                contentStream.showText(metaData.getColumnName(i) + "     ");
            }
            contentStream.newLine();
            contentStream.setFont(PDType1Font.HELVETICA, 10);

            // Print data rows
            while (resultSet.next()) {
                y -= 15; // Adjust vertical position for each row
                contentStream.newLineAtOffset(0, -15);
                for (int i = 1; i <= columnCount; i++) {
                    contentStream.showText(resultSet.getString(i) + "     ");
                }
                contentStream.newLine();
            }
            contentStream.endText();
            contentStream.close();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                document.save(file);
                document.close();
                showSuccess("PDF saved successfully!");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            CustomException c = new CustomException("Error creating PDF");
            c.logException();
            showAlert("Error creating PDF!");
        }
    }

}
