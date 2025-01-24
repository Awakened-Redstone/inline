@file:Suppress("UnstableApiUsage")
import java.net.URI

//region Setup
plugins {
    `maven-publish`
    java
    kotlin("jvm") version "2.1.0"
    id("dev.architectury.loom") version "1.9-SNAPSHOT"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "0.8.+"
    //id("dev.kikugie.j52j") version "1.0.2"
}

class CompatMixins {
    private var common: List<String> = listOf()
    private var fabric: List<String> = listOf()
    private var neoforge: List<String> = listOf()

    fun getMixins(): Map<String, String> {
        val mixins = common + if (loader.isFabric) fabric else neoforge
        return mapOf(
            "mod_id" to mod.id,
            "compat_mixins" to "[\n${mixins.joinToString(",\n") { "\"$it\"" }}\n]"
        )
    }
}

val mod = ModData(project)
val loader = LoaderData(project, loom.platform.get().name.lowercase())
val minecraftVersion = MinecraftVersionData(stonecutter)
//val awName = "${mod.id}.accesswidener"

version = "${mod.version}-$loader+$minecraftVersion"
group = mod.group
base.archivesName.set(mod.id)
//endregion

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.bawnorton.com/releases/")
    maven("https://maven.shedaniel.me")
    maven("https://jitpack.io")
    maven("https://maven.su5ed.dev/releases")
    maven("https://maven.isxander.dev/releases")

    maven("https://maven.felnull.dev")

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = URI("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven {
        setUrl("https://dl.cloudsmith.io/public/klikli-dev/mods/maven/")
        content {
            includeGroup("com.klikli_dev")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    //include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.0-beta.3")!!)!!)
    annotationProcessor(modImplementation("com.bawnorton.configurable:configurable-${loader.name()}-yarn:${property("configurable")}+$minecraftVersion") {
        exclude(module = "fabric-networking-api-v1")
        exclude(module = "yet-another-config-lib")
    })

    modCompileOnly("dev.isxander:yet-another-config-lib:${property("yacl")}-${loader.name()}")
}

fabric {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}+$minecraftVersion")
}

neoforge {
    modImplementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${property("fabric_api")}+${property("forgified_fabric_api")}+$minecraftVersion")
}

//region Build
fun Project.fabric(configuration: DependencyHandlerScope.() -> Unit) {
    if (loader.isFabric) dependencies(configuration)
}

fun Project.neoforge(configuration: DependencyHandlerScope.() -> Unit) {
    if (loader.isNeoForge) dependencies(configuration)
}

loom {
    //accessWidenerPath.set(rootProject.file("src/main/resources/$awName"))

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }

    runConfigs["client"].apply {
        vmArgs("-Dmixin.debug.export=true")
        programArgs("--quickPlaySingleplayer \"New World\" --uuid 2e7c2349-94ec-4862-8b68-344d049840d2 --username AwakenedRedstone")
    }

    sourceSets {
        main {
            resources {
                srcDir(project.file("src/main/generated"))
            }
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.release = minecraftVersion.javaVersion()
    }

    processResources {
        val compatMixins = CompatMixins().getMixins()
        inputs.properties(compatMixins)
        filesMatching("${mod.id}-compat.mixins.json") { expand(compatMixins) }

        val modMetadata = mapOf(
            "mod_id" to mod.id,
            "mod_name" to mod.name,
            "description" to mod.description,
            "version" to mod.version,
            "minecraft_dependency" to mod.minecraftDependency,
            "loader_version" to loader.getVersion()
        )

        inputs.properties(modMetadata)
        filesMatching("fabric.mod.json") { expand(modMetadata) }
        filesMatching("META-INF/neoforge.mods.toml") { expand(modMetadata) }
    }

    jar {
        dependsOn("copyDatagen")
    }

    withType<AbstractCopyTask> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    /*clean {
        delete(file(rootProject.file("build")))
    }*/
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(minecraftVersion.javaVersion())
    targetCompatibility = JavaVersion.toVersion(minecraftVersion.javaVersion())
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }
}
//endregion

