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
    private lateinit var itemData: HashMap<String, HashMap<String, String>>

    companion object {
        const val REBARS = "rebars"
        const val CUSTOM_ITEM = "custom_item"

        const val READY_MIXED_CONCRETE_HARD = "rmc_hard"
    }

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
        if (!data.has(CUSTOM_ITEM)) data.add(CUSTOM_ITEM, JsonObject())
        if (!memory.has(READY_MIXED_CONCRETE_HARD)) memory.add(READY_MIXED_CONCRETE_HARD, JsonObject())

        rebarData = Gson().fromJson(data.get(REBARS), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
        itemData = Gson().fromJson(data.get(CUSTOM_ITEM), object : TypeToken<HashMap<String, HashMap<String, String>>>() {}.type)
    }

    fun saveData() {
        val dataFile = File(plugin.dataFolder, "data.json")
        FileOutputStream(dataFile).close()

        data.asJsonObject.remove(REBARS)
        data.asJsonObject.remove(CUSTOM_ITEM)
        data.add(REBARS, Gson().toJsonTree(rebarData))
        data.add(CUSTOM_ITEM, Gson().toJsonTree(itemData))

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
        if (property == REBARS) {
            rebarData[inputProperty] =
                Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            return
        } else if (property == CUSTOM_ITEM) {
            itemData[inputProperty] =
                Gson().fromJson(value, object : TypeToken<HashMap<String, Any?>?>() {}.type)
            return
        }

        getData(property).asJsonObject.add(inputProperty, value)
    }

    private fun getData(property: String): JsonElement {
        return data.get(property)
    }

    fun getData(property: String, secondProperty: String): JsonElement? {
        if (property == REBARS) {
            val data = rebarData[secondProperty] ?: return null
            return Gson().toJsonTree(data)
        } else if (property == CUSTOM_ITEM) {
            val data = itemData[secondProperty] ?: return null
            return Gson().toJsonTree(data)
        }

        return data.get(property).asJsonObject?.get(secondProperty)
    }

    fun removeData(property: String, removeProperty: String) {
        if (property == REBARS) {
            rebarData.remove(removeProperty)
            return
        } else if (property == CUSTOM_ITEM) {
            itemData.remove(removeProperty)
            return
        }

        getData(property).asJsonObject.remove(removeProperty)
    }
}