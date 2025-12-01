package tech.ignitr.habitus.web.habit;

import lombok.Getter;
import tech.ignitr.habitus.data.habit.Frequency;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class HabitRequest {

    private UUID id;
    private UUID userId;
    private String tag;
    private Integer maxQuantity;
    private Frequency frequency;
    private Integer currentQuantity;
    private LocalDateTime check;
    private boolean done;

}
