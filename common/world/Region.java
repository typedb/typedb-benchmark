package grakn.simulation.common.world;

public interface Region {
    String name();
    String tracker();
    String topLevelName(); // Used to assign session keys for Grakn model
}
