package com.techie.microservices.notification.service;

import com.techie.microservices.order.event.OrderPlaceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed-topic")
    public void listen(OrderPlaceEvent orderPlaceEvent) {
        log.info("Got message from order placed topic {}", orderPlaceEvent);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlaceEvent.getEmail().toString());

            // Corrected subject formatting
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderPlaceEvent.getOrderNumber()));

            // Corrected body formatting
            messageHelper.setText(String.format("""
                    Hi,

                    Your order with order number %s is now placed successfully.

                    Best Regards,
                    Spring Shop
                    """, orderPlaceEvent.getOrderNumber()));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Notification email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }
}
