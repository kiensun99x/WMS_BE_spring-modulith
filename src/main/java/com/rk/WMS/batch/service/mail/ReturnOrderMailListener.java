package com.rk.WMS.batch.service.mail;

import com.rk.WMS.batch.event.OrdersReturnedEvent;
import com.rk.WMS.batch.event.ReturnOrderPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j(topic = "ORDER-RETURNED-MAIL")
@Component
@RequiredArgsConstructor
public class ReturnOrderMailListener {

    private final MailService mailService;
    private final MailTemplateReader templateReader;

    /**
     * Handle OrdersReturnedEvent sau khi transaction commit.
     *
     * Sử dụng AFTER_COMMIT để đảm bảo:
     * - Trạng thái order đã được cập nhật trong DB
     * - Không gửi mail cho dữ liệu bị rollback
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrdersReturnedEvent event) {

        log.info(
                "[MAIL][RETURN] Sending mails for {} returned orders",
                event.getOrders().size()
        );

        // Lần lượt gửi mail cho từng đơn hàng trong event
        for (ReturnOrderPayload payload : event.getOrders()) {
            sendMailToReceiver(payload);
            sendMailToSupplier(payload);
        }
    }

    /**
     * Gửi mail thông báo hoàn trả đơn hàng cho người nhận (receiver).
     *
     * @param payload dữ liệu đơn hàng đã được đóng gói trong event
     */
    private void sendMailToReceiver(ReturnOrderPayload payload) {

        String subject = "Thông báo hoàn trả đơn hàng " + payload.getOrderCode();

        String content = templateReader.read(
                "template/return-order-receiver.html",
                Map.of(
                        "receiverName", payload.getReceiverName(),
                        "orderCode", payload.getOrderCode(),
                        "supplierName", payload.getSupplierName(),
                        "failedDeliveryCount", payload.getFailedDeliveryCount()
                )
        );

        mailService.send(
                payload.getReceiverEmail(),
                subject,
                content
        );
    }

    /**
     * Gửi mail thông báo hoàn trả đơn hàng cho nhà cung cấp (supplier).
     *
     * @param payload dữ liệu đơn hàng đã được đóng gói trong event
     */
    private void sendMailToSupplier(ReturnOrderPayload payload) {

        String subject = "Thông báo hoàn trả đơn hàng " + payload.getOrderCode();

        String content = templateReader.read(
                "template/return-order-supplier.html",
                Map.of(
                        "supplierName", payload.getSupplierName(),
                        "orderCode", payload.getOrderCode(),
                        "receiverName", payload.getReceiverName(),
                        "failedDeliveryCount", payload.getFailedDeliveryCount()
                )
        );

        mailService.send(
                payload.getSupplierEmail(),
                subject,
                content
        );
    }
}