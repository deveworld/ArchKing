package dev.worldsw.archKing.rebar

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import dev.worldsw.archKing.item.AKItem
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Fence
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

class AKRebar(private val plugin: ArchKingPlugin) {
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

    fun onPlaceRebar(block: Block, rebarData: Int): Boolean {
        if (plugin.storage.getData(AKStorage.REBARS, block.location.toBlockLocation().toString()) != null) return false

        val world = block.world
        val location = block.location
        val rebar = world.spawn(location, BlockDisplay::class.java) { rebarBlock ->
            val blockData = plugin.akItem.getItem(rebarData).type.createBlockData()
            rebarBlock.block = blockData
            rebarBlock.persistentDataContainer.set(
                NamespacedKey(plugin, AKItem.CUSTOM_ITEM),
                PersistentDataType.INTEGER,
                rebarData
            )
            promiseBlock(rebarBlock, getTransformation(Vector3f(1f, 0.95f, 1f)))
        }

        val rebarInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 1f
            interact.interactionHeight = 0.5f
        }

        val data = JsonObject()
        data.addProperty("data", rebarData.toString())
        data.addProperty("rebar", rebar.uniqueId.toString())
        data.addProperty("rebarInteraction", rebarInteraction.uniqueId.toString())

        plugin.storage.addData(AKStorage.REBARS, location.toBlockLocation().toString(), data)
        return true
    }

    fun onBreakRebar(entity: Entity, data: JsonElement) {
        val location = entity.location.toBlockLocation().toString()
        val rebarModelUUID = data.asJsonObject.get("rebar").asString
        val rebarInteractionUUID = data.asJsonObject.get("rebarInteraction").asString
        entity.world.getEntity(UUID.fromString(rebarModelUUID))!!.remove()
        entity.world.getEntity(UUID.fromString(rebarInteractionUUID))!!.remove()

        plugin.storage.removeData(AKStorage.REBARS, location)
    }
}