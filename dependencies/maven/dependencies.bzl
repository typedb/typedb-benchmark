# Do not edit. bazel-deps autogenerates this file from dependencies/maven/dependencies.yaml.
def _jar_artifact_impl(ctx):
    jar_name = "%s.jar" % ctx.name
    ctx.download(
        output=ctx.path("jar/%s" % jar_name),
        url=ctx.attr.urls,
        sha256=ctx.attr.sha256,
        executable=False
    )
    src_name="%s-sources.jar" % ctx.name
    srcjar_attr=""
    has_sources = len(ctx.attr.src_urls) != 0
    if has_sources:
        ctx.download(
            output=ctx.path("jar/%s" % src_name),
            url=ctx.attr.src_urls,
            sha256=ctx.attr.src_sha256,
            executable=False
        )
        srcjar_attr ='\n    srcjar = ":%s",' % src_name

    build_file_contents = """
package(default_visibility = ['//visibility:public'])
java_import(
    name = 'jar',
    tags = ['maven_coordinates={artifact}'],
    jars = ['{jar_name}'],{srcjar_attr}
)
filegroup(
    name = 'file',
    srcs = [
        '{jar_name}',
        '{src_name}'
    ],
    visibility = ['//visibility:public']
)\n""".format(artifact = ctx.attr.artifact, jar_name = jar_name, src_name = src_name, srcjar_attr = srcjar_attr)
    ctx.file(ctx.path("jar/BUILD"), build_file_contents, False)
    return None

jar_artifact = repository_rule(
    attrs = {
        "artifact": attr.string(mandatory = True),
        "sha256": attr.string(mandatory = True),
        "urls": attr.string_list(mandatory = True),
        "src_sha256": attr.string(mandatory = False, default=""),
        "src_urls": attr.string_list(mandatory = False, default=[]),
    },
    implementation = _jar_artifact_impl
)

def jar_artifact_callback(hash):
    src_urls = []
    src_sha256 = ""
    source=hash.get("source", None)
    if source != None:
        src_urls = [source["url"]]
        src_sha256 = source["sha256"]
    jar_artifact(
        artifact = hash["artifact"],
        name = hash["name"],
        urls = [hash["url"]],
        sha256 = hash["sha256"],
        src_urls = src_urls,
        src_sha256 = src_sha256
    )
    native.bind(name = hash["bind"], actual = hash["actual"])


