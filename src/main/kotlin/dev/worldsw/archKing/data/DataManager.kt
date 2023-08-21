package dev.worldsw.archKing.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.worldsw.archKing.ArchKingPlugin
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class DataManager(private val plugin: ArchKingPlugin) {
    private lateinit var data: JsonObject
    private var memory: JsonObject = JsonObject()

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

        if (!data.has(REBARS)) {
            val rebars = JsonObject()
            data.add(REBARS, rebars)
        }
        if (!data.has(CUSTOM_ITEM)) {
            val rebars = JsonObject()
            data.add(CUSTOM_ITEM, rebars)
        }
    }

    fun addMemory(property: String, inputProperty: String, value: JsonElement) {
        getMemory(property).asJsonObject.add(inputProperty, value)
    }

    fun getMemory(property: String): JsonElement {
        return memory.get(property)
    }

    fun getMemory(property: String, secondProperty: String): JsonElement? {
        return memory.get(property).asJsonObject?.get(secondProperty)
    }

    fun removeMemory(property: String, removeProperty: String) {
        getMemory(property).asJsonObject.remove(removeProperty)
    }

    fun addData(property: String, inputProperty: String, value: JsonElement) {
        getData(property).asJsonObject.add(inputProperty, value)
    }

    fun getData(property: String): JsonElement {
        return data.get(property)
    }

    fun getData(property: String, secondProperty: String): JsonElement? {
        return data.get(property).asJsonObject?.get(secondProperty)
    }

    fun removeData(property: String, removeProperty: String) {
        getData(property).asJsonObject.remove(removeProperty)
    }

    fun saveData() {
        val dataFile = File(plugin.dataFolder, "data.json")
        FileOutputStream(dataFile).close()
        dataFile.outputStream().use { fileOutputStream ->
            fileOutputStream.write(data.toString().toByteArray(Charset.forName("UTF-8")))
        }
    }
}