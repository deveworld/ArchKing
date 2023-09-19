package dev.worldsw.archKing.block

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
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
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

class AKOverlapBlock(private val plugin: ArchKingPlugin) {
    companion object {
        val OVERLAP_BLOCKS = listOf(
            AKItemType.PIPE,
            AKItemType.REBAR_PILLAR,
            AKItemType.REBAR_BEAM,
            AKItemType.REBAR_SLAB,
            AKItemType.STEEL_FRAME,
            AKItemType.DECK_PLATE
        )

        val PIPES = listOf(AKItemType.PIPE)
        val REBARS = listOf(AKItemType.REBAR_PILLAR, AKItemType.REBAR_BEAM, AKItemType.REBAR_SLAB)
        val STEEL_FRAMES = listOf(AKItemType.STEEL_FRAME, AKItemType.DECK_PLATE)
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
            location.world.getEntity(UUID.fromString(alreadyData.asJsonObject.get("model").asString))!! as BlockDisplay
        if (slab.block as? Candle == null) return false
        val slabData = slab.block as Candle
        if (slabData.maximumCandles == slabData.candles) return false
        slabData.candles += 1
        slab.block = slabData
        return true
    }

    private fun updateWall(block: Block, blockData: BlockData, storage: String, height: Wall.Height): BlockData {
        if (blockData as? Wall == null) return blockData
        for (face in listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            val otherWallData = plugin.storage.getData(
                storage,
                block.getRelative(face).location.toBlockLocation().toString()
            ) ?: continue
            val otherWall =
                block.world.getEntity(UUID.fromString(otherWallData.asJsonObject.get("model").asString))!! as BlockDisplay
            if (otherWall.block as? Wall == null) continue
            blockData.setHeight(face, height)
            val otherWallBlockData = otherWall.block as Wall
            otherWallBlockData.setHeight(face.oppositeFace, height)
            otherWallBlockData.isUp = isUpWall(otherWallBlockData)
            otherWall.block = otherWallBlockData
        }
        blockData.isUp = isUpWall(blockData)
        return blockData
    }

