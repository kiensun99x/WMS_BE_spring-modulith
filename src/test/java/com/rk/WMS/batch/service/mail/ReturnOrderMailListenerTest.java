package com.rk.WMS.batch.service.mail;


import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ReturnOrderMailListener
 * Mục đích: Kiểm tra listener gửi mail khi có đơn hàng hoàn trả
 */
@ExtendWith(MockitoExtension.class)
class ReturnOrderMailListenerTest {

    @Mock
    private MailService mailService;

    @Mock
    private MailTemplateReader templateReader;

    @InjectMocks
    private ReturnOrderMailListener mailListener;

    private OrdersReturnedEvent event;
    private List<ReturnOrderPayload> payloads;

    @BeforeEach
    void setUp() {
        payloads = Arrays.asList(
                createPayload(1L, "ORD-001", "receiver1@test.com", "supplier1@test.com"),
                createPayload(2L, "ORD-002", "receiver2@test.com", "supplier2@test.com")
        );
        event = new OrdersReturnedEvent(payloads);
    }

    /**
     * Tạo payload cho test
     */
    private ReturnOrderPayload createPayload(Long orderId, String orderCode,
                                             String receiverEmail, String supplierEmail) {
        return ReturnOrderPayload.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .warehouseId(100L)
                .supplierName("Supplier " + orderId)
                .supplierEmail(supplierEmail)
                .receiverName("Receiver " + orderId)
                .receiverEmail(receiverEmail)
                .failedDeliveryCount(3)
                .actor("system")
                .eventTime(LocalDateTime.now())
                .build();
    }

    /**
     * Test case: Xử lý event thành công
     */
    @Test
    @DisplayName("handle - Xử lý event thành công")
    void handle_Success() {
        // Given
        when(templateReader.read(anyString(), anyMap()))
                .thenReturn("Rendered template content");
        doNothing().when(mailService).send(anyString(), anyString(), anyString());

        // When
        mailListener.handle(event);

        // Then - Gửi 2 mail cho receiver và 2 mail cho supplier = 4 mail
        verify(mailService, times(4)).send(anyString(), anyString(), anyString());
        verify(templateReader, times(4)).read(anyString(), anyMap());
    }

    /**
     * Test case: Xử lý event với payload rỗng
     */
    @Test
    @DisplayName("handle - Event với payload rỗng")
    void handle_EmptyPayload() {
        // Given
        OrdersReturnedEvent emptyEvent = new OrdersReturnedEvent(Arrays.asList());

        // When
        mailListener.handle(emptyEvent);

        // Then
        verify(mailService, never()).send(anyString(), anyString(), anyString());
    }

    /**
     * Test case: Xử lý event với 1 payload
     */
    @Test
    @DisplayName("handle - Event với 1 payload")
    void handle_SinglePayload() {
        // Given
        OrdersReturnedEvent singleEvent = new OrdersReturnedEvent(
                Arrays.asList(payloads.get(0)));

        when(templateReader.read(anyString(), anyMap()))
                .thenReturn("Rendered template content");
        doNothing().when(mailService).send(anyString(), anyString(), anyString());

        // When
        mailListener.handle(singleEvent);

        // Then - Gửi 1 mail cho receiver và 1 mail cho supplier = 2 mail
        verify(mailService, times(2)).send(anyString(), anyString(), anyString());
    }

    /**
     * Test case: Kiểm tra template path cho receiver
     */
    @Test
    @DisplayName("handle - Kiểm tra template path cho receiver")
    void handle_CheckReceiverTemplate() {
        // Given
        when(templateReader.read(eq("template/return-order-receiver.html"), anyMap()))
                .thenReturn("Receiver template");
        when(templateReader.read(eq("template/return-order-supplier.html"), anyMap()))
                .thenReturn("Supplier template");
        doNothing().when(mailService).send(anyString(), anyString(), anyString());

        // When
        mailListener.handle(event);

        // Then
        verify(templateReader).read(eq("template/return-order-receiver.html"), anyMap());
        verify(templateReader).read(eq("template/return-order-supplier.html"), anyMap());
    }

    /**
     * Test case: Kiểm tra tham số cho template
     */
    @Test
    @DisplayName("handle - Kiểm tra tham số template")
    void handle_CheckTemplateParams() {
        // Given
        when(templateReader.read(anyString(), anyMap()))
                .thenAnswer(invocation -> {
                    String path = invocation.getArgument(0);
                    Map<String, Object> params = invocation.getArgument(1);

                    if (path.contains("receiver")) {
                        assertTrue(params.containsKey("receiverName"));
                        assertTrue(params.containsKey("orderCode"));
                        assertTrue(params.containsKey("supplierName"));
                        assertTrue(params.containsKey("failedDeliveryCount"));
                    } else {
                        assertTrue(params.containsKey("supplierName"));
                        assertTrue(params.containsKey("orderCode"));
                        assertTrue(params.containsKey("receiverName"));
                        assertTrue(params.containsKey("failedDeliveryCount"));
                    }
                    return "content";
                });
        doNothing().when(mailService).send(anyString(), anyString(), anyString());

        // When
        mailListener.handle(event);

        // Then - Đã kiểm tra trong answer
        verify(templateReader, times(4)).read(anyString(), anyMap());
    }

    /**
     * Test case: Mail service bị lỗi khi gửi
     */
    @Test
    @DisplayName("handle - Mail service lỗi")
    void handle_MailServiceError() {
        // Given
        when(templateReader.read(anyString(), anyMap()))
                .thenReturn("Rendered template content");
        doThrow(new RuntimeException("Mail error"))
                .when(mailService).send(anyString(), anyString(), anyString());

        // When & Then - Exception được throw ra ngoài
        assertThrows(RuntimeException.class, () -> mailListener.handle(event));
    }
}
