package com.clinictraining.medbackend.reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class ReminderQueue {

    private static final BlockingQueue<ReminderMessage> QUEUE = new LinkedBlockingQueue<>();

    public void enqueue(ReminderMessage message) {
        QUEUE.offer(message);
    }

    public List<ReminderMessage> drain() {
        List<ReminderMessage> messages = new ArrayList<>();
        QUEUE.drainTo(messages);
        return messages;
    }
}