def list_dependencies():
    return [
    {"artifact": "ch.qos.logback:logback-classic:1.2.3", "lang": "java", "sha1": "7c4f3c474fb2c041d8028740440937705ebb473a", "sha256": "fb53f8539e7fcb8f093a56e138112056ec1dc809ebb020b59d8a36a5ebac37e0", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar", "source": {"sha1": "cfd5385e0c5ed1c8a5dce57d86e79cf357153a64", "sha256": "480cb5e99519271c9256716d4be1a27054047435ff72078d9deae5c6a19f63eb", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3-sources.jar"} , "name": "ch-qos-logback-logback-classic", "actual": "@ch-qos-logback-logback-classic//jar", "bind": "jar/ch/qos/logback/logback-classic"},
    {"artifact": "ch.qos.logback:logback-core:1.2.3", "lang": "java", "sha1": "864344400c3d4d92dfeb0a305dc87d953677c03c", "sha256": "5946d837fe6f960c02a53eda7a6926ecc3c758bbdd69aa453ee429f858217f22", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar", "source": {"sha1": "3ebabe69eba0196af9ad3a814f723fb720b9101e", "sha256": "1f69b6b638ec551d26b10feeade5a2b77abe347f9759da95022f0da9a63a9971", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3-sources.jar"} , "name": "ch-qos-logback-logback-core", "actual": "@ch-qos-logback-logback-core//jar", "bind": "jar/ch/qos/logback/logback-core"},
    {"artifact": "commons-cli:commons-cli:1.4", "lang": "java", "sha1": "c51c00206bb913cd8612b24abd9fa98ae89719b1", "sha256": "fd3c7c9545a9cdb2051d1f9155c4f76b1e4ac5a57304404a6eedb578ffba7328", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4.jar", "source": {"sha1": "40dfd9fdef125e19136135e68d54af6d9b0cfbb8", "sha256": "59fd9d6ca09ade4f27bddd274fb842ea48fd92118a755d0a64cf60413cd1c3fc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4-sources.jar"} , "name": "commons-cli-commons-cli", "actual": "@commons-cli-commons-cli//jar", "bind": "jar/commons-cli/commons-cli"},
    {"artifact": "io.netty:netty-tcnative-boringssl-static:2.0.25.Final", "lang": "java", "sha1": "185980556f9f083b5339825f19c9641c6c879417", "sha256": "96d9c14ab4c47cbad7fec9bdb083917db971d3754d6c7fa89f958bc719e230ed", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/io/netty/netty-tcnative-boringssl-static/2.0.25.Final/netty-tcnative-boringssl-static-2.0.25.Final.jar", "source": {"sha1": "a726743223aa538b81c316e0ae37743f3f33ea7f", "sha256": "dc44ab2ce5726ff14bedd4a12d16fd0eb1db45b04692d37af9ab0a3a3c295ce8", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/io/netty/netty-tcnative-boringssl-static/2.0.25.Final/netty-tcnative-boringssl-static-2.0.25.Final-sources.jar"} , "name": "io-netty-netty-tcnative-boringssl-static", "actual": "@io-netty-netty-tcnative-boringssl-static//jar", "bind": "jar/io/netty/netty-tcnative-boringssl-static"},
    {"artifact": "org.apache.commons:commons-csv:1.7", "lang": "java", "sha1": "cb5d05520f8fe1b409aaf29962e47dc5764f8f39", "sha256": "25f5e7914729a3cb9cbb83918b5f1116625cca63ce38a50f0fe596f837b9a524", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-csv/1.7/commons-csv-1.7.jar", "source": {"sha1": "ec726da54e382e22892af13bd99b52fc412702ee", "sha256": "c1cff0fdf9a690f23880dbe305fe971567abe54cb716a8bb1c9c9a2b2cfc9bdd", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-csv/1.7/commons-csv-1.7-sources.jar"} , "name": "org-apache-commons-commons-csv", "actual": "@org-apache-commons-commons-csv//jar", "bind": "jar/org/apache/commons/commons-csv"},
    {"artifact": "org.apache.commons:commons-lang3:3.9", "lang": "java", "sha1": "0122c7cee69b53ed4a7681c03d4ee4c0e2765da5", "sha256": "de2e1dcdcf3ef917a8ce858661a06726a9a944f28e33ad7f9e08bea44dc3c230", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.jar", "source": {"sha1": "8f1cb192e229bc4cd1c900c51171d96706e6d195", "sha256": "d97341ce0a7554028db3403e407bb51f4d902bf3287f64f709d7a8156eaf1910", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9-sources.jar"} , "name": "org-apache-commons-commons-lang3", "actual": "@org-apache-commons-commons-lang3//jar", "bind": "jar/org/apache/commons/commons-lang3"},
    {"artifact": "org.hamcrest:hamcrest:2.2", "lang": "java", "sha1": "1820c0968dba3a11a1b30669bb1f01978a91dedc", "sha256": "5e62846a89f05cd78cd9c1a553f340d002458380c320455dd1f8fc5497a8a1c1", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar", "source": {"sha1": "a0a13cfc629420efb587d954f982c4c6a100da25", "sha256": "f49e697dbc70591f91a90dd7f741f5780f53f63f34a416d6a9879499d4d666af", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/hamcrest/hamcrest/2.2/hamcrest-2.2-sources.jar"} , "name": "org-hamcrest-hamcrest", "actual": "@org-hamcrest-hamcrest//jar", "bind": "jar/org/hamcrest/hamcrest"},
    {"artifact": "org.slf4j:slf4j-api:1.7.28", "lang": "java", "sha1": "2cd9b264f76e3d087ee21bfc99305928e1bdb443", "sha256": "fb6e4f67a2a4689e3e713584db17a5d1090c1ebe6eec30e9e0349a6ee118141e", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28.jar", "source": {"sha1": "6444f3c8fce32e20f621e264807256c5e65f11c9", "sha256": "b1b8bfa4f2709684606001685d09ef905adc1b72ec53444ade90f44bfbcebcff", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28-sources.jar"} , "name": "org-slf4j-slf4j-api", "actual": "@org-slf4j-slf4j-api//jar", "bind": "jar/org/slf4j/slf4j-api"},
    {"artifact": "org.yaml:snakeyaml:1.25", "lang": "java", "sha1": "8b6e01ef661d8378ae6dd7b511a7f2a33fae1421", "sha256": "b50ef33187e7dc922b26dbe4dd0fdb3a9cf349e75a08b95269901548eee546eb", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/1.25/snakeyaml-1.25.jar", "source": {"sha1": "045379604b7a254120101009ba09537737daed6f", "sha256": "fcc58a580b640e80f6c71acdd20b00dd87b80c9944dcad0389a904b77f04c88d", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/1.25/snakeyaml-1.25-sources.jar"} , "name": "org-yaml-snakeyaml", "actual": "@org-yaml-snakeyaml//jar", "bind": "jar/org/yaml/snakeyaml"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
