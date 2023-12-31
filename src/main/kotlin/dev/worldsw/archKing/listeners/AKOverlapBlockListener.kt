package dev.worldsw.archKing.listeners

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

class AKOverlapBlockListener(private val plugin: ArchKingPlugin) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val data = event.itemInHand.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (data == AKItem.NOT_CUSTOM_ITEM) return
        if (data !in AKOverlapBlock.OVERLAP_BLOCKS) return

        event.isCancelled = true
//        // Get Near Interaction entity
//        val entities = event.block.location.add(0.5, 0.0, 0.5).getNearbyEntitiesByType(Interaction::class.java, 0.5)
//        plugin.logger.info(entities.size.toString())
//        for (entity in entities) {
//            plugin.logger.info(entity.uniqueId.toString())
//        }
        val success = when (data) {
            in AKOverlapBlock.REBARS -> {
                if (plugin.storage.getData(
                        AKStorage.REBARS,
                        event.block.location.toBlockLocation().toString()
                    ) != null
                ) {
                    plugin.akOverlapBlock.placeBlockOnBlock(event.itemInHand, event.block.location.toBlockLocation())
                } else {
                    plugin.akOverlapBlock.onRebarPlace(event.block, data)
                }
            }
            in AKOverlapBlock.PIPES -> {
                if (plugin.storage.getData(
                        AKStorage.PIPES,
                        event.block.location.toBlockLocation().toString()
                    ) != null
                ) {
                    plugin.akOverlapBlock.placeBlockOnBlock(event.itemInHand, event.block.location.toBlockLocation())
                } else {
                    plugin.akOverlapBlock.onPipePlace(event.block, data)
                }
            }
            in AKOverlapBlock.STEEL_FRAMES -> {
                if (plugin.storage.getData(
                        AKStorage.STEEL_FRAMES,
                        event.block.location.toBlockLocation().toString()
                    ) != null
                ) {
                    plugin.akOverlapBlock.placeBlockOnBlock(event.itemInHand, event.block.location.toBlockLocation())
                } else {
                    plugin.akOverlapBlock.onFramePlace(event.block, data)
                }
            }
            else -> false
        }
        if (!success) return
        if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(plugin.akItem.getItem(data))
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (event.entity !is Interaction) return

        val data = plugin.storage.getData(AKStorage.REBARS, event.entity.location.toBlockLocation().toString())
            ?: plugin.storage.getData(AKStorage.PIPES, event.entity.location.toBlockLocation().toString())
            ?: plugin.storage.getData(AKStorage.STEEL_FRAMES, event.entity.location.toBlockLocation().toString())?: return
        val akItemData = data.asJsonObject.get("data").asString.toInt()
        var quantity = 1
        if (akItemData in AKOverlapBlock.REBARS) {
            val rebar =
                event.entity.world.getEntity(UUID.fromString(data.asJsonObject.get("model").asString))!! as BlockDisplay

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
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
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