package com.calvi.model;

import java.util.UUID;

public class DailyTask {
    private UUID id;
    private String title;
    private boolean done = false; // odhaczone "dzisiaj" - resetowane raz dziennie, patrz Main.start()

    public DailyTask() {
        // potrzebne Jacksonowi przy wczytywaniu z JSON-a
    }

    public DailyTask(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
