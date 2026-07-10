package com.calvi.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Note {
    private UUID id;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public Note(String title, String content){
        this.id = UUID.randomUUID();
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Note\n" +
                "id = " + id + "\n" +
                "title = " + title + "\n" +
                "content = " + content + "\n" +
                "createdAt = " + createdAt + "\n"
                ;
    }
}
