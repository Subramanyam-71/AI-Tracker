package com.subramanyam.lifetracker.model;

import java.io.Serializable;

/**
 * A single trackable item, like "Gym" or "DSA", plus its current status.
 * Every task starts as NOT_STARTED until you click it.
 */
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    private TaskStatus status;

    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.NOT_STARTED;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
    this.name = name;
}

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
