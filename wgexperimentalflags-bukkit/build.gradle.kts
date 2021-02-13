import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.HasConvention

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "bstats"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        name = "aikar-timings"
        url = uri("http://repo.aikar.co/nexus/content/groups/aikar/")
    }
}

dependencies {
    "api"("com.destroystokyo.paper:paper-api:1.16.2-R0.1-SNAPSHOT")
    "implementation"("io.papermc:paperlib:1.0.4")
    "api"("com.sk89q.worldedit:worldedit-bukkit:${Versions.WORLDEDIT}")
    "api"("com.sk89q.worldguard:worldguard-bukkit:${Versions.WORLDGUARD}")
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "implementation"("org.bstats:bstats-bukkit:1.7")
}

tasks.named<Upload>("install") {
    (repositories as HasConvention).convention.getPlugin<MavenRepositoryHandlerConvention>().mavenInstaller {
        pom.whenConfigured {
            dependencies.firstOrNull { dep ->
                dep!!.withGroovyBuilder {
                    getProperty("groupId") == "com.destroystokyo.paper" && getProperty("artifactId") == "paper-api"
                }
            }?.withGroovyBuilder {
                setProperty("groupId", "org.spigotmc")
                setProperty("artifactId", "spigot-api")
            }
        }
    }
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.bstats", "de.terraconia.wgexperimentalflags.bukkit.bstats") {
            include(dependency("org.bstats:bstats-bukkit:1.7"))
        }
        relocate ("io.papermc.lib", "de.terraconia.wgexperimentalflags.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib:1.0.4"))
        }
        relocate ("co.aikar.timings.lib", "de.terraconia.wgexperimentalflags.bukkit.timingslib") {
            include(dependency("co.aikar:minecraft-timings:1.0.4"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
