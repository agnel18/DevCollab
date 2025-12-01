package tech.ignitr.habitus.data.habit;

import jakarta.persistence.*;
import lombok.*;
import tech.ignitr.habitus.data.user.User;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name="habits")
@NoArgsConstructor
@AllArgsConstructor
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    @Setter(AccessLevel.PROTECTED)
    private UUID id;

    @ManyToOne
    @Setter(AccessLevel.PROTECTED)
    private User user;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false, name="max_quantity")
    private Integer maxQuantity;

    @Column(nullable = false)
    @Enumerated(value=EnumType.STRING)
    private Frequency frequency;

    @Column(nullable = false, name="current_quantity")
    private Integer currentQuantity;

    @Column(name="date_done")
    @Temporal(TemporalType.DATE)
    private LocalDate date_done;

    @Column(nullable = false)
    private boolean done;

}
