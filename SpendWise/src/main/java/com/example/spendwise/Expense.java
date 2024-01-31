package com.example.spendwise;

import javafx.beans.property.*;

public class Expense {
    private final IntegerProperty expenseId = new SimpleIntegerProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();

    public Expense(){

    }
    public Expense(int expenseId, String description, double amount, String date, String category, int userId) {
        setExpenseId(expenseId);
        setDescription(description);
        setAmount(amount);
        setDate(date);
        setCategory(category);
        setUserId(userId);
    }

    public int getExpenseId() {
        return expenseId.get();
    }

    public void setExpenseId(int expenseId) {
        this.expenseId.set(expenseId);
    }

    public IntegerProperty expenseIdProperty() {
        return expenseId;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }
}
