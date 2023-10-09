package dev.worldsw.archKing.block

import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
            plugin.storage.removeMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString())
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
        if (dataAKItemType in AKOverlapBlock.OVERLAP_BLOCKS) return
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

    fun fallAKBlock(block: Block, entity: Entity) {
        if (entity.type != EntityType.FALLING_BLOCK) return
        val dataAKItemType = plugin.akBlock.getCustomBlockData(block)
        if (dataAKItemType == null) {
            plugin.akBlock.placeAKItem(entity.persistentDataContainer, block)
        } else {
            plugin.akBlock.removeCustomBlockData(block)
            entity.persistentDataContainer.set(
                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
                PersistentDataType.INTEGER,
                dataAKItemType
            )
        }
    }

    private fun breakRMC(block: Block) {
        val data = plugin.storage.getMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString()) ?: return
        plugin.storage.removeMemory(AKStorage.READY_MIXED_CONCRETE_HARD, block.location.toString())
        plugin.server.scheduler.cancelTask(data.asJsonObject.get("schedule").asInt)
    }

    fun renderGravity(player: Player) {
        val wood = plugin.storage.getData(AKStorage.GRAVITY, AKStorage.WOOD_GRAVITY)!!.asBoolean
        val concrete = plugin.storage.getData(AKStorage.GRAVITY, AKStorage.CONCRETE_GRAVITY)!!.asBoolean

        if (!(wood || concrete)) return

        val radius = 50

        if (wood) {
            for (block in getNearbyBlocks(player.location, radius)) {
                val location = block.location
                if (block.type in listOf(Material.OAK_PLANKS, Material.ACACIA_PLANKS, Material.BAMBOO_PLANKS)) {
                    location.getWorld().spawnFallingBlock(
                        location.toCenterLocation().add(0.0, -0.5, 0.0),
                        block.type.createBlockData()
                    )
                    block.type = Material.AIR
                }
            }
        }
        if (concrete) {
            for (block in getNearbyBlocks(player.location, radius)) {
                val location = block.location
                if (plugin.akBlock.getCustomBlockData(block) == AKItemType.CONCRETE) {
                    val entity = location.getWorld().spawnFallingBlock(
                        location.toCenterLocation().add(0.0, -0.5, 0.0),
                        block.type.createBlockData()
                    )
                    plugin.akBlock.fallAKBlock(block, entity)
                    block.type = Material.AIR
                }
            }
        }
    }

    private fun getNearbyBlocks(location: Location, radius: Int): List<Block> {
        val blocks: MutableList<Block> = ArrayList()
        for (x in location.blockX - radius..location.blockX + radius) {
            for (y in location.blockY - radius..location.blockY + radius) {
                for (z in location.blockZ - radius..location.blockZ + radius) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z))
                }
            }
        }
        return blocks
    }

    fun getCustomBlockData(block: Block): Int? {
        val data = plugin.storage.getData(AKStorage.ARCHKING_BLOCK, block.location.toString()) ?: return null
        return data.asJsonObject.get("data").asString.toIntOrNull()
    }

    private fun addCustomBlockData(block: Block, customBlock: Int) {
        val data = JsonObject()
        data.addProperty("data", customBlock.toString())
        plugin.storage.setData(AKStorage.ARCHKING_BLOCK, block.location.toString(), data)
    }

    private fun removeCustomBlockData(block: Block) {
        plugin.storage.removeData(AKStorage.ARCHKING_BLOCK, block.location.toString())
    }
}