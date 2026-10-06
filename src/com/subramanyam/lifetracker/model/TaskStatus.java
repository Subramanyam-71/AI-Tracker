package com.subramanyam.lifetracker.model;

/**
 * The 3 possible states a task can be in for a given day.
 * Kept as an enum (fixed set of options) rather than a String so we
 * can't accidentally misspell a status somewhere in the code.
 */
public enum TaskStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Clicking a task cycles it forward: Not Started -> In Progress -> Completed -> Not Started. */
    public TaskStatus next() {
        TaskStatus[] all = values();
        int nextIndex = (this.ordinal() + 1) % all.length;
        return all[nextIndex];
    }
}