package com.reminder.frontend.scheduler;

import com.reminder.frontend.client.ReminderClient;
import com.reminder.frontend.model.Reminder;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    private ScheduledExecutorService scheduler;
    private ReminderClient reminderClient;
    private Long userId;
    private static final long CHECK_INTERVAL_SECONDS = 30;
    
    public ReminderScheduler(ReminderClient reminderClient, Long userId) {
        this.reminderClient = reminderClient;
        this.userId = userId;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkReminders, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    public void stop() {
        scheduler.shutdown();
    }
    
    private void checkReminders() {
        try {
            List<Reminder> reminders = reminderClient.fetchReminders(userId);
            LocalDateTime now = LocalDateTime.now();
            
            for (Reminder reminder : reminders) {
                if (!reminder.isCompleted() && reminder.getDueDate() != null) {
                    if (isTimeToRemind(reminder.getDueDate(), now)) {
                        showAlert(reminder);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking reminders: " + e.getMessage());
        }
    }
    
    private boolean isTimeToRemind(LocalDateTime dueDate, LocalDateTime now) {
        long secondsDiff = java.time.temporal.ChronoUnit.SECONDS.between(dueDate, now);
        return secondsDiff >= 0 && secondsDiff < CHECK_INTERVAL_SECONDS;
    }
    
    private void showAlert(Reminder reminder) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reminder Alert");
            alert.setHeaderText("Time for: " + reminder.getTitle());
            alert.setContentText(reminder.getDescription());
            alert.showAndWait();
        });
    }
}
