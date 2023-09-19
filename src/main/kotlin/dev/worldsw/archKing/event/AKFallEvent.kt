package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.joml.Vector3f

class AKFallEvent(private val plugin: ArchKingPlugin) : Listener {
    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.block.getRelative(BlockFace.DOWN).type != Material.AIR) return
        if (
            event.blockPlaced.type !in listOf(Material.OAK_PLANKS, Material.ACACIA_PLANKS, Material.BAMBOO_PLANKS)
        ) return
        val block = event.block
        val location = block.location
        val fall = block.location.getWorld().spawnFallingBlock(location.toCenterLocation().add(0.0, -0.5, 0.0), event.blockPlaced.type.createBlockData())
        event.isCancelled = true
        if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(event.itemInHand.asQuantity(1))
    }
}