package com.pharmacy.sgpa.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
    private int id;
    private String username;
    private String action;
    private LocalDateTime timestamp;

    public LogEntry(int id, String username, String action, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getters
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Formatter for TableView
    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}