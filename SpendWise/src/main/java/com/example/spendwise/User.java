package com.example.spendwise;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private double balance;

    public User(){

    }
    public User(int id,String username,String email){
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public User(String username, String email, String password, double balance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public StringProperty usernameProperty() {
        return new SimpleStringProperty(username);
    }

    public StringProperty emailProperty() {
        return new SimpleStringProperty(email);
    }

    public StringProperty passwordProperty() {
        return new SimpleStringProperty(password);
    }

    public DoubleProperty balanceProperty() {
        return new SimpleDoubleProperty(balance);
    }

    public String getPassword() {
        return this.password;
    }

    public double getBalance() {
        return this.balance;
    }
}
