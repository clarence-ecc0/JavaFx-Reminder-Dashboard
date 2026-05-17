package com.reminder.backend.repository;

import com.reminder.backend.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByCompleted(boolean completed);
    List<Reminder> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    List<Reminder> findAllByOrderByDueDateAsc();
}
