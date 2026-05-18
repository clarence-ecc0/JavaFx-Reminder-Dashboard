package com.reminder.backend.controller;

import com.reminder.backend.model.Reminder;
import com.reminder.backend.model.User;
import com.reminder.backend.repository.ReminderRepository;
import com.reminder.backend.repository.UserRepository;
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
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public List<Reminder> getAllReminders(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return reminderRepository.findByUserIdOrderByDueDateAsc(userId);
        }
        return reminderRepository.findAllByOrderByDueDateAsc();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Reminder> getReminderById(@PathVariable Long id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        return reminder.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    public List<Reminder> getActiveReminders(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return reminderRepository.findByUserIdAndCompleted(userId, false);
        }
        return reminderRepository.findByCompleted(false);
    }
    
    @PostMapping
    public ResponseEntity<Reminder> createReminder(@RequestBody Reminder reminder, @RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                reminder.setUser(user.get());
                return ResponseEntity.ok(reminderRepository.save(reminder));
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
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