//region Others
if (loader.isFabric) {
    fabricApi {
        configureDataGeneration {
            createRunConfiguration = true
            modId = mod.id
            outputDirectory = rootProject.rootDir.resolve("src/main/generated")
        }
    }

    dependencies {
        mappings("net.fabricmc:yarn:$minecraftVersion+build.${property("yarn_build")}:v2")
        modImplementation("net.fabricmc:fabric-loader:${loader.getVersion()}")
    }

    tasks {
        register<Copy>("copyDatagen") {
            from("src/main/generated")
            into("${layout.buildDirectory.get()}/resources/main")
            dependsOn("runDatagen")
        }

        processResources {
            exclude("**/neoforge.mods.toml")
        }
    }
}

if (loader.isNeoForge) {
    val generatedSources = rootProject.rootDir.resolve("src/main/generated")

    sourceSets {
        main {
            resources.srcDir(generatedSources)
        }
    }

    dependencies {
        mappings(loom.layered {
            mappings("net.fabricmc:yarn:$minecraftVersion+build.${property("yarn_build")}:v2")
            mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
        })
        neoForge("net.neoforged:neoforge:${loader.getVersion()}")
    }

    tasks {
        processResources {
            exclude("**/fabric.mod.json")
        }

        /*remapJar {
            atAccessWideners.add(awName)
        }*/

        register<Copy>("copyDatagen") {
            from(rootProject.file("versions/$minecraftVersion-fabric/src/main/generated"))
            into("${layout.buildDirectory.get()}/resources/main")
        }
    }
}

tasks.register("swapLoaders") {
    group = "stonecutter"

    val tasks = parent!!.tasks
    if (stonecutter.current.isActive) {
        when (loader.name()) {
            "neoforge" -> finalizedBy(tasks.findByName("Set active project to $minecraftVersion-fabric"))
            "fabric" -> finalizedBy(tasks.findByName("Set active project to $minecraftVersion-neoforge"))
        }
    }
}
//endregion

//region Publish
extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "bawnorton"
            url = uri("https://maven.bawnorton.com/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "${mod.group}.${mod.id}"
            artifactId = "${mod.id}-$loader"
            version = "${mod.version}+$minecraftVersion"

            from(components["java"])
        }
    }
}

val changelogFile: String = if (file("changelog.md").exists()) file("changelog.md").readText() else "No changelog provided"

var changelogString = ""
val lines = changelogFile.split("\n")
var going = false
lines.forEach { line ->
    if (line.startsWith("##") && !line.startsWith("###")) {
        if (going) return@forEach
        going = true
    }

    if (going) {
        changelogString += line + "\n"
    }
}

fun loader(): String {
    return when (loader.name()) {
        "fabric" -> "Fabric"
        "neoforge" -> "NeoForge"
        else -> "Unknown"
    }
}

publishMods {
    val projectVersion: String = property("mod_version").toString()
    val projectVersionNumber: List<String> = projectVersion.split(Regex("-"), 2)
    type = STABLE

    var releaseName = "Release ${projectVersionNumber[0]} for ${loader()}"
    if (projectVersion.contains("beta")) {
        val projectBeta: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Beta ${projectBeta[1]} for ${loader()}"
        type = BETA
    } else if (projectVersion.contains("alpha")) {
        val projectAlpha: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Alpha ${projectAlpha[1]} for ${loader()}"
        type = ALPHA
    } else if (projectVersion.contains("rc")) {
        val projectRC: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Release Candidate ${projectRC[1]} for ${loader()}"
        type = BETA
    }

    file = tasks.remapJar.get().archiveFile
    val tag = "${mod.version}+$loader"
    val branch = "main"
    changelog = changelogString
    displayName = releaseName
    modLoaders.add(loader.name())

    /*github {
        accessToken = providers.gradleProperty("GITHUB_TOKEN")
        repository = "Awakened-Redstone/${mod.name}"
        commitish = branch
        tagName = tag
    }*/

    modrinth {
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
        projectId = mod.modrinthProjId
        version = tag
        minecraftVersions.addAll(mod.supportedVersions.split(", "))
        requires("configurable")
        if (loader.isFabric) {
            requires("fabric-api")
        } else {
            requires("forgified-fabric-api")
        }
    }

    curseforge {
        accessToken = providers.gradleProperty("CURSEFORGE_TOKEN")
        projectId = mod.curseforgeProjId
        minecraftVersions.addAll(mod.supportedVersions)
        requires("configurable")
        if (loader.isFabric) {
            requires("fabric-api")
        } else {
            requires("forgified-fabric-api")
        }
    }
}
//endregion
