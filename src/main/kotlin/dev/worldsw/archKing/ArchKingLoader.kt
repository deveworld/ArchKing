package dev.worldsw.archKing

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader


class ArchKingLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
//        classpathBuilder.addLibrary(JarLibrary(Path.of("dependency.jar")))
//        val resolver = MavenLibraryResolver()
//        resolver.addDependency(Dependency(DefaultArtifact("com.example:example:version"), null))
//        resolver.addRepository(
//            RemoteRepository.Builder(
//                "paper",
//                "default",
//                "https://repo.papermc.io/repository/maven-public/"
//            ).build()
//        )
//        classpathBuilder.addLibrary(resolver)
    }
}