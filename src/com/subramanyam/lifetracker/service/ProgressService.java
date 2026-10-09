
package com.subramanyam.lifetracker.service;

import com.subramanyam.lifetracker.model.DailyProgress;
import com.subramanyam.lifetracker.model.Task;
import com.subramanyam.lifetracker.model.TaskStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgressService {

    private static final Path SAVE_FILE = Path.of(
            System.getProperty("user.home"),
            ".life-progress-tracker",
            "progress-data.ser"
    );

    private static final String[] DEFAULT_TASK_NAMES = {
            "Gym",
            "AI Platform / Project",
            "DSA",
            "Client Editing",
            "GT Jersey",
            "RGB Lights"
    };

    private final Map<LocalDate, DailyProgress> progressByDate =
            new HashMap<>();

    public ProgressService() {
        load();
    }

    /**
     * Returns saved progress for a date.
     *
     * For a new date, carries forward the latest saved
     * tasks and their statuses.
     */
    public DailyProgress getProgressFor(LocalDate date) {

        DailyProgress existing = progressByDate.get(date);

        if (existing != null) {
            return existing;
        }

        LocalDate latestDate = progressByDate.keySet()
                .stream()
                .max(LocalDate::compareTo)
                .orElse(null);

        if (latestDate != null) {

            DailyProgress latestProgress =
                    progressByDate.get(latestDate);

            List<Task> tasks = new ArrayList<>();

            for (Task oldTask : latestProgress.getTasks()) {

                Task newTask = new Task(oldTask.getName());

                newTask.setStatus(oldTask.getStatus());

                tasks.add(newTask);
            }

            DailyProgress newDay = new DailyProgress(date, tasks);

            progressByDate.put(date, newDay);
            save();

            return newDay;
        }

        DailyProgress firstDay = createFreshDay(date);

        progressByDate.put(date, firstDay);
        save();

        return firstDay;
    }

    public DailyProgress getExistingProgress(LocalDate date) {
        return progressByDate.get(date);
    }

    /**
     * Saves all daily progress without deleting historical data.
     */
    public void save() {

        try {
            Files.createDirectories(SAVE_FILE.getParent());

            try (ObjectOutputStream output =
                         new ObjectOutputStream(
                                 Files.newOutputStream(SAVE_FILE))) {

                output.writeObject(progressByDate);
            }

        } catch (IOException exception) {
            System.err.println(
                    "Could not save Life Progress data: "
                            + exception.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {

        if (!Files.exists(SAVE_FILE)) {
            return;
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             Files.newInputStream(SAVE_FILE))) {

            Object savedData = input.readObject();

            if (savedData instanceof Map<?, ?>) {
                progressByDate.putAll(
                        (Map<LocalDate, DailyProgress>) savedData
                );
            }

        } catch (
                IOException |
                ClassNotFoundException |
                ClassCastException exception
        ) {
            System.err.println(
                    "Could not load Life Progress data: "
                            + exception.getMessage()
            );
        }
    }

    /**
     * Monthly progress uses the latest saved snapshot
     * within the requested month.
     *
     * This prevents carried-forward tasks from being
     * counted again for every day.
     */
    private List<Task> getMonthlyTasks(YearMonth month) {

        return progressByDate.entrySet()
                .stream()
                .filter(entry ->
                        YearMonth.from(entry.getKey()).equals(month)
                )
                .max(Map.Entry.comparingByKey())
                .map(entry ->
                        new ArrayList<>(entry.getValue().getTasks())
                )
                .orElseGet(ArrayList::new);
    }

    /**
     * Counts each task in the latest monthly snapshot once.
     */
    public int getMonthlyTaskCount(
            YearMonth month,
            TaskStatus status
    ) {

        return (int) getMonthlyTasks(month)
                .stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    /**
     * Returns the number of tasks in the latest monthly snapshot.
     */
    public int getMonthlyTaskTotal(YearMonth month) {
        return getMonthlyTasks(month).size();
    }

    /**
     * Returns 1 if this goal's latest monthly status matches
     * the requested status; otherwise returns 0.
     */
    public int getMonthlyTaskCountForGoal(
            YearMonth month,
            String goalName,
            TaskStatus status
    ) {

        return (int) getMonthlyTasks(month)
                .stream()
                .filter(task -> task.getName().equals(goalName))
                .filter(task -> task.getStatus() == status)
                .limit(1)
                .count();
    }

    /**
     * Each goal name contributes at most one to the breakdown.
     */
    public int getMonthlyTaskTotalForGoal(
            YearMonth month,
            String goalName
    ) {

        return getMonthlyTasks(month)
                .stream()
                .anyMatch(task -> task.getName().equals(goalName))
                ? 1 : 0;
    }

    /**
     * Returns unique goal names from the latest monthly snapshot.
     */
    public Set<String> getMonthlyGoalNames(YearMonth month) {

        Set<String> names = new LinkedHashSet<>();

        getMonthlyTasks(month).forEach(
                task -> names.add(task.getName())
        );

        return names;
    }

    /**
     * Creates the first day with the default tasks.
     */
    private DailyProgress createFreshDay(LocalDate date) {

        List<Task> tasks = new ArrayList<>();

        for (String name : DEFAULT_TASK_NAMES) {
            tasks.add(new Task(name));
        }

        return new DailyProgress(date, tasks);
    }
}
