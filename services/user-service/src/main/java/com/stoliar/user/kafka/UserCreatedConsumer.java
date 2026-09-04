package com.stoliar.user.kafka;

import com.stoliar.events.user.UserCreated;
import com.stoliar.user.service.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedConsumer {

    private static final String TOPIC = "user.created";

    private final UserService userService;

    public UserCreatedConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(
            topics = TOPIC,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UserCreated event) {
        userService.createUser(event);
    }
}