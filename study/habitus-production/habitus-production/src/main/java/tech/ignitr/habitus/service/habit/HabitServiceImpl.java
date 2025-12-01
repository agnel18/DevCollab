package tech.ignitr.habitus.service.habit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tech.ignitr.habitus.configuration.DatabaseException;
import tech.ignitr.habitus.data.habit.Habit;
import tech.ignitr.habitus.data.habit.HabitRepository;
import tech.ignitr.habitus.data.user.User;
import tech.ignitr.habitus.data.user.UserRepository;
import tech.ignitr.habitus.web.habit.HabitRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService{

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    //services for habit API endpoints
    /**
     * saving a new habit to database
     * @param requestBody - all of HabitEntity params
     * @return HabitStatusReturn - combination of new Entity and status code
     */
    @Override
    public ResponseEntity<Habit> postHabit(HabitRequest requestBody){
        Habit newHabit = Habit.builder()
                .id(UUID.randomUUID())
                .user(new User())
                .tag(requestBody.getTag())
                .frequency(requestBody.getFrequency())
                .currentQuantity(0)
                .maxQuantity(requestBody.getMaxQuantity())
                .done(false)
                .build();
        habitRepository.saveAndFlush(newHabit);
        return ResponseEntity.ok(newHabit);
    }

    /**
     * getting a list of all habits by UserID
     * @param userId - the user ID to be selected by
     * @return ResponseEntity with either list of habits and ok code or empty list and not found code
     */
    @Override
    public ResponseEntity<List<Habit>> getHabits(UUID userId) {
        try{
            checkHabits(userId);
            return ResponseEntity.ok(habitRepository.findAllByUser(userRepository
                            .findById(userId).orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NOT_FOUND))
                    ).orElseThrow(()-> new DatabaseException("data not found", HttpStatus.NO_CONTENT)));
        }catch (DatabaseException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * updating a HabitEntity in the database
     * @param requestBody - all of HabitEntity params
     * @return empty response with http status code
     */
    @Override
    public ResponseEntity<Habit> putHabit(HabitRequest requestBody) {
        try{
            return ResponseEntity.ok(updateHabit(requestBody));
        }catch(DatabaseException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * delete a habit (HabitEntity)
     * @param id - id of the habit that should be deleted
     * @return hempty response with http status code
     */
    @Override
    public ResponseEntity<Void> deleteHabit(UUID id) {
        try{
            habitRepository.delete(habitRepository.findById(id)
                    .orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NOT_FOUND)));
            habitRepository.flush();
            return ResponseEntity.ok().build();
        }catch(DatabaseException e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * deleting all habit (HabitEntity)
     * @param userId - id of the habit that should be deleted
     * @return empty response with http status code
     */
    @Override
    public ResponseEntity<Void> deleteAllHabits(UUID userId) {
        try{
            habitRepository.deleteAllByUser(userRepository.findById(userId)
                    .orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NOT_FOUND)));
            habitRepository.flush();
            return ResponseEntity.ok().build();
        }catch(DatabaseException e){
            return ResponseEntity.notFound().build();
        }
    }

    private Habit updateHabit(HabitRequest requestBody) throws DatabaseException {
        Habit updatable = habitRepository.findById(requestBody.getId())
                .orElseThrow(()-> new DatabaseException("user not found", HttpStatus.NO_CONTENT));
        return new Habit();
    }

    /**
     *
     * @param userId to search for all habits to check/update
     * @throws DatabaseException if user or data was not found
     */
    private void checkHabits(UUID userId) throws DatabaseException {
        List<Habit> list = habitRepository.findAllByUser(userRepository.findById(userId)
                        .orElseThrow(() -> new DatabaseException("user not found", HttpStatus.NOT_FOUND)))
                .orElseThrow(() -> new DatabaseException("data not found", HttpStatus.NO_CONTENT));

        for (Habit habit : list) {
            if(habit.getCurrentQuantity() >= habit.getMaxQuantity()){
                habit.setDone(true);
                habit.setDate_done(
                        LocalDate.now());
            }else{
                switch (habit.getFrequency()) {
                    case DAILY -> {
                        if (habit.getDate_done().plusDays(1).isAfter(LocalDate.now())) {
                            habit.setDone(false);
                            habit.setCurrentQuantity(0);
                            habit.setDate_done(null);
                        }
                    }
                    case WEEKLY -> {
                        if (habit.getDate_done().plusWeeks(1).isAfter(LocalDate.now())) {
                            habit.setDone(false);
                            habit.setCurrentQuantity(0);
                            habit.setDate_done(null);
                        }
                    }
                    case BIWEEKLY -> {
                        if (habit.getDate_done().plusWeeks(2).isAfter(LocalDate.now())) {
                            habit.setDone(false);
                            habit.setCurrentQuantity(0);
                            habit.setDate_done(null);
                        }
                    }
                    case TRIWEEKLY -> {
                        if (habit.getDate_done().plusWeeks(3).isAfter(LocalDate.now())) {
                            habit.setDone(false);
                            habit.setCurrentQuantity(0);
                            habit.setDate_done(null);
                        }
                    }
                    case MONTHLY -> {
                        if (habit.getDate_done().plusMonths(1).isAfter(LocalDate.now())) {
                            habit.setDone(false);
                            habit.setCurrentQuantity(0);
                            habit.setDate_done(null);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        habitRepository.saveAllAndFlush(list);
    }
}
