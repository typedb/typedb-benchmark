package com.vaticle.typedb.benchmark.simulation.agent;

import com.vaticle.typedb.benchmark.common.concept.Country;
import com.vaticle.typedb.benchmark.common.concept.Gender;
import com.vaticle.typedb.benchmark.common.concept.Person;
import com.vaticle.typedb.benchmark.common.params.Context;
import com.vaticle.typedb.benchmark.common.seed.RandomSource;
import com.vaticle.typedb.benchmark.simulation.driver.Client;
import com.vaticle.typedb.benchmark.simulation.driver.Session;
import com.vaticle.typedb.benchmark.simulation.driver.Transaction;

import java.util.ArrayList;
import java.util.List;

public abstract class MutualFriendshipAgent<TX extends Transaction> extends Agent<Country, TX> {

    protected MutualFriendshipAgent(Client<?, TX> client, Context context) {
        super(client, context);
    }

    @Override
    protected Class<? extends Agent> agentClass() {
        return MutualFriendshipAgent.class;
    }

    @Override
    protected List<Country> regions() {
        return context.seedData().countries();
    }

    @Override
    protected List<Report> run(Session<TX> session, Country country, RandomSource random) {
        try (TX tx = session.readTransaction()) {
            matchMutualFriendsByGender(tx, country, Gender.FEMALE);
            if (context.isReporting()) {
                throw new RuntimeException("Reports are not available for read-only agents.");
            }
        }
        return new ArrayList<>();
    }

    protected abstract void matchMutualFriendsByGender(TX tx, Country country, Gender gender);
}
