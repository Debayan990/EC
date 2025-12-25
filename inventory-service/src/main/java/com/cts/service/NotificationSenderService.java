package com.cts.service;

import com.cts.client.NotificationServiceClient;
import com.cts.dtos.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderService {

    private final NotificationServiceClient notificationServiceClient;     // Inject the Feign Client

    public void sendLowStockNotification(Long itemId) {
        try {
            NotificationDto notification = new NotificationDto();
            notification.setEventType("LowStock");
            notification.setRecipient("inventory.manager@company.com");
            notification.setMessage("Stock for item " + itemId + " is low.");
            notificationServiceClient.sendNotification(notification);
        } catch (Exception e) {
            log.error("Failed to send low stock notification for item: {}. Error: {}", itemId, e.getMessage());
        }
    }
}
