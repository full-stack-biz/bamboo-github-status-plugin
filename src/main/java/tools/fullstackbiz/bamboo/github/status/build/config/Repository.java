package tools.fullstackbiz.bamboo.github.status.build.config;

public class Repository {
    private int id;
    private String name;

    public Repository(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return name;
    }
}
