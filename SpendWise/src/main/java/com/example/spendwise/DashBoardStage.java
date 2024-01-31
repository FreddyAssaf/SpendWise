package com.example.spendwise;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DashBoardStage extends Application {
    private Stage dashBoardStage;
    Text amountBalance = new Text("535");
    Text amountShopping = new Text("322");
    Text amountFoodDrinks = new Text("55");
    Text amountBillsUtilities = new Text("566");
    Text amountOthers = new Text("11");
    TextField descriptionField = new TextField();
    TextField amountField = new TextField();
    DatePicker datePicker = new DatePicker();
    RadioButton shoppingRadioButton = new RadioButton("Shopping");
    RadioButton foodDrinksRadioButton = new RadioButton("Food & Drinks");
    RadioButton billsUtilitiesRadioButton = new RadioButton("Bills & Utilities");
    RadioButton othersRadioButton = new RadioButton("Others");
    Button addButton = new Button("Add");
    TableView<Expense> tableView = new TableView<>();
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/spendwise_schema";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fa12##1993";

    public void start(Stage primaryStage) throws Exception {

        updateTextFieldValues(UserSession.getLoggedInUser().getId(), amountBalance,amountShopping,amountBillsUtilities,amountFoodDrinks,amountOthers);
        dashBoardStage = primaryStage;

        // Create a PieChart
        PieChart pieChart = new PieChart();

// Create data for the PieChart using the values from Text objects
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Balance", Double.parseDouble(amountBalance.getText())),
                new PieChart.Data("Shopping", Double.parseDouble(amountShopping.getText())),
                new PieChart.Data("Food & Drinks", Double.parseDouble(amountFoodDrinks.getText())),
                new PieChart.Data("Bills & Utilities", Double.parseDouble(amountBillsUtilities.getText())),
                new PieChart.Data("Others", Double.parseDouble(amountOthers.getText()))
        );

// Set the data to the PieChart
        pieChart.setData(pieChartData);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(4000), pieChart);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();


        ToggleGroup categoryToggleGroup = new ToggleGroup();
        shoppingRadioButton.setToggleGroup(categoryToggleGroup);
        foodDrinksRadioButton.setToggleGroup(categoryToggleGroup);
        billsUtilitiesRadioButton.setToggleGroup(categoryToggleGroup);
        othersRadioButton.setToggleGroup(categoryToggleGroup);

        addButton.setStyle("-fx-background-color: #FFA07A; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        addButton.setOnAction(event -> {
            String description = descriptionField.getText().trim();
            String amountText = amountField.getText().trim();
            LocalDate date = datePicker.getValue();
            RadioButton selectedRadioButton = (RadioButton) categoryToggleGroup.getSelectedToggle();

            if (description.isEmpty() || amountText.isEmpty() || date == null || selectedRadioButton == null) {
                // Validate if all fields are filled
                showAlert("Please fill all fields.");
                CustomException c = new CustomException("Not filling all the fields");
                c.logException();
                return;
            }
            if (Double.parseDouble(amountText) < 0) {
                showAlert("Amount cannot be negative");
                CustomException c = new CustomException("Entering negative amount");
                c.logException();
            } else {
                String category = "";
                if (shoppingRadioButton.isSelected()) {
                    category = "shopping";
                } else if (foodDrinksRadioButton.isSelected()) {
                    category = "foodAndDrinks";
                } else if (billsUtilitiesRadioButton.isSelected()) {
                    category = "billsAndUtilities";
                } else if (othersRadioButton.isSelected()) {
                    category = "others";
                }

                if (category.isEmpty()) {
                    showAlert("Please select a category.");
                    CustomException c = new CustomException("Not Selecting a category");
                    c.logException();
                    return;
                }

                try {
                    Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                    if (connection != null) {
                        String countQuery = "SELECT COUNT(*) AS expenses_count FROM expense WHERE DATE(date) = CURRENT_DATE() AND user_id = ?";
                        PreparedStatement countStatement = connection.prepareStatement(countQuery);
                        countStatement.setInt(1, UserSession.getLoggedInUser().getId());

                        ResultSet countResult = countStatement.executeQuery();
                        if (countResult.next()) {
                            int expensesCount = countResult.getInt("expenses_count");

                            if (expensesCount >= 10) {
                                showAlert("You have reached the maximum expenses limit for today.");
                                CustomException c = new CustomException("Reaching the maximum limit for a specific date");
                                c.logException();
                                return;
                            } else {
                                // Query to get the user's balance
                                String balanceQuery = "SELECT balance FROM user WHERE user_id = ?";
                                PreparedStatement balanceStatement = connection.prepareStatement(balanceQuery);
                                balanceStatement.setInt(1, UserSession.getLoggedInUser().getId());

                                ResultSet balanceResult = balanceStatement.executeQuery();
                                if (balanceResult.next()) {
                                    double userBalance = balanceResult.getDouble("balance");
                                    double expenseAmount = Double.parseDouble(amountText);

                                    if (expenseAmount > userBalance) {
                                        showAlert("Expense amount exceeds your balance.");
                                        CustomException c = new CustomException("Expense amount exceeding the balance");
                                        c.logException();
                                        return;
                                    } else if (expenseAmount > userBalance/2) {
                                        showWarning("The amount entered is greater than half of your balance");
                                    }

                                    // If the expense amount is within the balance, proceed to add the expense
                                    String insertQuery = "INSERT INTO expense (description, amount, date, category, user_id) VALUES (?, ?, ?, ?, ?)";
                                    PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                                    preparedStatement.setString(1, description);
                                    preparedStatement.setDouble(2, expenseAmount);
                                    preparedStatement.setDate(3, Date.valueOf(date));
                                    preparedStatement.setString(4, category);
                                    preparedStatement.setInt(5, UserSession.getLoggedInUser().getId());

                                    // Execute insert query
                                    int rowsInserted = preparedStatement.executeUpdate();
                                    if (rowsInserted > 0) {
                                        // Update user's balance after deducting the expense amount
                                        String updateBalanceQuery = "UPDATE user SET balance = ? WHERE user_id = ?";
                                        double updatedBalance = userBalance - expenseAmount;
                                        PreparedStatement updateBalanceStatement = connection.prepareStatement(updateBalanceQuery);
                                        updateBalanceStatement.setDouble(1, updatedBalance);
                                        updateBalanceStatement.setInt(2, UserSession.getLoggedInUser().getId());
                                        updateBalanceStatement.executeUpdate();
                                        populateExpenseTable(tableView);

                                        showSuccess("Expense added successfully.");
                                        // Update text field values
                                        updateTextFieldValues(UserSession.getLoggedInUser().getId(), amountBalance, amountShopping, amountBillsUtilities, amountFoodDrinks, amountOthers);
                                        // Update Pie Chart
                                        updatePieChartData(pieChart, amountBalance.getText(), amountShopping.getText(), amountBillsUtilities.getText(), amountFoodDrinks.getText(), amountOthers.getText());
                                    }
                                }
                            }
                            }
                        } else {
                            showAlert("Failed to connect to the database!");
                            CustomException c = new CustomException("Error connecting to database");
                        c.logException();
                        }
                    } catch(SQLException e){
                        e.printStackTrace();
                        showAlert("Failed to add expense. Please try again.");
                    CustomException c = new CustomException("Error adding expense");
                    c.logException();
                    } catch(RuntimeException e){
                        throw new RuntimeException(e);
                    }
                }
            });



        Text txt = new Text("Add Expense");
        txt.setFill(Color.WHITE);
        txt.setFont(Font.font("Arial",FontWeight.BOLD,30));

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: rgb(102, 51, 153); -fx-border-radius: 10px;-fx-border-width: 2px;");

        Label descriptionLabel = new Label("Description:");
        Label amountLabel = new Label("Amount:");
        Label dateLabel = new Label("Date:");
        Label categoryLabel = new Label("Category:");

        VBox categories = new VBox(5, shoppingRadioButton, foodDrinksRadioButton, billsUtilitiesRadioButton, othersRadioButton);

        vbox.getChildren().addAll(txt,descriptionLabel, descriptionField, amountLabel, amountField, dateLabel, datePicker, categoryLabel, categories, addButton);
        vbox.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));


        VBox menu = new VBox();
        menu.setPadding(new Insets(10));
        menu.setSpacing(20);
        menu.setAlignment(Pos.CENTER_LEFT); // Align VBox contents to the left
        menu.setStyle("-fx-background-color: #f4f4f4;");

        Image logoImage = new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(150); // Adjust the width as needed
        logoImageView.setPreserveRatio(true);

        Button dashBoard = createButton("DashBoard", "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\dashboard.png");
        Button addToCash = createButton("Add Cash", "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\cash.png");
        addToCash.setOnAction(e->{
            try {
                openAddCash();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        Button settings = createButton("Settings", "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\settings.png");
        settings.setOnAction(e->{
            SettingsStage settingsStage = new SettingsStage();
            try {
                settingsStage.start(new Stage());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            dashBoardStage.close();
        });
        Button logout = createButton("Logout", "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\logout.png");
        logout.setOnAction(e->{logoutToWelcomeStage();});

        // Adding elements to the menu VBox
        menu.getChildren().addAll(logoImageView, dashBoard, addToCash, settings, logout);
        if (checkForDarkMode()) {
            // Set dark mode background color for the BorderPane
            menu.setStyle("-fx-background-color: rgb(44, 44, 44);"); // Dark mode color
            menu.lookupAll(".button").forEach(node -> node.setStyle("-fx-text-fill: white;-fx-background-color: rgb(44, 44, 44);"));
        } else {
            // Set light mode background color for the BorderPane
            menu.setStyle("-fx-background-color: rgb(200, 200, 200);"); // Light mode color
            menu.lookupAll(".button").forEach(node -> node.setStyle("-fx-text-fill: black;-fx-background-color: rgb(200, 200, 200);"));
        }




        // Border pane to set the menu in the center-left
        BorderPane borderPane = new BorderPane();

        HBox hbox = new HBox();
        hbox.setSpacing(60); // Set spacing between squares
        hbox.setAlignment(Pos.CENTER);

        // Creating StackPanes to hold the squares and VBoxes for each category
        StackPane balance = createStackPane("Balance", amountBalance, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\cash.png", Color.rgb(51, 153, 102));
        StackPane shopping = createStackPane("Shopping", amountShopping, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\shopping.png", Color.rgb(51, 102, 153));
        StackPane foodDrinks = createStackPane("Food & Drinks", amountFoodDrinks, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\food.png", Color.rgb(153, 51, 102));
        StackPane billsUtilities = createStackPane("Bills & Utilities", amountBillsUtilities, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\bills.png", Color.rgb(102, 51, 153));
        StackPane others = createStackPane("Others", amountOthers, "file:\\Users\\fredd\\OneDrive\\Desktop\\image\\others.png", Color.rgb(153, 153, 102));

        // Adding the StackPanes to the HBox
        hbox.getChildren().addAll(balance, shopping, foodDrinks, billsUtilities, others);

        // TableView
        //columns and their cell value factories
        TableColumn<Expense, Integer> idColumn = new TableColumn<>("Expense ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().expenseIdProperty().asObject());

        TableColumn<Expense, String> descColumn = new TableColumn<>("Description");
        descColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        TableColumn<Expense, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        TableColumn<Expense, Integer> userIdColumn = new TableColumn<>("User ID");
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());

        // Add columns to TableView
        tableView.getColumns().addAll(idColumn, descColumn, amountColumn, dateColumn, categoryColumn, userIdColumn);

        ArrayList<Expense> expenses = new ArrayList<>();
        // Add sample data (replace this with your actual data)
        ObservableList<Expense> data = FXCollections.observableArrayList(expenses);
        tableView.setItems(data);
        populateExpenseTable(tableView);


        HBox hBox2 =new HBox();
        hBox2.setPadding(new Insets(20,20,20,0));
        hBox2.setSpacing(20);
        //hBox2.setAlignment(Pos.CENTER);
        hBox2.getChildren().addAll(tableView,vbox,pieChart);


        borderPane.setLeft(menu);
        BorderPane.setAlignment(menu, Pos.CENTER_LEFT); // Align VBox in the BorderPane
        borderPane.setTop(hbox);
        BorderPane.setAlignment(hbox,Pos.CENTER_RIGHT);
        borderPane.setCenter(hBox2);
        // Inside the start method

        if (checkForDarkMode()) {
            // Set dark mode background color for the BorderPane
            borderPane.setStyle("-fx-background-color: rgb(44, 44, 44);"); // Dark mode color
        } else {
            // Set light mode background color for the BorderPane
            borderPane.setStyle("-fx-background-color: rgb(200, 200, 200);"); // Light mode color
        }


        Scene scene = new Scene(borderPane, 400, 400);

        // Creating the scene and setting it in the stage

        primaryStage.setScene(scene);
        primaryStage.setTitle("Dash Board");
        primaryStage.getIcons().add(new Image("file:\\Users\\fredd\\OneDrive\\Desktop\\image\\spendwiselogo.png"));
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Button createButton(String buttonText, String iconFileName) {
        Image icon = new Image(iconFileName);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(20); // Adjust icon width as needed
        imageView.setPreserveRatio(true);

        Button button = new Button(buttonText, imageView);
        button.setContentDisplay(ContentDisplay.TOP); // Set text below the icon
        button.setAlignment(Pos.CENTER); // Center-align text
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
        button.setPrefWidth(150); // Set preferred button width

        return button;
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
        Image iconImage = new Image(iconPath);
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
    private void openAddCash() throws Exception {
        AddCash addCash = new AddCash();
        addCash.start(new Stage());
        dashBoardStage.close();
    }
    private void logoutToWelcomeStage(){
        WelcomeStage welcomeStage = new WelcomeStage();
        welcomeStage.start(new Stage());
        dashBoardStage.close();
    }
    private void updateTextFieldValues(int userId, Text amountBalanceField, Text amountShoppingField,
                                       Text amountBillsAndUtilitiesField, Text amountFoodDrinksField,
                                       Text amountOthersField) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            if (connection != null) {
                String amountBalanceQuery = "SELECT balance AS totalBalance FROM user WHERE user_id = ?";
                String amountShoppingQuery = "SELECT SUM(amount) AS totalShopping FROM expense WHERE user_id = ? AND category = 'shopping'";
                String amountBillsAndUtilitiesQuery = "SELECT SUM(amount) AS totalBillsAndUtilities FROM expense WHERE user_id = ? AND category = 'billsAndUtilities'";
                String amountFoodDrinksQuery = "SELECT SUM(amount) AS totalFoodDrinks FROM expense WHERE user_id = ? AND category = 'foodAndDrinks'";
                String amountOthersQuery = "SELECT SUM(amount) AS totalOthers FROM expense WHERE user_id = ? AND category = 'others'";

                PreparedStatement balanceStatement = connection.prepareStatement(amountBalanceQuery);
                PreparedStatement shoppingStatement = connection.prepareStatement(amountShoppingQuery);
                PreparedStatement billsUtilitiesStatement = connection.prepareStatement(amountBillsAndUtilitiesQuery);
                PreparedStatement foodDrinksStatement = connection.prepareStatement(amountFoodDrinksQuery);
                PreparedStatement othersStatement = connection.prepareStatement(amountOthersQuery);

                balanceStatement.setInt(1, userId);
                shoppingStatement.setInt(1, userId);
                billsUtilitiesStatement.setInt(1, userId);
                foodDrinksStatement.setInt(1, userId);
                othersStatement.setInt(1, userId);

                ResultSet balanceResult = balanceStatement.executeQuery();
                ResultSet shoppingResult = shoppingStatement.executeQuery();
                ResultSet billsUtilitiesResult = billsUtilitiesStatement.executeQuery();
                ResultSet foodDrinksResult = foodDrinksStatement.executeQuery();
                ResultSet othersResult = othersStatement.executeQuery();

                if (balanceResult.next()) {
                    double amountBalance = balanceResult.getDouble("totalBalance");
                    amountBalanceField.setText(String.valueOf(amountBalance));
                }
                if (shoppingResult.next()) {
                    double amountShopping = shoppingResult.getDouble("totalShopping");
                    amountShoppingField.setText(String.valueOf(amountShopping));
                }
                if (billsUtilitiesResult.next()) {
                    double amountBillsAndUtilities = billsUtilitiesResult.getDouble("totalBillsAndUtilities");
                    amountBillsAndUtilitiesField.setText(String.valueOf(amountBillsAndUtilities));
                }
                if (foodDrinksResult.next()) {
                    double amountFoodDrinks = foodDrinksResult.getDouble("totalFoodDrinks");
                    amountFoodDrinksField.setText(String.valueOf(amountFoodDrinks));
                }
                if (othersResult.next()) {
                    double amountOthers = othersResult.getDouble("totalOthers");
                    amountOthersField.setText(String.valueOf(amountOthers));
                }

                System.out.println("Text field values updated successfully!");
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
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void updatePieChartData(PieChart pieChart, String balance, String shopping, String billsUtilities, String foodDrinks, String others) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Balance", Double.parseDouble(balance)),
                new PieChart.Data("Shopping", Double.parseDouble(shopping)),
                new PieChart.Data("Food & Drinks", Double.parseDouble(foodDrinks)),
                new PieChart.Data("Bills & Utilities", Double.parseDouble(billsUtilities)),
                new PieChart.Data("Others", Double.parseDouble(others))
        );

        pieChart.setData(pieChartData);
    }
    private void populateExpenseTable(TableView<Expense> tableView) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            if (connection != null) {
                String selectQuery = "SELECT * FROM expense WHERE user_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                preparedStatement.setInt(1, UserSession.getLoggedInUser().getId());

                ResultSet resultSet = preparedStatement.executeQuery();
                ObservableList<Expense> expensesList = FXCollections.observableArrayList();

                while (resultSet.next()) {
                    // Create Expense objects and add them to the list
                    Expense expense = new Expense();
                    expense.setExpenseId(resultSet.getInt("expense_id"));
                    expense.setDescription(resultSet.getString("description"));
                    expense.setAmount(resultSet.getDouble("amount"));
                    expense.setDate(resultSet.getString("date"));
                    expense.setCategory(resultSet.getString("category"));
                    expense.setUserId(resultSet.getInt("user_id"));

                    expensesList.add(expense);
                }

                // Set the data in the TableView
                tableView.setItems(expensesList);
            } else {
                showAlert("Failed to connect to the database!");
                CustomException c = new CustomException("Error connecting to database");
                c.logException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to fetch expenses. Please try again.");
            CustomException c = new CustomException("Fetching expenses failed");
            c.logException();
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

