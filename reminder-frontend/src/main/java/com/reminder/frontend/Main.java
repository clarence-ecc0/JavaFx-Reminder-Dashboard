package com.reminder.frontend;

import com.reminder.frontend.client.ReminderClient;
import com.reminder.frontend.scheduler.ReminderScheduler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    private ReminderScheduler scheduler;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        BorderPane root = loader.load();
        
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Reminder Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        scheduler = new ReminderScheduler(new ReminderClient());
        scheduler.start();
    }
    
    @Override
    public void stop() throws Exception {
        if (scheduler != null) {
            scheduler.stop();
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
