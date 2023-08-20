package dev.worldsw.archKing.item

import com.google.gson.JsonObject
import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.DataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ArchKingItem(private val plugin: ArchKingPlugin) {
    private var lime: ItemStack = ItemStack(Material.SUGAR); private var cementPowder: ItemStack
    private var cementClinker: ItemStack; private var cement: ItemStack
    private var readyMixedConcrete: ItemStack; private var redCement: ItemStack
    private var gypsum: ItemStack; private var concrete: ItemStack

    companion object {
        const val CUSTOM_ITEM           = "ArchKingItem"

        const val NOT_CUSTOM_ITEM       = 0

        const val LIME                  = 1
        const val CEMENT_POWDER         = 2
        const val CEMENT_CLINKER        = 3
        const val CEMENT                = 4
        const val READY_MIXED_CONCRETE  = 5
        const val GYPSUM                = 6
        const val CONCRETE              = 7
        const val RED_CEMENT            = 99
    }

    fun getItem(item: Int, quantity: Int = 1): ItemStack {
        return when (item) {
            LIME ->                     lime.asQuantity(quantity)
            CEMENT_POWDER ->            cementPowder.asQuantity(quantity)
            CEMENT_CLINKER ->           cementClinker.asQuantity(quantity)
            CEMENT ->                   cement.asQuantity(quantity)
            READY_MIXED_CONCRETE ->     readyMixedConcrete.asQuantity(quantity)
            RED_CEMENT ->               redCement.asQuantity(quantity)
            GYPSUM ->                   gypsum.asQuantity(quantity)
            CONCRETE ->                 concrete.asQuantity(quantity)
            else ->                     ItemStack(Material.AIR)
        }
    }

    fun getCustomBlockData(block: Block): Int? {
        if (!plugin.dataManager.getData(DataManager.CUSTOM_ITEM).asJsonObject.has(block.location.toString())) return null
        return plugin.dataManager.getData(DataManager.CUSTOM_ITEM, block.location.toString())!!.asJsonObject!!.get("data").asInt
    }

    fun addCustomBlockData(block: Block, customBlock: Int) {
        val data = JsonObject()
        data.addProperty("data", customBlock)
        plugin.dataManager.addData(DataManager.CUSTOM_ITEM, block.location.toString(), data)
    }

    fun removeCustomBlockData(block: Block) {
        plugin.dataManager.removeData(DataManager.CUSTOM_ITEM, block.location.toString())
    }

    init {
        lime = ItemStack(Material.SUGAR)
        lime.editMeta {
            it.displayName(Component.text("석회").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, LIME)
        }

        cementPowder = ItemStack(Material.GUNPOWDER)
        cementPowder.editMeta {
            it.displayName(Component.text("시멘트 가루").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, CEMENT_POWDER)
        }

        cementClinker = ItemStack(Material.LIGHT_GRAY_CONCRETE_POWDER)
        cementClinker.editMeta {
            it.displayName(Component.text("시멘트 클린커").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, CEMENT_CLINKER)
        }

        cement = ItemStack(Material.WHITE_CONCRETE_POWDER)
        cement.editMeta {
            it.displayName(Component.text("시멘트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, CEMENT)
        }

        readyMixedConcrete = ItemStack(Material.GRAY_CONCRETE_POWDER)
        readyMixedConcrete.editMeta {
            it.displayName(Component.text("레미콘").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, READY_MIXED_CONCRETE)
        }

        redCement = ItemStack(Material.RED_CONCRETE_POWDER)
        redCement.editMeta {
            it.displayName(Component.text("피가 묻은 시멘트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, RED_CEMENT)
        }

        gypsum = ItemStack(Material.AMETHYST_SHARD)
        gypsum.editMeta {
            it.displayName(Component.text("석고").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, GYPSUM)
        }

        concrete = ItemStack(Material.LIGHT_GRAY_CONCRETE)
        concrete.editMeta {
            it.displayName(Component.text("콘크리트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER, CONCRETE)
        }
    }
}