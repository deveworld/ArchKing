package dev.worldsw.archKing.event

import com.jogamp.common.os.Platform
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AKRebarEvent(private val plugin: ArchKingPlugin) : Listener {
    /**
     * On Break Rebar
     */
    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return

        val data = plugin.storage.getData(AKStorage.REBARS, event.entity.location.toBlockLocation().toString()) ?: return
        plugin.akRebar.onBreakRebar(event.entity, data)

        val player = (event.damager as Player)
        player.world.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.addItem(plugin.akItem.getItem(data.asJsonObject.get("data").asString.toInt()))
    }

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val data = event.itemInHand.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (data == AKItem.NOT_CUSTOM_ITEM) return
        if (data !in listOf(AKItemType.REBAR_BEAM, AKItemType.REBAR_PILLAR, AKItemType.REBAR_SLAB)) return

        event.isCancelled = true
        val success = plugin.akRebar.onPlaceRebar(event.block, data)
        if (!success) return
        if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItemAnySlot(plugin.akItem.getItem(data))
    }

    @EventHandler
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.type != EntityType.INTERACTION) return

        val mainHandItem = event.player.inventory.itemInMainHand
        val offHandItem = event.player.inventory.itemInOffHand
        val location = event.rightClicked.location.toBlockLocation()

        if (!placeBlockOnRebar(event.player, mainHandItem, location)) placeBlockOnRebar(event.player, offHandItem, location)
    }

    private fun placeBlockOnRebar(player: Player, item: ItemStack, location: Location): Boolean {
        if (item.itemMeta == null) return false
        val dataOff = item.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (dataOff != AKItem.NOT_CUSTOM_ITEM) {
            plugin.akBlock.placeAKItem(item.itemMeta.persistentDataContainer, player.world.getBlockAt(location))
        }
        player.world.getBlockAt(location).type = item.type
        player.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.removeItemAnySlot(item.asQuantity(1))
        return true
    }
}