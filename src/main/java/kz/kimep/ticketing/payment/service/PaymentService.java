package kz.kimep.ticketing.payment.service;

import kz.kimep.ticketing.booking.domain.Booking;
import kz.kimep.ticketing.payment.domain.Payment;
import kz.kimep.ticketing.payment.domain.PaymentStatus;
import kz.kimep.ticketing.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int REF_START = 0;
    private static final int REF_END = 8;

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPayment(Booking booking, String method, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString()
                .substring(REF_START, REF_END).toUpperCase());
        payment.setPaidAt(Instant.now());
        return paymentRepository.save(payment);
    }
}