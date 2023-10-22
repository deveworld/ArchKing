package dev.worldsw.archKing.listeners

import dev.worldsw.archKing.ArchKingPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldSaveEvent

class AKStorageListener(private val plugin: ArchKingPlugin) : Listener {
    /**
     * Save data at world save.
     */
    @EventHandler
    fun onWorldSave(event: WorldSaveEvent) {
        plugin.storage.saveData()
    }
}