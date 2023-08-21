package dev.worldsw.archKing.block

import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class AKBlock(private val plugin: ArchKingPlugin) {
    fun isAKBlock(block: Block): Boolean {
        return getCustomBlockData(block) != null
    }

    fun breakBlock(block: Block, dropItem: Boolean = true) {
        val dataAKItemType = getCustomBlockData(block) ?: return
        plugin.akBlock.removeCustomBlockData(block)
        if (dataAKItemType == AKItemType.READY_MIXED_CONCRETE) breakRMC(block)

        if (dropItem) dropItem(block.location, dataAKItemType)
    }

    fun dropItem(location: Location, dataAKItemType: Int) {
        val dropItems = plugin.akItem.getItem(dataAKItemType, 1)
        location.world.dropItemNaturally(location, dropItems)
    }
    /**
     * On Place R.M.C.
     */
    private fun rmcToc(block: Block) {
        val rmc = plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            val dataAKItemType = plugin.akBlock.getCustomBlockData(block) ?: return@scheduleSyncDelayedTask
            if (dataAKItemType != AKItemType.READY_MIXED_CONCRETE) return@scheduleSyncDelayedTask

            plugin.akBlock.removeCustomBlockData(block)
            plugin.akBlock.addCustomBlockData(block, AKItemType.CONCRETE)
            block.type = plugin.akItem.getItem(AKItemType.CONCRETE).type
        }, (3600 + (-600..600).random()).toLong())
        val data = JsonObject()
        data.addProperty("schedule", rmc)
        plugin.storage.addMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString(), data)
    }

    /**
     * On Place AKBlock
     */
    fun placeAKItem(data: PersistentDataContainer, block: Block) {
        val dataAKItemType = data.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )
        if (dataAKItemType == AKItem.NOT_CUSTOM_ITEM) return
        plugin.akBlock.addCustomBlockData(block, dataAKItemType)
        if (dataAKItemType == AKItemType.READY_MIXED_CONCRETE) rmcToc(block)
    }

    /**
     * On Move AKBlock
     */
    fun moveAKBlock(block: Block, direction: BlockFace) {
        val dataAKItemType = plugin.akBlock.getCustomBlockData(block) ?: return
        plugin.akBlock.removeCustomBlockData(block)
        val newBlock = block.getRelative(direction)
        plugin.akBlock.addCustomBlockData(newBlock, dataAKItemType)
    }

    private fun breakRMC(block: Block) {
        val data = plugin.storage.getMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString()) ?: return
        plugin.storage.removeMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString())
        plugin.server.scheduler.cancelTask(data.asJsonObject.get("schedule").asInt)
    }

    fun getCustomBlockData(block: Block): Int? {
        if (!plugin.storage.getData(AKStorage.CUSTOM_ITEM).asJsonObject.has(block.location.toString())) return null
        return plugin.storage.getData(AKStorage.CUSTOM_ITEM, block.location.toString())!!.asJsonObject!!.get("data").asInt
    }

    private fun addCustomBlockData(block: Block, customBlock: Int) {
        val data = JsonObject()
        data.addProperty("data", customBlock)
        plugin.storage.addData(AKStorage.CUSTOM_ITEM, block.location.toString(), data)
    }

    fun removeCustomBlockData(block: Block) {
        plugin.storage.removeData(AKStorage.CUSTOM_ITEM, block.location.toString())
    }
}