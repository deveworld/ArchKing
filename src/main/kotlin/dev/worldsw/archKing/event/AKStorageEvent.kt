package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldSaveEvent

class AKStorageEvent(private val plugin: ArchKingPlugin) : Listener {
    /**
     * Save data at world save.
     */
    @EventHandler
    fun onWorldSaveEvent(event: WorldSaveEvent) {
        plugin.storage.saveData()
    }
}