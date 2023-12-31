package dev.worldsw.archKing

import dev.worldsw.archKing.block.AKBlock
import dev.worldsw.archKing.block.AKOverlapBlock
import dev.worldsw.archKing.command.AKCommand
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.listeners.*
import dev.worldsw.archKing.recipe.AKRecipe
import org.bukkit.Bukkit
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

class ArchKingPlugin: JavaPlugin(){

    companion object{
        lateinit var instance:ArchKingPlugin
            private set
    }

    lateinit var storage: AKStorage
    lateinit var akItem: AKItem
    lateinit var akBlock: AKBlock
    lateinit var akOverlapBlock: AKOverlapBlock

    override fun onEnable() {

        instance = this

        storage = AKStorage(this)

        storage.init()
        akItem = AKItem(this)
        akBlock = AKBlock(this)
        akOverlapBlock = AKOverlapBlock(this)
        AKRecipe(this)

//        server.pluginManager.registerEvents(this, this)
//        server.pluginManager.registerEvents(AKStorageListener(this), this)
//        server.pluginManager.registerEvents(AKOverlapBlockListener(this), this)
//        server.pluginManager.registerEvents(AKGypsumListener(this), this)
//        server.pluginManager.registerEvents(AKBlockListener(this), this)
//        server.pluginManager.registerEvents(AKFallListener(this), this)
//        server.pluginManager.registerEvents(AKPaintListener(this), this)

        listOf(
            AKStorageListener(this),
            AKOverlapBlockListener(this),
            AKGypsumListener(this),
            AKBlockListener(this),
            AKFallListener(this),
            AKPaintListener(this)
        ).forEach{listener ->
            server.pluginManager.registerEvents(listener, this)
        }

        val voidTabCompleter = TabCompleter { _, _, _, _ -> mutableListOf() }
        val commandList = listOf("akitem", "akgravity")
        commandList.forEach {
            getCommand(it)!!.setExecutor(AKCommand(this))
            when (it) {
                "akitem" -> {
                    getCommand(it)!!.tabCompleter = TabCompleter { _, _, label, args ->
                        if (label.equals(it, true) && args.size == 1) {
                            akItem.getAllProperties()
                        } else {
                            mutableListOf()
                        }
                    }
                }

                "akgravity" -> {
                    getCommand(it)!!.tabCompleter = TabCompleter { _, _, label, args ->
                        if (label.equals(it, true) && args.size == 1) {
                            mutableListOf("concrete", "wood")
                        } else {
                            mutableListOf()
                        }
                    }
                }

                else -> {
                    getCommand(it)!!.tabCompleter = voidTabCompleter
                }
            }
        }
    }

    override fun onDisable() {
        storage.saveData()
        Bukkit.getScheduler().cancelTasks(this)
    }
}