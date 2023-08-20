package dev.worldsw.archKing

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

class ArchKingBootstrap: PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {

    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return ArchKingPlugin()
    }
}