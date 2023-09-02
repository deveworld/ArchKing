package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.block.AKOverlapBlock
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.data.type.Candle
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class AKOverlapBlockEvent(private val plugin: ArchKingPlugin) : Listener {

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val data = event.itemInHand.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (data == AKItem.NOT_CUSTOM_ITEM) return
        if (data !in AKOverlapBlock.OVERLAP_BLOCKS) return

        event.isCancelled = true
        val success = if (data in AKOverlapBlock.REBARS) {
            plugin.akOverlapBlock.onRebarPlace(event.block, data)
        } else {
            plugin.akOverlapBlock.onPipePlace(event.block, data)
        }
        if (!success) return
        if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(plugin.akItem.getItem(data))
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (event.entity !is Interaction) return

        val data = plugin.storage.getData(AKStorage.REBARS, event.entity.location.toBlockLocation().toString())
            ?: plugin.storage.getData(AKStorage.PIPES, event.entity.location.toBlockLocation().toString()) ?: return
        val akItemData = data.asJsonObject.get("data").asString.toInt()
        var quantity = 1
        if (akItemData in AKOverlapBlock.REBARS) {
            val rebar =
                event.entity.world.getEntity(UUID.fromString(data.asJsonObject.get("rebar").asString))!! as BlockDisplay

            quantity = if (akItemData == AKItemType.REBAR_SLAB) {
                if (rebar.block as? Candle != null) (rebar.block as Candle).candles else 1
            } else {
                1
            }
        }
        plugin.akOverlapBlock.onBreak(event.entity, data)

        val player = (event.damager as Player)
        player.world.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.addItem(
            plugin.akItem.getItem(
                akItemData,
                quantity
            )
        )
    }

    @EventHandler
    fun onPlayerInteractAtEntityEvent(event: PlayerInteractAtEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return

        var location = event.rightClicked.location
        if (event.player.isSneaking) {
            val face = event.rightClicked.boundingBox.rayTrace(
                event.player.eyeLocation.toVector(),
                event.player.location.direction,
                10.0
            )?.hitBlockFace ?: return
            location = location.block.getRelative(face).location
        }
        if (location.block.type != Material.AIR) return

        val mainHandItem = event.player.inventory.itemInMainHand
        val offHandItem = event.player.inventory.itemInOffHand

        if (!plugin.akOverlapBlock.placeBlockOnBlock(mainHandItem, location.toBlockLocation())) {
            if (plugin.akOverlapBlock.placeBlockOnBlock(offHandItem, location.toBlockLocation())) {
                if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(offHandItem.asQuantity(1))
            }
        } else {
            if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(mainHandItem.asQuantity(1))
        }
    }
}