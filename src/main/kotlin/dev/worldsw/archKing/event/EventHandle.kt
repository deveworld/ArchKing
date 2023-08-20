package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.DataManager
import dev.worldsw.archKing.item.ArchKingItem
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


class EventHandle(private val plugin: ArchKingPlugin) : Listener {
    private fun rmcToc(block: Block) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val dataArchKingItem = plugin.itemManager.getCustomBlockData(block) ?: return@scheduleSyncDelayedTask
            if (dataArchKingItem != ArchKingItem.READY_MIXED_CONCRETE) return@scheduleSyncDelayedTask

            plugin.itemManager.removeCustomBlockData(block)
            plugin.itemManager.addCustomBlockData(block, ArchKingItem.CONCRETE)
            block.type = plugin.itemManager.getItem(ArchKingItem.CONCRETE).type
        }, (3600 + (-600..600).random()).toLong())
    }

    @EventHandler
    fun onWorldSaveEvent(event: WorldSaveEvent) {
        plugin.dataManager.saveData()
    }

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.block.type == Material.IRON_BARS) {
            plugin.rebarHandler.onPlaceRebar(event.block)
            event.isCancelled = true
            if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItem(ItemStack(Material.IRON_BARS))
        }

        val dataArchKingItem = event.itemInHand.itemMeta.persistentDataContainer
            .getOrDefault(NamespacedKey(plugin, ArchKingItem.CUSTOM_ITEM), PersistentDataType.INTEGER, ArchKingItem.NOT_CUSTOM_ITEM)
        if (dataArchKingItem == ArchKingItem.NOT_CUSTOM_ITEM) return
        plugin.itemManager.addCustomBlockData(event.block, dataArchKingItem)
        if (dataArchKingItem == ArchKingItem.READY_MIXED_CONCRETE) rmcToc(event.block)
    }

    private fun handleBlockMove(block: Block, direction: BlockFace) {
        val dataArchKingItem = plugin.itemManager.getCustomBlockData(block) ?: return
        plugin.itemManager.removeCustomBlockData(block)
        val newBlock = block.getRelative(direction)
        plugin.itemManager.addCustomBlockData(newBlock, dataArchKingItem)
    }

    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
        for (block in event.blocks) handleBlockMove(block, event.direction)
    }

    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonRetractEvent) {
        for (block in event.blocks) handleBlockMove(block, event.direction)
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (!event.isDropItems) return
        val dataArchKingItem = plugin.itemManager.getCustomBlockData(event.block) ?: return
        plugin.itemManager.removeCustomBlockData(event.block)

        val player = event.player
        if (event.player.gameMode == GameMode.CREATIVE) return

        if (!player.inventory.itemInMainHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            val dropItems = plugin.itemManager.getItem(dataArchKingItem, 1)
            event.isDropItems = false
            event.block.drops.clear()
            player.world.dropItemNaturally(event.block.location, dropItems)
        }
    }

    @EventHandler
    fun onBlockBreakBlockEvent(event: BlockBreakBlockEvent) {
        if (event.drops.size == 0) return
        val dataArchKingItem = plugin.itemManager.getCustomBlockData(event.block) ?: return
        plugin.itemManager.removeCustomBlockData(event.block)

        val dropItems = plugin.itemManager.getItem(dataArchKingItem, 1)
        event.block.drops.clear()
        event.block.world.dropItemNaturally(event.block.location, dropItems)
    }

    @EventHandler
    fun onEntityPickupItemEvent(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        if (event.item.itemStack.type == Material.AMETHYST_SHARD) event.item.itemStack = plugin.itemManager
            .getItem(ArchKingItem.GYPSUM, event.item.itemStack.amount)
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val location = event.entity.location.toBlockLocation().toString()
        if (plugin.dataManager.getData(DataManager.REBARS).asJsonObject.has(location)) {
            plugin.rebarHandler.onBreakRebar(event.entity)
            val player = (event.damager as Player)
            player.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
            if (player.gameMode != GameMode.CREATIVE) player.inventory.addItem(ItemStack(Material.IRON_BARS))
        }
    }
}