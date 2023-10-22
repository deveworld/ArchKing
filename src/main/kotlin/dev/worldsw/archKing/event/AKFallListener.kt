package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent

class AKFallListener(private val plugin: ArchKingPlugin) : Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.getRelative(BlockFace.DOWN).type != Material.AIR) return

        val wood = plugin.storage.getData(AKStorage.GRAVITY, AKStorage.WOOD_GRAVITY)!!.asBoolean
        val concrete = plugin.storage.getData(AKStorage.GRAVITY, AKStorage.CONCRETE_GRAVITY)!!.asBoolean

        if (!(wood || concrete)) return

        val block = event.blockPlaced
        val location = block.location

        var success = false

        if (wood && block.type in listOf(Material.OAK_PLANKS, Material.ACACIA_PLANKS, Material.BAMBOO_PLANKS)) {
            location.getWorld().spawnFallingBlock(
                location.toCenterLocation().add(0.0, -0.5, 0.0),
                block.type.createBlockData()
            )
            success = true
        }
        if (concrete && plugin.akBlock.getCustomBlockData(block) == AKItemType.CONCRETE) {
            val entity = location.getWorld().spawnFallingBlock(
                location.toCenterLocation().add(0.0, -0.5, 0.0),
                block.type.createBlockData()
            )
            plugin.akBlock.fallAKBlock(block, entity)
            success = true
        }

        if (success) {
            event.isCancelled = true
            if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(
                event.itemInHand.asQuantity(1)
            )
        }
    }
}