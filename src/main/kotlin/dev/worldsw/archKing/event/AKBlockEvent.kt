package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItem
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.persistence.PersistentDataType


class AKBlockEvent(private val plugin: ArchKingPlugin) : Listener {
    /**
     * On Place AKBlock
     */
    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        plugin.akBlock.placeAKItem(event.itemInHand.itemMeta.persistentDataContainer, event.block)
    }

    /**
     * On Move AKBlock
     */
    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
        for (block in event.blocks) plugin.akBlock.moveAKBlock(block, event.direction)
    }
    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonRetractEvent) {
        for (block in event.blocks) plugin.akBlock.moveAKBlock(block, event.direction)
    }

    /**
     * On Break AKBlock
     */
    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (!event.isDropItems) return
        if (!plugin.akBlock.isAKBlock(event.block)) return

        var dropItem = false

        if (event.player.gameMode != GameMode.CREATIVE && !event.player.inventory.itemInMainHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            event.isDropItems = false
            event.block.drops.clear()
            dropItem = true
        }

        plugin.akBlock.breakBlock(event.block, dropItem)
    }
    @EventHandler
    fun onBlockBreakBlockEvent(event: BlockBreakBlockEvent) {
        if (event.drops.size == 0) return
        event.block.drops.clear()
        plugin.akBlock.breakBlock(event.block)
    }

    /**
     * On Fall AKBlock
     */
    @EventHandler
    fun onEntityChangeBlockEvent(event: EntityChangeBlockEvent) {
        plugin.akBlock.fallAKBlock(event.block, event.entity)
    }
    @EventHandler
    fun onEntityDropItemEvent(event: EntityDropItemEvent) {
        if (event.entityType != EntityType.FALLING_BLOCK) return
        val dataAKItemType = event.entity.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (dataAKItemType == AKItem.NOT_CUSTOM_ITEM) return

        plugin.akBlock.dropItem(event.entity.location, dataAKItemType)
        event.isCancelled = true
    }
}