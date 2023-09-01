package dev.worldsw.archKing

import dev.worldsw.archKing.block.AKBlock
import dev.worldsw.archKing.block.AKOverlapBlock
import dev.worldsw.archKing.command.AKCommand
import dev.worldsw.archKing.recipe.AKRecipe
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.event.*
import dev.worldsw.archKing.item.AKItem
import org.bukkit.Bukkit
import org.bukkit.command.TabCompleter
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class ArchKingPlugin: JavaPlugin(), Listener {
    lateinit var storage: AKStorage
    lateinit var akItem: AKItem
    lateinit var akBlock: AKBlock
    lateinit var akOverlapBlock: AKOverlapBlock

    override fun onEnable() {
        storage = AKStorage(this)

        storage.init()
        akItem = AKItem(this)
        akBlock = AKBlock(this)
        akOverlapBlock = AKOverlapBlock(this)
        AKRecipe(this)

        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(AKStorageEvent(this), this)
        server.pluginManager.registerEvents(AKOverlapBlockEvent(this), this)
        server.pluginManager.registerEvents(AKGypsumEvent(this), this)
        server.pluginManager.registerEvents(AKBlockEvent(this), this)

        val voidTabCompleter = TabCompleter { _, _, _, _ -> mutableListOf() }
        val commandList = listOf("archkingitem")
        val getAkiCommands = listOf("archkingitem")
        commandList.forEach {
            getCommand(it)!!.setExecutor(AKCommand(this))
            if (it in getAkiCommands) {
                getCommand(it)!!.tabCompleter = TabCompleter { _, _, label, args ->
                    if (label.equals(it, true) && args.size == 1) {
                        akItem.getAllProperties()
                    } else {
                        mutableListOf()
                    }
                }
            } else {
                getCommand(it)!!.tabCompleter = voidTabCompleter
            }
        }
    }

    override fun onDisable() {
        storage.saveData()
        Bukkit.getScheduler().cancelTasks(this)
    }
}