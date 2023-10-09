package dev.worldsw.archKing.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import dev.worldsw.archKing.ArchKingPlugin
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset


class AKStorage(private val plugin: ArchKingPlugin) {
    private lateinit var data: JsonObject
    private var memory: JsonObject = JsonObject()

    private lateinit var overlapBlockData: HashMap<String, HashMap<String, HashMap<String, String>>>
    private lateinit var blockData: HashMap<String, HashMap<String, String>>
    private lateinit var gravityData: HashMap<String, Boolean>

    companion object {
        @Deprecated("An old key", ReplaceWith("ARCHKING_BLOCK"))
        const val CUSTOM_ITEM = "custom_item"

        const val GRAVITY = "gravity"
        const val WOOD_GRAVITY = "gravity_wood"
        const val CONCRETE_GRAVITY = "gravity_concrete"
        val GRAVITY_TYPES = listOf(WOOD_GRAVITY, CONCRETE_GRAVITY)

        const val ARCHKING_BLOCK = "archking_block"
        const val OVERLAP_ARCHKING_BLOCK = "overlap_archking_block"

        const val REBARS = "rebars"
        const val PIPES = "pipes"
        const val STEEL_FRAMES = "steel_frames"
        val OVERLAP_ARCHKING_BLOCKS = listOf(REBARS, PIPES, STEEL_FRAMES)

        const val READY_MIXED_CONCRETE_HARD = "rmc_hard"
    }

    private fun makeNewFile(file: File) {
        file.createNewFile()
        file.outputStream().use { fileOutputStream ->
            plugin.getResource("data.json")?.copyTo(fileOutputStream)
        }
    }

    @SuppressWarnings("deprecation")
    fun init() {
        val dataFolder = plugin.dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val dataFile = File(dataFolder, "data.json")
        if (!dataFile.exists()) makeNewFile(dataFile)
        if (dataFile.reader().readText() == "") makeNewFile(dataFile)
        data = JsonParser.parseReader(dataFile.reader(Charset.forName("UTF-8"))).asJsonObject

        if (!data.has(ARCHKING_BLOCK)) data.add(ARCHKING_BLOCK, JsonObject())
        if (!data.has(OVERLAP_ARCHKING_BLOCK)) data.add(OVERLAP_ARCHKING_BLOCK, JsonObject())
        if (!data.has(GRAVITY)) data.add(GRAVITY, JsonObject())

        if (!memory.has(READY_MIXED_CONCRETE_HARD)) memory.add(READY_MIXED_CONCRETE_HARD, JsonObject())

        if (data.has(CUSTOM_ITEM)) {
            val items = data.get(CUSTOM_ITEM)
            data.add(ARCHKING_BLOCK, items)
            data.remove(CUSTOM_ITEM)
        }

        for (block in OVERLAP_ARCHKING_BLOCKS) {
            if (!data.get(OVERLAP_ARCHKING_BLOCK).asJsonObject.has(block)) data.get(OVERLAP_ARCHKING_BLOCK)
                .asJsonObject.add(block, JsonObject())
        }

        for (gravity in GRAVITY_TYPES) {
            if (!data.get(GRAVITY).asJsonObject.has(gravity)) data.get(GRAVITY)
                .asJsonObject.addProperty(gravity, false)
        }

        overlapBlockData = Gson().fromJson(
            data.get(OVERLAP_ARCHKING_BLOCK),
            object : TypeToken<HashMap<String, HashMap<String, HashMap<String, String>>>>() {}.type)
        blockData = Gson().fromJson(data.get(ARCHKING_BLOCK), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
        gravityData = Gson().fromJson(data.get(GRAVITY), object : TypeToken<HashMap<String, Boolean>>() {}.type)
    }

    fun saveData() {
        val dataFile = File(plugin.dataFolder, "data.json")
        FileOutputStream(dataFile).close()

        data.asJsonObject.remove(OVERLAP_ARCHKING_BLOCK)
        data.asJsonObject.remove(ARCHKING_BLOCK)
        data.asJsonObject.remove(GRAVITY)
        data.add(OVERLAP_ARCHKING_BLOCK, Gson().toJsonTree(overlapBlockData))
        data.add(ARCHKING_BLOCK, Gson().toJsonTree(blockData))
        data.add(GRAVITY, Gson().toJsonTree(gravityData))

        dataFile.outputStream().use { fileOutputStream ->
            fileOutputStream.write(data.toString().toByteArray(Charset.forName("UTF-8")))
        }
    }

    /**
     * Memory
     */
    fun addMemory(property: String, inputProperty: String, value: JsonElement) {
        getMemory(property).asJsonObject.add(inputProperty, value)
    }

    private fun getMemory(property: String): JsonElement {
        return memory.get(property)
    }

    fun getMemory(property: String, secondProperty: String): JsonElement? {
        return memory.get(property).asJsonObject?.get(secondProperty)
    }

    fun removeMemory(property: String, removeProperty: String) {
        getMemory(property).asJsonObject.remove(removeProperty)
    }


    /**
     * Data
     */
    fun setData(property: String, inputProperty: String, value: JsonElement) {
        when (property) {
            in OVERLAP_ARCHKING_BLOCKS -> overlapBlockData[property]!![inputProperty] =
                Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            ARCHKING_BLOCK -> blockData[inputProperty] =
                Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            GRAVITY -> gravityData[inputProperty] =
                Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            else -> getData(property).asJsonObject.add(inputProperty, value)
        }
    }

    fun setData(property: String, inputProperty: String, value: Boolean) {
        when (property) {
            GRAVITY -> gravityData[inputProperty] = value
        }
    }

    private fun getData(property: String): JsonElement {
        return data.get(property)
    }

    fun getData(property: String, secondProperty: String): JsonElement? {
        return when (property) {
            in OVERLAP_ARCHKING_BLOCKS -> {
                val data = overlapBlockData[property]!![secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            ARCHKING_BLOCK -> {
                val data = blockData[secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            GRAVITY -> {
                val data = gravityData[secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            else -> data.get(property).asJsonObject?.get(secondProperty)
        }
    }

    fun removeData(property: String, removeProperty: String) {
        when (property) {
            in OVERLAP_ARCHKING_BLOCKS -> overlapBlockData[property]!!.remove(removeProperty)
            ARCHKING_BLOCK -> blockData.remove(removeProperty)
            GRAVITY -> gravityData.remove(removeProperty)
            else -> getData(property).asJsonObject.remove(removeProperty)
        }
    }
}