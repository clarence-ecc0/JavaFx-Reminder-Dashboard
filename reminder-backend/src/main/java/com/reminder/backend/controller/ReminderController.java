package com.reminder.backend.controller;

import com.reminder.backend.model.Reminder;
import com.reminder.backend.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {
    
    @Autowired
    private ReminderRepository reminderRepository;
    
    @GetMapping
    public List<Reminder> getAllReminders() {
        return reminderRepository.findAllByOrderByDueDateAsc();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Reminder> getReminderById(@PathVariable Long id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        return reminder.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    public List<Reminder> getActiveReminders() {
        return reminderRepository.findByCompleted(false);
    }
    
    @PostMapping
    public Reminder createReminder(@RequestBody Reminder reminder) {
        return reminderRepository.save(reminder);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Reminder> updateReminder(@PathVariable Long id, @RequestBody Reminder reminderDetails) {
        Optional<Reminder> optionalReminder = reminderRepository.findById(id);
        
        if (optionalReminder.isPresent()) {
            Reminder reminder = optionalReminder.get();
            reminder.setTitle(reminderDetails.getTitle());
            reminder.setDescription(reminderDetails.getDescription());
            reminder.setDueDate(reminderDetails.getDueDate());
            reminder.setCompleted(reminderDetails.isCompleted());
            reminder.setUpdatedAt(java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(reminderRepository.save(reminder));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        if (reminderRepository.existsById(id)) {
            reminderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