    fun placeBlockOnBlock(item: ItemStack, location: Location): Boolean {
        if (item.itemMeta == null) return false
        val itemData = item.itemMeta.persistentDataContainer.getOrDefault(
            NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
            PersistentDataType.INTEGER,
            AKItem.NOT_CUSTOM_ITEM
        )

        if (itemData == AKItem.NOT_CUSTOM_ITEM) { // Place NOT custom data => allow
            if (!item.type.isBlock) return false
            location.world.getBlockAt(location).type = item.type
            location.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
            return true
        }

        // rebar place
        if (itemData in REBARS) {
            val rebarData = plugin.storage.getData(AKStorage.REBARS, location.toBlockLocation().toString())
            if (rebarData != null) {
                val rebarItemData = rebarData.asJsonObject.get("data").asString.toIntOrNull()
                // Update rebar slab
                if (itemData == AKItemType.REBAR_SLAB && rebarItemData == AKItemType.REBAR_SLAB)
                    return plugin.akOverlapBlock.updateRebarSlab(location)
                return false
            }
            return onRebarPlace(location.block, itemData)
        }

        // pipe place
        if (itemData in PIPES) {
            if (plugin.storage.getData(AKStorage.PIPES, location.toBlockLocation().toString()) != null) return false

            return onPipePlace(location.block, itemData)
        }

        // steel frame place
        if (itemData in STEEL_FRAMES) {
            if (plugin.storage.getData(AKStorage.STEEL_FRAMES, location.toBlockLocation().toString()) != null) return false

            return onFramePlace(location.block, itemData)
        }

        location.world.getBlockAt(location).type = item.type
        location.world.playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f)
        plugin.akBlock.placeAKItem(item.itemMeta.persistentDataContainer, location.world.getBlockAt(location))
        return true
    }

    private fun spawnBlockDisplay(block: Block, itemData: Int): BlockDisplay {
        val world = block.world
        val location = block.location
        val entity = world.spawn(location, BlockDisplay::class.java) { blockEntity ->
            val blockData = plugin.akItem.getItem(itemData).type.createBlockData()
            blockEntity.block = blockData
            blockEntity.persistentDataContainer.set(
                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
                PersistentDataType.INTEGER,
                itemData
            )
            promiseBlock(blockEntity, getTransformation(Vector3f(0.99f, 0.98f, 0.99f)))
        }
        return entity
    }

    fun onRebarPlace(block: Block, rebarData: Int): Boolean {
        if (rebarData !in REBARS) return false

        val world = block.world
        val location = block.location
        val rebar = spawnBlockDisplay(block, rebarData)
//        val rebar = world.spawn(location, BlockDisplay::class.java) { blockEntity ->
//            val blockData = plugin.akItem.getItem(rebarData).type.createBlockData()
//            updateRebarWall(block, blockData, Wall.Height.LOW)
//            blockEntity.block = blockData
//            blockEntity.persistentDataContainer.set(
//                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
//                PersistentDataType.INTEGER,
//                rebarData
//            )
//            promiseBlock(blockEntity, getTransformation(Vector3f(1f, 1f, 1f)))
//        }
        rebar.block = updateWall(block, rebar.block, AKStorage.REBARS, Wall.Height.LOW)

        val rebarInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.95f
            interact.interactionHeight = if (rebarData == AKItemType.REBAR_SLAB) 0.2f else 0.95f
        }

        val data = JsonObject()
        data.addProperty("data", rebarData.toString())
        data.addProperty("model", rebar.uniqueId.toString())
        data.addProperty("interaction", rebarInteraction.uniqueId.toString())

        plugin.storage.setData(AKStorage.REBARS, location.toBlockLocation().toString(), data)
        return true
    }

    fun onPipePlace(block: Block, pipeData: Int): Boolean {
        if (pipeData !in PIPES) return false

        val world = block.world
        val location = block.location
        val pipe = spawnBlockDisplay(block, pipeData)
        pipe.block = updateWall(block, pipe.block, AKStorage.PIPES, Wall.Height.LOW)

        val pipeInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.3f
            interact.interactionHeight = 1f
        }

        val data = JsonObject()
        data.addProperty("data", pipeData.toString())
        data.addProperty("model", pipe.uniqueId.toString())
        data.addProperty("interaction", pipeInteraction.uniqueId.toString())

        plugin.storage.setData(AKStorage.PIPES, location.toBlockLocation().toString(), data)
        return true
    }

    fun onFramePlace(block: Block, frameData: Int): Boolean {
        if (frameData !in STEEL_FRAMES) return false

        val world = block.world
        val location = block.location
        val frame = spawnBlockDisplay(block, frameData)
        frame.block = updateWall(block, frame.block, AKStorage.STEEL_FRAMES, Wall.Height.LOW)

        val pipeInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.5f
            interact.interactionHeight = 1f
        }

        val data = JsonObject()
        data.addProperty("data", frameData.toString())
        data.addProperty("model", frame.uniqueId.toString())
        data.addProperty("interaction", pipeInteraction.uniqueId.toString())

        plugin.storage.setData(AKStorage.STEEL_FRAMES, location.toBlockLocation().toString(), data)
        return true
    }

    fun onBreak(entity: Entity, data: JsonElement) {
        val akItemData = data.asJsonObject.get("data").asString.toIntOrNull()

        val location = entity.location.toBlockLocation().toString()
        for ((key, value) in data.asJsonObject.entrySet()) {
            if (key == "data") continue
            val uuid = value.asString
            val model = entity.world.getEntity(UUID.fromString(uuid))
            val block = entity.world.getBlockAt(entity.location)
            if (model as? BlockDisplay != null) {
                when (akItemData) {
                    in REBARS -> updateWall(block, model.block, AKStorage.REBARS, Wall.Height.NONE)
                    in STEEL_FRAMES -> updateWall(block, model.block, AKStorage.STEEL_FRAMES, Wall.Height.NONE)
                    in PIPES -> updateWall(block, model.block, AKStorage.PIPES, Wall.Height.NONE)
                }
            }
            model?.remove()
        }

        val type: String = when (akItemData) {
            in REBARS -> AKStorage.REBARS
            in PIPES -> AKStorage.PIPES
            in STEEL_FRAMES -> AKStorage.STEEL_FRAMES
            else -> ""
        }
        plugin.storage.removeData(type, location)
    }
}