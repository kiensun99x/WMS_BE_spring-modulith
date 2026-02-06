package com.rk.WMS.batch.service.mail;

import com.rk.WMS.batch.event.ReturnOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnOrderMailListener {

    private final MailService mailService;

    @EventListener
    public void handle(ReturnOrderEvent event) {

        sendMailToReceiver(event);
        sendMailToSupplier(event);
    }

    private void sendMailToReceiver(ReturnOrderEvent event) {

        String subject = "Thông báo hoàn trả đơn hàng " + event.getOrderCode();

        String content = String.format("""
                Xin chào %s,

                Bạn có đơn hàng từ

                Mã đơn: %s
                Người gửi: %s

                Do đã quá %d lần thực hiện giao hàng không thành công.
                Đơn hàng của bạn sẽ được hoàn trả về người gửi hàng.

                Trân trọng cảm ơn.
                """,
                event.getReceiverName(),
                event.getOrderCode(),
                event.getSupplierName(),
                event.getFailedDeliveryCount()
        );

        mailService.send(
                event.getReceiverEmail(),
                subject,
                content
        );
    }

    private void sendMailToSupplier(ReturnOrderEvent event) {

        String subject = "Thông báo hoàn trả đơn hàng " + event.getOrderCode();

        String content = String.format("""
                Xin chào %s,

                Bạn có đơn hàng

                Mã đơn: %s
                Người nhận: %s

                Do đã quá %d lần thực hiện giao hàng không thành công.
                Đơn hàng của bạn sẽ được hoàn trả về người gửi hàng.

                Trân trọng cảm ơn.
                """,
                event.getSupplierName(),
                event.getOrderCode(),
                event.getReceiverName(),
                event.getFailedDeliveryCount()
        );

        mailService.send(
                event.getSupplierEmail(),
                subject,
                content
        );
    }
}