java_test(
    name = "MarriageAgentE2E",
    srcs = [
        "MarriageAgentE2E.java",
    ],
    test_class = "grakn.simulation.agents.test.MarriageAgentE2E",
    deps = [
        # External dependencies from @graknlabs
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//:simulation",
        "//agents:agents",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library",
    ],
)