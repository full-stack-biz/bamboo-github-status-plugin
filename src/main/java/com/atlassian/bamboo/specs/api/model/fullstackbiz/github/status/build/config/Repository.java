package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

public class Repository {
    private int id;
    private String name;
    private boolean enabled;

    public Repository() {}

    public Repository(int id, String name, Boolean enabled) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
    }

    public Repository(int id, String name) {
        this.id = id;
        this.name = name;
        this.enabled = false;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return name;
    }
}
