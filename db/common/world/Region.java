package grakn.simulation.db.common.world;

public interface Region {
    String name();
    String tracker();
    World.Continent continent();
}
