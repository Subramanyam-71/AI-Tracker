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
     * Returns progress for the requested date.
     *
     * If the date already exists, return its saved data.
     *
     * If it is a new date, copy the latest saved day's
     * tasks AND their statuses.
     */
    public DailyProgress getProgressFor(LocalDate date) {

        DailyProgress existing = progressByDate.get(date);

        if (existing != null) {
            return existing;
        }

        // Find the latest saved date.
        LocalDate latestDate = progressByDate.keySet()
                .stream()
                .max(LocalDate::compareTo)
                .orElse(null);

        // If previous data exists, copy it.
        if (latestDate != null) {

            DailyProgress latestProgress =
                    progressByDate.get(latestDate);

            List<Task> tasks = new ArrayList<>();

            for (Task oldTask : latestProgress.getTasks()) {

                Task newTask =
                        new Task(oldTask.getName());

                // Keep the same status.
                newTask.setStatus(
                        oldTask.getStatus()
                );

                tasks.add(newTask);
            }

            DailyProgress newDay =
                    new DailyProgress(date, tasks);

            progressByDate.put(date, newDay);

            save();

            return newDay;
        }

        // First day of the application.
        DailyProgress firstDay =
                createFreshDay(date);

        progressByDate.put(date, firstDay);

        save();

        return firstDay;
    }

    /**
     * Returns existing progress for a date.
     */
    public DailyProgress getExistingProgress(LocalDate date) {
        return progressByDate.get(date);
    }

    /**
     * Saves all progress data.
     */
    public void save() {

        try {

            Files.createDirectories(
                    SAVE_FILE.getParent()
            );

            try (ObjectOutputStream output =
                         new ObjectOutputStream(
                                 Files.newOutputStream(SAVE_FILE)
                         )) {

                output.writeObject(progressByDate);
            }

        } catch (IOException exception) {

            System.err.println(
                    "Could not save Life Progress data: "
                            + exception.getMessage()
            );
        }
    }

    /**
     * Loads saved progress.
     */
    @SuppressWarnings("unchecked")
    private void load() {

        if (!Files.exists(SAVE_FILE)) {
            return;
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             Files.newInputStream(SAVE_FILE)
                     )) {

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
     * Counts tasks by status for the month.
     *
     * This keeps the original monthly total behavior.
     */
    public int getMonthlyTaskCount(
            YearMonth month,
            TaskStatus status
    ) {

        return getMonthlyTasks(month)
                .stream()
                .mapToInt(task ->
                        task.getStatus() == status
                                ? 1
                                : 0
                )
                .sum();
    }

    /**
     * Returns total task records for the month.
     */
    public int getMonthlyTaskTotal(
            YearMonth month
    ) {

        return getMonthlyTasks(month).size();
    }

    /**
     * Returns 1 if the latest saved status of this goal
     * matches the requested status, otherwise 0.
     *
     * This prevents:
     *
     * Today   -> Completed
     * Tomorrow -> Completed
     *
     * from being counted as 2/2.
     */
    public int getMonthlyTaskCountForGoal(
            YearMonth month,
            String goalName,
            TaskStatus status
    ) {

        Task latestTask = progressByDate.entrySet()
                .stream()

                .filter(entry ->
                        YearMonth.from(entry.getKey())
                                .equals(month)
                )

                .sorted(
                        Map.Entry
                                .<LocalDate, DailyProgress>
                                comparingByKey()
                                .reversed()
                )

                .flatMap(entry ->
                        entry.getValue()
                                .getTasks()
                                .stream()
                )

                .filter(task ->
                        task.getName()
                                .equals(goalName)
                )

                .findFirst()
                .orElse(null);

        if (latestTask == null) {
            return 0;
        }

        return latestTask.getStatus() == status
                ? 1
                : 0;
    }

    /**
     * Returns 1 when this goal exists in the month.
     *
     * It does not count the same carried-forward goal
     * multiple times.
     */
    public int getMonthlyTaskTotalForGoal(
            YearMonth month,
            String goalName
    ) {

        boolean exists =
                progressByDate.entrySet()
                        .stream()

                        .filter(entry ->
                                YearMonth.from(entry.getKey())
                                        .equals(month)
                        )

                        .flatMap(entry ->
                                entry.getValue()
                                        .getTasks()
                                        .stream()
                        )

                        .anyMatch(task ->
                                task.getName()
                                        .equals(goalName)
                        );

        return exists ? 1 : 0;
    }

    /**
     * Returns goal names that were tracked during the month.
     */
    public Set<String> getMonthlyGoalNames(
            YearMonth month
    ) {

        Set<String> names =
                new LinkedHashSet<>();

        getMonthlyTasks(month)
                .forEach(task ->
                        names.add(task.getName())
                );

        return names;
    }

    /**
     * Gets all task records stored for the month.
     */
    private List<Task> getMonthlyTasks(
            YearMonth month
    ) {

        return progressByDate
                .entrySet()
                .stream()

                .filter(entry ->
                        YearMonth.from(entry.getKey())
                                .equals(month)
                )

                .flatMap(entry ->
                        entry.getValue()
                                .getTasks()
                                .stream()
                )

                .toList();
    }

    /**
     * Creates the first day with the default tasks.
     */
    private DailyProgress createFreshDay(
            LocalDate date
    ) {

        List<Task> tasks =
                new ArrayList<>();

        for (String name : DEFAULT_TASK_NAMES) {

            tasks.add(
                    new Task(name)
            );
        }

        return new DailyProgress(
                date,
                tasks
        );
    }
}