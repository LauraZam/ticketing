package kz.kimep.ticketing.booking.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateHoldRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotEmpty(message = "At least one seat must be selected")
    private Set<Long> seatIds;
}