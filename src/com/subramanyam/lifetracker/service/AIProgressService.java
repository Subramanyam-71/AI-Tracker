package com.subramanyam.lifetracker.service;

import com.subramanyam.lifetracker.model.DailyProgress;
import com.subramanyam.lifetracker.model.Task;
import com.subramanyam.lifetracker.model.TaskStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Stage 7: A very basic "AI-style" daily progress analysis.
 *
 * This is intentionally simple, rule-based logic — no real AI model
 * is called yet. Everything happens in ONE method, generateAnalysis(),
 * so later you can swap its body for a real call to an AI API (like
 * the Anthropic or OpenAI API) without touching any other class in
 * the app — TaskTrackerPanel only ever calls this one method and
 * displays whatever String comes back.
 */
public class AIProgressService {

    public String generateAnalysis(DailyProgress progress) {
        List<Task> tasks = progress.getTasks();

        List<String> completedNames = namesWithStatus(tasks, TaskStatus.COMPLETED);
        List<String> notStartedNames = namesWithStatus(tasks, TaskStatus.NOT_STARTED);
        List<String> inProgressNames = namesWithStatus(tasks, TaskStatus.IN_PROGRESS);

        // Suggest something not started yet first; if everything has at
        // least been started, suggest finishing something in progress.
        String suggestion = !notStartedNames.isEmpty() ? notStartedNames.get(0)
                : !inProgressNames.isEmpty() ? inProgressNames.get(0)
                : null;

        if (completedNames.isEmpty()) {
            if (suggestion == null) {
                return "No tasks tracked yet today. Add one above and get started!";
            }
            return "Nothing completed yet today. Try starting with " + suggestion
                    + " — even a small step counts.";
        }

        StringBuilder message = new StringBuilder("You had a productive day. You completed ");
        message.append(joinNaturally(completedNames)).append(".");

        if (suggestion != null) {
            message.append(" Tomorrow, try to spend some time on ").append(suggestion).append(".");
        } else {
            message.append(" Every task is done — great work!");
        }

        return message.toString();
    }

    private List<String> namesWithStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .map(Task::getName)
                .collect(Collectors.toList());
    }

    /** Joins a list like ["Gym", "DSA"] into "Gym and DSA", or "Gym, DSA and AI Project" for 3+. */
    private String joinNaturally(List<String> items) {
        if (items.size() == 1) {
            return items.get(0);
        }
        String allButLast = String.join(", ", items.subList(0, items.size() - 1));
        return allButLast + " and " + items.get(items.size() - 1);
    }
}