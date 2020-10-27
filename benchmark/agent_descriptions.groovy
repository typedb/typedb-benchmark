package grakn.simulation.benchmark

static LinkedHashMap<String, String> descriptions() {
    def desc = [:]

    desc."PersonBirth" = """
Adds people to the world simulation. This involves adding a single entity with a large number of attributes attached.
"""
    desc."Employment" = """
Finds existing people and makes them employees of companies.
"""
    return desc
}

