package com.pharmacy.sgpa.controller;

import com.pharmacy.sgpa.dao.LoggerDAO;
import com.pharmacy.sgpa.model.LogEntry;
import com.pharmacy.sgpa.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LogsController {

    @FXML private TableView<LogEntry> tableLogs;
    @FXML private TableColumn<LogEntry, String> colDate;
    @FXML private TableColumn<LogEntry, String> colUser;
    @FXML private TableColumn<LogEntry, String> colAction;

    private LoggerDAO loggerDAO;

    @FXML
    public void initialize() {
        loggerDAO = new LoggerDAO();

        // Configuration of columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        refreshLogs();
    }

    @FXML
    public void refreshLogs() {
        tableLogs.setItems(FXCollections.observableArrayList(loggerDAO.getAllLogs()));
    }

    @FXML
    public void handleBack(ActionEvent event) {
        NavigationUtil.navigateTo(event, "/fxml/DashboardView.fxml");
    }
}