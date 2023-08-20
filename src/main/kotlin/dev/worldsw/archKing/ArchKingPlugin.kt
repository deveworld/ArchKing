package dev.worldsw.archKing

import dev.worldsw.archKing.event.EventHandle
import dev.worldsw.archKing.recipe.ArchKingRecipe
import dev.worldsw.archKing.data.DataManager
import dev.worldsw.archKing.item.ArchKingItem
import dev.worldsw.archKing.rebar.RebarHandler
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class ArchKingPlugin: JavaPlugin(), Listener {
    lateinit var dataManager: DataManager
    lateinit var rebarHandler: RebarHandler
    lateinit var itemManager: ArchKingItem

    override fun onEnable() {
        rebarHandler = RebarHandler(this)
        dataManager = DataManager(this)

        dataManager.init()
        itemManager = ArchKingItem(this)
        ArchKingRecipe(this)

        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(EventHandle(this), this)
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
    }
}