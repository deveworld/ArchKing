package dev.worldsw.archKing.block

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Candle
import org.bukkit.block.data.type.Wall
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

class AKOverlapBlock(private val plugin: ArchKingPlugin) {
    companion object {
        val OVERLAP_BLOCKS = listOf(AKItemType.PIPE, AKItemType.REBAR_PILLAR, AKItemType.REBAR_BEAM, AKItemType.REBAR_SLAB)

        val PIPES = listOf(AKItemType.PIPE)
        val REBARS = listOf(AKItemType.REBAR_PILLAR, AKItemType.REBAR_BEAM, AKItemType.REBAR_SLAB)
    }

    private fun promiseBlock(blockDisplay: BlockDisplay, transformation: Transformation) {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, {
            blockDisplay.transformation = transformation
        }, 0L)
    }

    private fun getTransformation(scale: Vector3f): Transformation {
        val zeroVector3f = Vector3f(0f, 0f, 0f)
        val zeroAxisAngle4f = AxisAngle4f(0f, zeroVector3f)
        return Transformation(zeroVector3f, zeroAxisAngle4f, scale, zeroAxisAngle4f)
    }

    private fun isUpWall(wall: Wall): Boolean {
        val northHeight = wall.getHeight(BlockFace.NORTH)
        val southHeight = wall.getHeight(BlockFace.SOUTH)
        val eastHeight = wall.getHeight(BlockFace.EAST)
        val westHeight = wall.getHeight(BlockFace.WEST)

        return if (northHeight != Wall.Height.NONE && northHeight == southHeight
            && eastHeight == Wall.Height.NONE && eastHeight == westHeight) {
            false
        } else if (eastHeight != Wall.Height.NONE && eastHeight == westHeight
            && northHeight == Wall.Height.NONE && northHeight == southHeight) {
            false
        } else {
            true
        }
    }

    private fun updateRebarSlab(location: Location): Boolean {
        val alreadyData = plugin.storage.getData(AKStorage.REBARS, location.toBlockLocation().toString())
        if (alreadyData == null || alreadyData.asJsonObject.get("data").asString.toIntOrNull() != AKItemType.REBAR_SLAB) return false
        val slab =
            location.world.getEntity(UUID.fromString(alreadyData.asJsonObject.get("rebar").asString))!! as BlockDisplay
        if (slab.block as? Candle == null) return false
        val slabData = slab.block as Candle
        if (slabData.maximumCandles == slabData.candles) return false
        slabData.candles += 1
        slab.block = slabData
        return true
    }

    private fun updateRebarWall(block: Block, blockData: BlockData, height: Wall.Height) {
        if (blockData as? Wall == null) return
        for (face in listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            val otherRebarData = plugin.storage.getData(
                AKStorage.REBARS,
                block.getRelative(face).location.toBlockLocation().toString()
            ) ?: continue
            val otherRebar =
                block.world.getEntity(UUID.fromString(otherRebarData.asJsonObject.get("rebar").asString))!! as BlockDisplay
            if (otherRebar.block as? Wall == null) continue
            blockData.setHeight(face, height)
            val otherRebarBlockData = otherRebar.block as Wall
            otherRebarBlockData.setHeight(face.oppositeFace, height)
            otherRebarBlockData.isUp = isUpWall(otherRebarBlockData)
            otherRebar.block = otherRebarBlockData
        }
        blockData.isUp = isUpWall(blockData)
    }

    fun placeBlockOnBlock(player: Player, item: ItemStack, location: Location): Boolean {
        if (item.itemMeta == null) return false
        val itemData = item.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )

        if (itemData == AKItem.NOT_CUSTOM_ITEM) {
            player.world.getBlockAt(location).type = item.type
            player.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
            if (player.gameMode != GameMode.CREATIVE) player.inventory.removeItemAnySlot(item.asQuantity(1))
            return true
        }

        if (itemData in listOf(AKItemType.REBAR_BEAM, AKItemType.REBAR_PILLAR)
            && plugin.storage.getData(AKStorage.REBARS, location.toBlockLocation().toString()) != null) return false
        if (itemData == AKItemType.REBAR_SLAB) {
            return if (plugin.akOverlapBlock.updateRebarSlab(location)) {
                if (player.gameMode != GameMode.CREATIVE) player.inventory.removeItemAnySlot(
                    item.asQuantity(
                        1
                    )
                )
                true
            } else {
                false
            }
        }

        if (itemData in PIPES) {
            return if (onPipePlace(location.block, itemData)) {
                if (player.gameMode != GameMode.CREATIVE) player.inventory.removeItemAnySlot(item.asQuantity(1))
                true
            } else {
                false
            }
        }

        player.world.getBlockAt(location).type = item.type
        player.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
        plugin.akBlock.placeAKItem(item.itemMeta.persistentDataContainer, player.world.getBlockAt(location))
        return true
    }

    fun onRebarPlace(block: Block, rebarData: Int): Boolean {
        val alreadyData = plugin.storage.getData(AKStorage.REBARS, block.location.toBlockLocation().toString())
        if (alreadyData != null) return false
        if (rebarData !in REBARS) return false

        val world = block.world
        val location = block.location
        val rebar = world.spawn(location, BlockDisplay::class.java) { rebarBlock ->
            val blockData = plugin.akItem.getItem(rebarData).type.createBlockData()
            updateRebarWall(block, blockData, Wall.Height.LOW)
            rebarBlock.block = blockData
            rebarBlock.persistentDataContainer.set(
                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
                PersistentDataType.INTEGER,
                rebarData
            )
            promiseBlock(rebarBlock, getTransformation(Vector3f(1f, 1f, 1f)))
        }

        val rebarInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.9f
            interact.interactionHeight = if (rebarData == AKItemType.REBAR_SLAB) 0.2f else 0.9f
        }

        val data = JsonObject()
        data.addProperty("data", rebarData.toString())
        data.addProperty("rebar", rebar.uniqueId.toString())
        data.addProperty("rebarInteraction", rebarInteraction.uniqueId.toString())

        plugin.storage.addData(AKStorage.REBARS, location.toBlockLocation().toString(), data)
        return true
    }

    fun onPipePlace(block: Block, pipeData: Int): Boolean {
        val alreadyData = plugin.storage.getData(AKStorage.PIPES, block.location.toBlockLocation().toString())
        if (alreadyData != null) return false
        if (pipeData !in PIPES) return false

        val world = block.world
        val location = block.location
        val pipe = world.spawn(location, BlockDisplay::class.java) { pipeBlock ->
            val blockData = plugin.akItem.getItem(pipeData).type.createBlockData()
            pipeBlock.block = blockData
            pipeBlock.persistentDataContainer.set(
                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
                PersistentDataType.INTEGER,
                pipeData
            )
            promiseBlock(pipeBlock, getTransformation(Vector3f(1f, 1f, 1f)))
        }

        val pipeInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.3f
            interact.interactionHeight = 1f
        }

        val data = JsonObject()
        data.addProperty("data", pipeData.toString())
        data.addProperty("pipe", pipe.uniqueId.toString())
        data.addProperty("pipeInteraction", pipeInteraction.uniqueId.toString())

        plugin.storage.addData(AKStorage.PIPES, location.toBlockLocation().toString(), data)
        return true
    }

    fun onBreak(entity: Entity, data: JsonElement) {
        val akItemData = data.asJsonObject.get("data").asString.toIntOrNull()

        val location = entity.location.toBlockLocation().toString()
        for ((key, value) in data.asJsonObject.entrySet()) {
            if (key == "data") continue
            val uuid = value.asString
            val model = entity.world.getEntity(UUID.fromString(uuid))!!
            if (akItemData in REBARS && model as? BlockDisplay != null) {
                val block = entity.world.getBlockAt(entity.location)
                updateRebarWall(block, model.block, Wall.Height.NONE)
            }
            model.remove()
        }

        val type: String = if (akItemData in REBARS) {
            AKStorage.REBARS
        } else {
            if (akItemData in PIPES) AKStorage.PIPES else ""
        }
        plugin.storage.removeData(type, location)
    }
}