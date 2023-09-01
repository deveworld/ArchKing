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

    private lateinit var rebarData: HashMap<String, HashMap<String, String>>
    private lateinit var pipeData: HashMap<String, HashMap<String, String>>
    private lateinit var blockData: HashMap<String, HashMap<String, String>>

    companion object {
        const val REBARS = "rebars"
        const val PIPES = "pipes"

        @Deprecated("An old key", ReplaceWith("ARCHKING_BLOCK"))
        const val CUSTOM_ITEM = "custom_item"

        const val ARCHKING_BLOCK = "archking_block"

        const val READY_MIXED_CONCRETE_HARD = "rmc_hard"
    }

    @SuppressWarnings("deprecation")
    fun init() {
        val dataFolder = plugin.dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val dataFile = File(dataFolder, "data.json")
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            dataFile.outputStream().use { fileOutputStream ->
                plugin.getResource("data.json")?.copyTo(fileOutputStream)
            }
        }
        data = JsonParser.parseReader(dataFile.reader(Charset.forName("UTF-8"))).asJsonObject

        if (!data.has(REBARS)) data.add(REBARS, JsonObject())
        if (!data.has(PIPES)) data.add(PIPES, JsonObject())
        if (!data.has(ARCHKING_BLOCK)) data.add(ARCHKING_BLOCK, JsonObject())
        if (!memory.has(READY_MIXED_CONCRETE_HARD)) memory.add(READY_MIXED_CONCRETE_HARD, JsonObject())

        if (data.has(CUSTOM_ITEM)) {
            val items = data.get(CUSTOM_ITEM)
            data.add(ARCHKING_BLOCK, items)
            data.remove(CUSTOM_ITEM)
        }

        rebarData = Gson().fromJson(data.get(REBARS), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
        pipeData = Gson().fromJson(data.get(PIPES), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
        blockData = Gson().fromJson(data.get(ARCHKING_BLOCK), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
    }

    fun saveData() {
        val dataFile = File(plugin.dataFolder, "data.json")
        FileOutputStream(dataFile).close()

        data.asJsonObject.remove(REBARS)
        data.asJsonObject.remove(PIPES)
        data.asJsonObject.remove(ARCHKING_BLOCK)
        data.add(REBARS, Gson().toJsonTree(rebarData))
        data.add(PIPES, Gson().toJsonTree(pipeData))
        data.add(ARCHKING_BLOCK, Gson().toJsonTree(blockData))

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
    fun addData(property: String, inputProperty: String, value: JsonElement) {
        when (property) {
            REBARS -> rebarData[inputProperty] =
                    Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            PIPES -> pipeData[inputProperty] =
                    Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            ARCHKING_BLOCK -> blockData[inputProperty] =
                    Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            else -> getData(property).asJsonObject.add(inputProperty, value)
        }
    }

    private fun getData(property: String): JsonElement {
        return data.get(property)
    }

    fun getData(property: String, secondProperty: String): JsonElement? {
        return when (property) {
            REBARS -> {
                val data = rebarData[secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            PIPES -> {
                val data = pipeData[secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            ARCHKING_BLOCK -> {
                val data = blockData[secondProperty] ?: return null
                Gson().toJsonTree(data)
            }
            else -> data.get(property).asJsonObject?.get(secondProperty)
        }
    }

    fun removeData(property: String, removeProperty: String) {
        when (property) {
            REBARS -> rebarData.remove(removeProperty)
            PIPES -> pipeData.remove(removeProperty)
            ARCHKING_BLOCK -> blockData.remove(removeProperty)
            else -> getData(property).asJsonObject.remove(removeProperty)
        }
    }
}