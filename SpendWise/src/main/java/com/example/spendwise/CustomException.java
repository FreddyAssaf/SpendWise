package com.example.spendwise;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomException extends RuntimeException {
    private final String logMessage;

    public CustomException(String logMessage) {
        super(logMessage);
        this.logMessage = logMessage;
    }

    public void logException() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("error_log.txt", true))) {
            writer.println(getFormattedTimestamp() + ": " + logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFormattedTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return dateFormat.format(now);
    }
}
