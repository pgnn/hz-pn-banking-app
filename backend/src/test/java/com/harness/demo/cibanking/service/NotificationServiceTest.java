package com.harness.demo.cibanking.service;

import com.harness.demo.cibanking.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the in-memory notification inbox and read/unread handling.
 */
class NotificationServiceTest {

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService();
    }

    @Test
    void getNotifications_returnsAllSeededNotifications() {
        assertThat(service.getNotifications()).hasSize(6);
    }

    @Test
    void getUnreadCount_countsOnlyUnread() {
        assertThat(service.getUnreadCount()).isEqualTo(2);
    }

    @Test
    void markRead_marksNotificationRead() {
        Notification n = service.markRead("ntf-001");
        assertThat(n).isNotNull();
        assertThat(n.isRead()).isTrue();
        assertThat(service.getUnreadCount()).isEqualTo(1);
    }

    @Test
    void markRead_isIdempotentOnAlreadyReadNotification() {
        Notification n = service.markRead("ntf-003");
        assertThat(n).isNotNull();
        assertThat(n.isRead()).isTrue();
        assertThat(service.getUnreadCount()).isEqualTo(2);
    }

    @Test
    void markRead_returnsNullForUnknownId() {
        assertThat(service.markRead("does-not-exist")).isNull();
        assertThat(service.getUnreadCount()).isEqualTo(2);
    }
}
