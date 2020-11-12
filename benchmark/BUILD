load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

exports_files([
    "templates/agent_section.tex",
    "templates/agent_queries.tex",
    "templates/introduction.tex",
    "templates/agent_intro.tex",
    ])

load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_and_java_library")
load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_binary")

groovy_and_java_library(
    name = "benchmarklib",
    srcs = glob([
        "*.groovy",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//grakn",
        "//neo4j",
        "//common/agent",
    ],
    data = [
        "templates/agent_section.tex",
        "templates/agent_queries.tex",
        "templates/introduction.tex",
        "templates/agent_intro.tex",
    ]
)

groovy_binary(
    name = "benchmark",
    srcs = glob(["*.groovy"]),
    main_class = "grakn.simulation.benchmark.Report",
    deps = [":benchmarklib"],
)
