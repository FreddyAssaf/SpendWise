package com.example.spendwise;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.imageio.ImageIO;
import java.sql.*;

public class Admin extends Application {

    private static final int ROW_HEIGHT = 20;
    private static final int CELL_MARGIN = 2;
    private TableView<User> tableView;
    private TextField usernameField, emailField, passwordField;
    private TextField currentUsernameField,newEmailField, newPasswordField;
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // First Form - Add User
        VBox addUserForm = createUserForm("Add User");
        Button addButton = new Button("Add");
        addButton.setOnAction(e->{addUser();});
        addUserForm.getChildren().addAll(addButton);

        // Second Form - Change Email & Password
        VBox changeForm = createUserForm("Change Email & Password");
        Button changeButton = new Button("Change");
        changeButton.setOnAction(e->{changeEmailAndPassword();});
        changeForm.getChildren().addAll(changeButton);

        // Table View
        tableView = new TableView<>();
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        TableColumn<User, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());

        TableColumn<User, Double> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(cellData -> cellData.getValue().balanceProperty().asObject());

        tableView.getColumns().addAll(usernameCol, emailCol, passwordCol, balanceCol);
        tableView.setItems(getDataFromDatabase());
        // Set preferred height for the TableView
        tableView.setPrefHeight(400); // Set the preferred height

