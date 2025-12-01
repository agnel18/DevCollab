package tech.ignitr.habitus.data.habit;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.ignitr.habitus.data.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitRepository extends JpaRepository <Habit, UUID>{
    Optional<List<Habit>> findAllByUser (User user);
    Habit getHabitById(UUID id);
    void deleteAllByUser(User user);

}
