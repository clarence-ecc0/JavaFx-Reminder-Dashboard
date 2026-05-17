package com.reminder.frontend.controller;

import com.reminder.frontend.client.ReminderClient;
import com.reminder.frontend.model.Reminder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DashboardController {
    @FXML private TableView<Reminder> reminderTable;
    @FXML private TextField titleInput;
    @FXML private TextField descriptionInput;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button addButton;
    
    private ReminderClient reminderClient;
    
    @FXML
    public void initialize() {
        reminderClient = new ReminderClient();
        
        setupTimeSpinners();
        addButton.setOnAction(event -> handleAddReminder());
        setupActionsColumn();
        loadReminders();
    }
    
    private void setupTimeSpinners() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalTime.now().getHour()));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalTime.now().getMinute(), 5));
    }
    
    private void setupActionsColumn() {
        TableColumn<Reminder, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<Reminder, Void>() {
            private final Button completeBtn = new Button("Toggle");
            private final Button deleteBtn = new Button("Delete");
            
            {
                completeBtn.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                
                completeBtn.setOnAction(event -> {
                    Reminder reminder = getTableView().getItems().get(getIndex());
                    handleToggleCompletion(reminder);
                });
                
                deleteBtn.setOnAction(event -> {
                    Reminder reminder = getTableView().getItems().get(getIndex());
                    handleDelete(reminder);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, completeBtn, deleteBtn);
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });
        
        reminderTable.getColumns().add(actionsColumn);
    }
    
    private void loadReminders() {
        Task<List<Reminder>> task = new Task<List<Reminder>>() {
            @Override
            protected List<Reminder> call() throws Exception {
                return reminderClient.fetchReminders();
            }
        };
        
        task.setOnSucceeded(event -> {
            List<Reminder> reminders = task.getValue();
            reminderTable.getItems().setAll(reminders);
        });
        
        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load reminders");
            alert.setContentText("Could not connect to backend: " + task.getException().getMessage());
            alert.showAndWait();
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void handleAddReminder() {
        String title = titleInput.getText().trim();
        String description = descriptionInput.getText().trim();
        LocalDate date = datePicker.getValue();
        
        if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Title is required");
            alert.showAndWait();
            return;
        }
        
        if (date == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Due date is required");
            alert.showAndWait();
            return;
        }
        
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        LocalDateTime dueDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
        Reminder reminder = new Reminder(title, description, dueDateTime);
        
        Task<Reminder> task = new Task<Reminder>() {
            @Override
            protected Reminder call() throws Exception {
                return reminderClient.createReminder(reminder);
            }
        };
        
        task.setOnSucceeded(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Reminder created");
            alert.showAndWait();
            
            titleInput.clear();
            descriptionInput.clear();
            datePicker.setValue(null);
            
            loadReminders();
        });
        
        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to create reminder");
            alert.setContentText(task.getException().getMessage());
            alert.showAndWait();
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void handleToggleCompletion(Reminder reminder) {
        reminder.setCompleted(!reminder.isCompleted());
        
        Task<Reminder> task = new Task<Reminder>() {
            @Override
            protected Reminder call() throws Exception {
                return reminderClient.updateReminder(reminder.getId(), reminder);
            }
        };
        
        task.setOnSucceeded(event -> {
            loadReminders();
        });
        
        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to update reminder");
            alert.setContentText(task.getException().getMessage());
            alert.showAndWait();
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void handleDelete(Reminder reminder) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete reminder: " + reminder.getTitle() + "?");
        confirmAlert.setContentText("This action cannot be undone.");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    reminderClient.deleteReminder(reminder.getId());
                    return null;
                }
            };
            
            task.setOnSucceeded(event -> {
                loadReminders();
            });
            
            task.setOnFailed(event -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to delete reminder");
                alert.setContentText(task.getException().getMessage());
                alert.showAndWait();
            });
            
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
