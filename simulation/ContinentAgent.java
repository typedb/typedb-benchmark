package grakn.simulation;

import java.util.List;

public abstract class ContinentAgent extends ParallelAgent<Continent> {

    @Override
    List<Continent> getParallelList() {
        return Continent.ALL;
    }
}
