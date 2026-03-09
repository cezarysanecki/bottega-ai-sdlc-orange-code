package com.clinictraining.medbackend.web;

import com.clinictraining.medbackend.reminder.ReminderMessage;
import com.clinictraining.medbackend.reminder.ReminderQueue;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReminderQueue reminderQueue;

    public NotificationController(ReminderQueue reminderQueue) {
        this.reminderQueue = reminderQueue;
    }

    @GetMapping("/poll")
    public ResponseEntity<List<ReminderMessage>> poll() {
        return ResponseEntity.ok(reminderQueue.drain());
    }
}
