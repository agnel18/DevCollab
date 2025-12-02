package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.TimeEntry;
import com.agnel.devcollab.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    // Get all time entries for the current user for a given week
    @GetMapping("/week")
    public List<TimeEntry> getWeekEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDateTime weekStart = start.atStartOfDay();
        LocalDateTime weekEnd = start.plusDays(7).atTime(LocalTime.MAX);
        // TODO: get userId from userDetails
        Long userId = 1L;
        return timeEntryRepository.findByUserIdAndStartBetween(userId, weekStart, weekEnd);
    }

    // Create a new time entry
    @PostMapping
    public TimeEntry create(@RequestBody TimeEntry entry, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: set user from userDetails
        return timeEntryRepository.save(entry);
    }

    // Update an existing time entry (resize, move, edit details)
    @PatchMapping("/{id}")
    public TimeEntry update(@PathVariable Long id, @RequestBody TimeEntry updated) {
        TimeEntry entry = timeEntryRepository.findById(id).orElseThrow();
        if (updated.getStart() != null) entry.setStart(updated.getStart());
        if (updated.getEnd() != null) entry.setEnd(updated.getEnd());
        if (updated.getDescription() != null) entry.setDescription(updated.getDescription());
        if (updated.getTags() != null) entry.setTags(updated.getTags());
        // ...other fields as needed
        return timeEntryRepository.save(entry);
    }

    // Delete a time entry
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        timeEntryRepository.deleteById(id);
    }
}
