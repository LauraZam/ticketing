package kz.kimep.ticketing.booking.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookingRequest {

    @NotBlank(message = "Hold reference is required")
    private String holdReference;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}