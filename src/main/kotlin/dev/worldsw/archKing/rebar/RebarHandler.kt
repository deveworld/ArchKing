package dev.worldsw.archKing.rebar

import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.DataManager
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

class RebarHandler(private val plugin: ArchKingPlugin) {
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

    fun onPlaceRebar(block: Block) {
        if (plugin.dataManager.getData(DataManager.REBARS, block.location.toBlockLocation().toString()) != null) return

        val world = block.world
        val location = block.location
        val rebar = world.spawn(location, BlockDisplay::class.java) { rebarBlock ->
            rebarBlock.block = Material.IRON_BARS.createBlockData()
            promiseBlock(rebarBlock, getTransformation(Vector3f(1f, 0.95f, 1f)))
        }

        val rebarInteraction = world.spawn(location.clone().add(0.5, 0.0, 0.5), Interaction::class.java) { interact ->
            interact.interactionWidth = 0.2f
            interact.interactionHeight = 1f
        }

        val data = JsonObject()
        data.addProperty("rebar", rebar.uniqueId.toString())
        data.addProperty("rebarInteraction", rebarInteraction.uniqueId.toString())

        plugin.dataManager.addData(DataManager.REBARS, location.toBlockLocation().toString(), data)
    }

    fun onBreakRebar(entity: Entity) {
        val location = entity.location.toBlockLocation().toString()
        val data = plugin.dataManager.getData(DataManager.REBARS, location)
        for (model in data!!.asJsonObject.entrySet()) {
            val modelUUID = model.value.asString
            entity.world.getEntity(UUID.fromString(modelUUID))!!.remove()
        }
        plugin.dataManager.removeData(DataManager.REBARS, location)
    }
}