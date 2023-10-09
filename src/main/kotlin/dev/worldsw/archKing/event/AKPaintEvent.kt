package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class AKPaintEvent(private val plugin: ArchKingPlugin) : Listener {
    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.isBlockInHand) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!event.hasItem() || !event.hasBlock()) return
        if (event.item!!.type != Material.WHITE_DYE) return

        if (event.clickedBlock!!.type == Material.BRICKS || plugin.akBlock.getCustomBlockData(event.clickedBlock!!) == AKItemType.CONCRETE) {
            event.clickedBlock!!.type = Material.WHITE_CONCRETE
            if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(
                event.item!!.asQuantity(1)
            )
        }
    }
}