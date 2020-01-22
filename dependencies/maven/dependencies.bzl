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
    {"artifact": "commons-cli:commons-cli:1.4", "lang": "java", "sha1": "c51c00206bb913cd8612b24abd9fa98ae89719b1", "sha256": "fd3c7c9545a9cdb2051d1f9155c4f76b1e4ac5a57304404a6eedb578ffba7328", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4.jar", "source": {"sha1": "40dfd9fdef125e19136135e68d54af6d9b0cfbb8", "sha256": "59fd9d6ca09ade4f27bddd274fb842ea48fd92118a755d0a64cf60413cd1c3fc", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.4/commons-cli-1.4-sources.jar"} , "name": "commons-cli-commons-cli", "actual": "@commons-cli-commons-cli//jar", "bind": "jar/commons-cli/commons-cli"},
    {"artifact": "org.apache.commons:commons-csv:1.7", "lang": "java", "sha1": "cb5d05520f8fe1b409aaf29962e47dc5764f8f39", "sha256": "25f5e7914729a3cb9cbb83918b5f1116625cca63ce38a50f0fe596f837b9a524", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-csv/1.7/commons-csv-1.7.jar", "source": {"sha1": "ec726da54e382e22892af13bd99b52fc412702ee", "sha256": "c1cff0fdf9a690f23880dbe305fe971567abe54cb716a8bb1c9c9a2b2cfc9bdd", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-csv/1.7/commons-csv-1.7-sources.jar"} , "name": "org-apache-commons-commons-csv", "actual": "@org-apache-commons-commons-csv//jar", "bind": "jar/org/apache/commons/commons-csv"},
    {"artifact": "org.apache.commons:commons-lang3:3.9", "lang": "java", "sha1": "0122c7cee69b53ed4a7681c03d4ee4c0e2765da5", "sha256": "de2e1dcdcf3ef917a8ce858661a06726a9a944f28e33ad7f9e08bea44dc3c230", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.jar", "source": {"sha1": "8f1cb192e229bc4cd1c900c51171d96706e6d195", "sha256": "d97341ce0a7554028db3403e407bb51f4d902bf3287f64f709d7a8156eaf1910", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9-sources.jar"} , "name": "org-apache-commons-commons-lang3", "actual": "@org-apache-commons-commons-lang3//jar", "bind": "jar/org/apache/commons/commons-lang3"},
    {"artifact": "org.yaml:snakeyaml:1.25", "lang": "java", "sha1": "8b6e01ef661d8378ae6dd7b511a7f2a33fae1421", "sha256": "b50ef33187e7dc922b26dbe4dd0fdb3a9cf349e75a08b95269901548eee546eb", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/1.25/snakeyaml-1.25.jar", "source": {"sha1": "045379604b7a254120101009ba09537737daed6f", "sha256": "fcc58a580b640e80f6c71acdd20b00dd87b80c9944dcad0389a904b77f04c88d", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/1.25/snakeyaml-1.25-sources.jar"} , "name": "org-yaml-snakeyaml", "actual": "@org-yaml-snakeyaml//jar", "bind": "jar/org/yaml/snakeyaml"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
