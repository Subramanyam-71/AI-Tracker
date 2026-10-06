package com.subramanyam.lifetracker.service;

import com.subramanyam.lifetracker.model.DailyProgress;
import com.subramanyam.lifetracker.model.TaskStatus;

import java.time.LocalDate;

/** Supplies the daily quote and a short, personal progress reminder. */
public class MotivationService {

    private static final String[] QUOTES = {
            "Small progress every day becomes a big result.",
            "Finish what you started. Your future self will thank you.",
            "Discipline is choosing what you want most over what you want now.",
            "One focused hour today can change your whole week.",
            "You do not need to be perfect. You just need to keep moving.",
            "Your goals are built by the tasks you complete today.",
            "Start where you are. Use what you have. Do what you can."
    };

    public String getDailyQuote(LocalDate date) {
        return QUOTES[Math.floorMod(date.getDayOfYear(), QUOTES.length)];
    }

    public String getProgressReminder(ProgressService progressService, LocalDate date) {
        DailyProgress progress = progressService.getProgressFor(date);
        long completed = progress.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
        long inProgress = progress.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS).count();
        long notStarted = progress.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.NOT_STARTED).count();

        if (completed == progress.getTasks().size()) {
            return "Amazing work — every task is completed today!";
        }
        if (inProgress > 0) {
            return "You have " + inProgress + " task" + plural(inProgress)
                    + " in progress and " + notStarted + " not started. Keep going!";
        }
        return "You have " + notStarted + " task" + plural(notStarted)
                + " waiting. Pick one and begin now.";
    }

    private String plural(long count) {
        return count == 1 ? "" : "s";
    }
}