// Set the TableView to resize with its container
        VBox.setVgrow(tableView, Priority.ALWAYS);

        VBox.setVgrow(tableView, Priority.ALWAYS);

        VBox formsAndTable = new VBox(20, addUserForm, changeForm, tableView);

        // ID Delete Section
        VBox userNameDeleteSection = new VBox(10);
        userNameDeleteSection.setPadding(new Insets(10));
        userNameDeleteSection.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        TextField idField = new TextField();
        idField.setPromptText("Username");
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e->{
            deleteUser(idField.getText());
        });

        userNameDeleteSection.getChildren().addAll(idField, deleteButton);

        // Final layout
        VBox mainLayout = new VBox(20, formsAndTable, userNameDeleteSection);
        root.setCenter(mainLayout);
        Button downloadPDFButton = new Button("Download as PDF");
        downloadPDFButton.setOnAction(e->{downloadPDF();});
        VBox buttonContainer = new VBox(downloadPDFButton);
        mainLayout.getChildren().add(buttonContainer);


        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Admin Panel");
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void downloadPDF() {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            String query = "SELECT * FROM user";
            PreparedStatement statement = connection.prepareStatement(query);
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
                showAlert("PDF saved successfully!");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            CustomException c = new CustomException("Error creating PDF");
            c.logException();
            showAlert("Error creating PDF!");
        }
    }




    private VBox createUserForm(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox form = new VBox(10);
        form.getChildren().add(titleLabel);

        if (title.equals("Add User")) {
            usernameField = new TextField();
            usernameField.setPromptText("Username");

            emailField = new TextField();
            emailField.setPromptText("Email");

            passwordField = new PasswordField();
            passwordField.setPromptText("Password");

            form.getChildren().addAll(usernameField, emailField, passwordField);
        } else if (title.equals("Change Email & Password")) {
            currentUsernameField = new TextField();
            currentUsernameField.setPromptText("Current Username");

            newEmailField = new TextField();
            newEmailField.setPromptText("New Email");

            newPasswordField = new PasswordField();
            newPasswordField.setPromptText("New Password");

            form.getChildren().addAll(currentUsernameField, newEmailField, newPasswordField);
        }

        form.setStyle("-fx-padding: 20px; -fx-border-color: black; -fx-border-width: 2px;");

        return form;
    }
    public static void main(String[] args) {
        launch(args);
    }
    private ObservableList<User> getDataFromDatabase() {
        ObservableList<User> users = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "SELECT username, email, password, balance FROM user";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("password");
                    double balance = resultSet.getDouble("balance");
                    users.add(new User(username, email, password, balance));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomException c = new CustomException("SQL ERROR");
            c.logException();
        }

        // Print the retrieved data to check if it's getting fetched properly
        for (User user : users) {
            System.out.println(user.getUsername() + " " + user.getEmail() + " " + user.getPassword() + " " + user.getBalance());
        }

        return users;
    }
    private void deleteUser(String username) {
        if (username.isEmpty()) {
            // Show an alert if the username field is empty
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a username to delete.");
            alert.showAndWait();
        } else {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                String query = "DELETE FROM user WHERE username = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, username);
                    int affectedRows = statement.executeUpdate();

                    if (affectedRows > 0) {
                        // If deletion is successful, remove the user from the table view
                        ObservableList<User> items = tableView.getItems();
                        items.removeIf(user -> user.getUsername().equals(username));
                    } else {
                        // Show an alert if the user with the given username does not exist
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("No user found with the given username.");
                        alert.showAndWait();
                        CustomException c = new CustomException("Entering an invalid username to delete");
                        c.logException();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Show an alert for any database-related error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error occurred while deleting the user.");
                alert.showAndWait();
                CustomException c = new CustomException("SQL ERROR");
                c.logException();
            }
        }
    }
    private void addUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Please fill in all fields!");
            CustomException c = new CustomException("Not filling all the fields");
            c.logException();
            return;
        }
        if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
            showAlert("Invalid email format");
            CustomException c = new CustomException("Invalid email Format");
            c.logException();
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String usernameQuery = "SELECT COUNT(*) AS count FROM user WHERE username = ?";
            String emailQuery = "SELECT COUNT(*) AS count FROM user WHERE email = ?";

            boolean usernameExists = isExists(connection, usernameQuery, username);
            boolean emailExists = isExists(connection, emailQuery, email);

            if (usernameExists || emailExists) {
                if (usernameExists) {
                    showAlert("Username already exists!");
                    CustomException c = new CustomException("Username already exists");
                    c.logException();
                }
                if (emailExists) {
                    showAlert("Email already exists!");
                    CustomException c = new CustomException("Email already exists");
                    c.logException();
                }
            } else {
                String insertQuery = "INSERT INTO user (username, email, password, balance) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setString(3, password);
                    statement.setDouble(4, 0.0); // Setting balance as 0 for a new user
                    int affectedRows = statement.executeUpdate();

                    if (affectedRows > 0) {
                        tableView.getItems().add(new User(username, email, password, 0.0)); // Add to table view
                        clearFields(); // Clear input fields after successful addition
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            CustomException c = new CustomException("SQL ERROR");
            c.logException();
        }
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        newEmailField.clear();
        newPasswordField.clear();
    }


    private boolean isExists(Connection connection, String query, String parameter) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }
    private void showAlert(String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
    private void changeEmailAndPassword() {
        String currentUsername = currentUsernameField.getText();
        String newEmail = newEmailField.getText();
        String newPassword = newPasswordField.getText();

        if (currentUsername.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty()) {
            showAlert("Please fill in all fields!");
            CustomException c = new CustomException("Not filling all the fields for change");
            c.logException();
            return;
        }
        if (!newEmail.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showAlert("Invalid email format");
            CustomException c = new CustomException("Invalid email format for change");
            c.logException();
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            if (isNewEmailExists(connection, newEmail)) {
                showAlert("Email already exists!");
                CustomException c = new CustomException("Email already exists for change");
                c.logException();
                return;
            }

            String query = "UPDATE user SET email = ?, password = ? WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newEmail);
                statement.setString(2, newPassword);
                statement.setString(3, currentUsername);
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    showAlert("Email and password updated successfully!");
                    tableView.setItems(getDataFromDatabase());
                    clearFields();
                } else {
                    showAlert("User not found with the given username!");
                    CustomException c = new CustomException("User not found for change");
                    c.logException();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            CustomException c = new CustomException("SQL ERROR for change");
            c.logException();
        }
    }

    private boolean isNewEmailExists(Connection connection, String newEmail) throws SQLException {
        String emailQuery = "SELECT COUNT(*) AS count FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(emailQuery)) {
            statement.setString(1, newEmail);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }



}

