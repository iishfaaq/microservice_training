package com.techie.microservices.notification_service;

import com.techie.microservices.notification.NotificationServiceApplication;
import org.springframework.boot.SpringApplication;

public class TestNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
