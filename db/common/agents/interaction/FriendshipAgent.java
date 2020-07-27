package grakn.simulation.db.common.agents.interaction;

import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDateTime;
import java.util.List;

public abstract class FriendshipAgent extends CityAgent {

    @Override
    public final void iterate() {

        List<String> residentEmails = getResidentEmails(today());
        closeTx();  // TODO Closing and reopening the transaction here is a workaround for https://github.com/graknlabs/grakn/issues/5585
        if (residentEmails.size() > 0) {
            shuffle(residentEmails);
            int numFriendships = world().getScaleFactor();
            for (int i = 0; i < numFriendships; i++) {

                String friend1 = pickOne(residentEmails);
                String friend2 = pickOne(residentEmails);

                insertFriendship(friend1, friend2);
            }
            tx().commitWithTracing();
        }
    }

    protected abstract List<String> getResidentEmails(LocalDateTime earliestDate);

    protected abstract void insertFriendship(String friend1Email, String friend2Email);
}
