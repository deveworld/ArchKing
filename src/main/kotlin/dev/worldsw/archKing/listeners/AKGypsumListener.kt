package dev.worldsw.archKing.listeners

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class AKGypsumListener(private val plugin: ArchKingPlugin) : Listener {
    /**
     * Change AMETHYST_SHARD to GYPSUM
     */
    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        if (event.item.itemStack.type == Material.AMETHYST_SHARD) event.item.itemStack = plugin.akItem
            .getItem(AKItemType.GYPSUM, event.item.itemStack.amount)
    }
}