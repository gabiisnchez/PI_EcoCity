package com.ecocity.app;

public class Incident {
    private String title;
    private String description;
    private String urgency;

    public Incident(String title, String description, String urgency) {
        this.title = title;
        this.description = description;
        this.urgency = urgency;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}
