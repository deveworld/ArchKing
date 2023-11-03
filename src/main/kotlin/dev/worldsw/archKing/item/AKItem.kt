package dev.worldsw.archKing.item

import dev.worldsw.archKing.ArchKingPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties

class AKItem(private val plugin: ArchKingPlugin) {


    companion object {
        const val CUSTOM_ITEM           = "ArchKingItem"
        const val NOT_CUSTOM_ITEM       = 0
    }

    private val items = HashMap<Int, ItemStack>()
    fun getItem(item:Int, quantity:Int = 1):ItemStack{
        return items[item]?.asQuantity(quantity)?:ItemStack(Material.AIR)
    }

    fun getAllProperties(): List<String> {
        val propertyNames = mutableListOf<String>()
        val companion = AKItemType::class.companionObject
        if (companion != null) {
            val properties = companion.memberProperties
            for (property in properties) {
                if (property.isConst) {
                    propertyNames.add(property.name.lowercase())
                }
            }
        }
        return propertyNames.toList()
    }

    fun getId(name: String): Int? {
        val companion = AKItemType::class.companionObject
        if (companion != null) {
            val properties = companion.memberProperties
            for (property in properties) {
                if (property.isConst) {
                    if (name.equals(property.name, true)) {
                        return property.getter.call(companion) as Int
                    }
                }
            }
        }
        return null
    }

    init {
        listOf(
            Triple(Material.SUGAR, "석회", AKItemType.LIME),
            Triple(Material.GUNPOWDER, "시멘트 가루", AKItemType.CEMENT_POWDER),
            Triple(Material.LIGHT_GRAY_CONCRETE_POWDER, "시멘트 클린커", AKItemType.CEMENT_CLINKER),
            Triple(Material.WHITE_CONCRETE_POWDER, "시멘트", AKItemType.CEMENT),
            Triple(Material.GRAY_CONCRETE_POWDER, "레미콘", AKItemType.READY_MIXED_CONCRETE),
            Triple(Material.RED_CONCRETE_POWDER, "피가 묻은 시멘트", AKItemType.RED_CEMENT),
            Triple(Material.AMETHYST_SHARD, "석고", AKItemType.GYPSUM),
            Triple(Material.LIGHT_GRAY_CONCRETE, "콘크리트", AKItemType.CONCRETE),
            Triple(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, "철근", AKItemType.REBAR), // ItemFlag Hide ITEM
            Triple(Material.RED_NETHER_BRICK_WALL, "철근 기둥", AKItemType.REBAR_PILLAR),
            Triple(Material.RED_NETHER_BRICK_WALL, "철근 보", AKItemType.REBAR_BEAM),
            Triple(Material.MAGENTA_CANDLE, "철근 슬래브", AKItemType.REBAR_SLAB),
            Triple(Material.MUD_BRICK_WALL, "강관", AKItemType.PIPE),
            Triple(Material.NETHER_BRICK_WALL, "철골", AKItemType.STEEL_FRAME),
            Triple(Material.IRON_TRAPDOOR, "데크플레이트", AKItemType.DECK_PLATE)
        ).forEach{
            items[it.third] = ItemStack(it.first).apply {
                editMeta {meta ->
                    meta.displayName(Component.text(it.second).decoration(TextDecoration.ITALIC, false))
                    meta.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                        it.third
                    )

                }
            }
        }
        items[AKItemType.REBAR]!!.editMeta{it.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)}


    }
}