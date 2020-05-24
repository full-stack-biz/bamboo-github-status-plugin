package tools.fullstackbiz.bamboo.github.status.build.config;

public class Repository {
    private int id;
    private String name;
    private boolean enabled;

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

    public boolean getEnabled() { return this.enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return name;
    }
}
