package grakn.simulation.db.common.world;

public interface Region {
    String name();
    String tracker();
    String topLevelName(); // Used to assign session keys for Grakn model
}
