java_test(
    name = "QueryCountE2E",
    srcs = [
        "QueryCountE2E.java",
        "common/TestArgsInterpreter.java",
    ],
    test_class = "grakn.simulation.agents.test.QueryCountE2E",
    deps = [
        # External dependencies from @graknlabs
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest",
    ],
    size="large"
)