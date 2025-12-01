package tech.ignitr.habitus.web.habit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.ignitr.habitus.data.habit.Habit;
import tech.ignitr.habitus.service.habit.HabitService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService service;

    /**
     * API call for creating a new habit (HabitEntity)
     * @param requestBody - all of HabitEntity params
     * @return ResponseEntity containing the status code from service method and the created Habit
     */
    @PostMapping("/")
    public ResponseEntity <Habit> postHabit(@RequestBody HabitRequest requestBody){
        return service.postHabit(requestBody);
    }

    /**
     * API call for getting all habits (HabitEntity) by userID
     * @param userId - the user ID to be selected by
     * @return ResponseEntity containing the status code from service method and a list of Habits
     */
    @GetMapping("/")
    public ResponseEntity <List<Habit>> getAllHabit(@RequestParam("userId") UUID userId){
        return service.getHabits(userId);
    }

    /**
     * API call for updating habit (HabitEntity)
     * @param id - id of the HabitEntry to add
     * @param requestBody - all Habit params
     * @return ResponseEntity containing the status code from service method
     */
    @PutMapping("/")
    public ResponseEntity <Habit> putHabit(@RequestBody HabitRequest requestBody){
        return service.putHabit(requestBody);
    }

    /**
     * API call for deleting a habit (HabitEntity)
     * @param id - id of the habit that should be deleted
     * @return ResponseEntity containing the status code from service method
     */
    @DeleteMapping("/")
    public ResponseEntity <Void> deleteHabit (@RequestParam("id") UUID id){
        return service.deleteHabit(id);
    }

    /**
     * API call for deleting all habits (HabitEntity)
     * @param userId - id of the user all habits should be deleted from
     * @return ResponseEntity containing the status code from service method
     */
    @DeleteMapping("/bulk/")
    public ResponseEntity <Void> deleteAllHabits (@RequestParam("userId") UUID userId){
        return service.deleteAllHabits(userId);
    }
}
