package com.subramanyam.lifetracker.model;

import java.time.LocalDate;
import java.io.Serializable;
import java.util.List;

/**
 * Everything tracked for a single calendar day: the date, the list of
 * tasks (Gym, DSA, etc.) with their statuses, and a short free-text note.
 *
 * Stage 3 note: this only lives in memory right now — closing the app
 * loses it. Saving it to disk is Stage 5.
 */
public class DailyProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final List<Task> tasks;
    private String note = "";

    public DailyProgress(LocalDate date, List<Task> tasks) {
        this.date = date;
        this.tasks = tasks;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
