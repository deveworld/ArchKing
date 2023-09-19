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
    private var lime: ItemStack = ItemStack(Material.SUGAR); private var cementPowder: ItemStack
    private var cementClinker: ItemStack; private var cement: ItemStack
    private var readyMixedConcrete: ItemStack; private var redCement: ItemStack
    private var gypsum: ItemStack; private var concrete: ItemStack

    private var rebar: ItemStack; private var rebarPillar: ItemStack
    private var rebarBeam: ItemStack; private var rebarSlab: ItemStack
    private var steelFrame: ItemStack; private var deckPlate: ItemStack

    private var pipe: ItemStack

    companion object {
        const val CUSTOM_ITEM           = "ArchKingItem"
        const val NOT_CUSTOM_ITEM       = 0
    }

    fun getItem(item: Int, quantity: Int = 1): ItemStack {
        return when (item) {
            AKItemType.LIME ->                     lime.asQuantity(quantity)
            AKItemType.CEMENT_POWDER ->            cementPowder.asQuantity(quantity)
            AKItemType.CEMENT_CLINKER ->           cementClinker.asQuantity(quantity)
            AKItemType.CEMENT ->                   cement.asQuantity(quantity)
            AKItemType.READY_MIXED_CONCRETE ->     readyMixedConcrete.asQuantity(quantity)
            AKItemType.RED_CEMENT ->               redCement.asQuantity(quantity)
            AKItemType.GYPSUM ->                   gypsum.asQuantity(quantity)
            AKItemType.CONCRETE ->                 concrete.asQuantity(quantity)

            AKItemType.REBAR ->                    rebar.asQuantity(quantity)
            AKItemType.STEEL_FRAME ->              steelFrame.asQuantity(quantity)
            AKItemType.REBAR_PILLAR ->             rebarPillar.asQuantity(quantity)
            AKItemType.REBAR_BEAM ->               rebarBeam.asQuantity(quantity)
            AKItemType.REBAR_SLAB ->               rebarSlab.asQuantity(quantity)
            AKItemType.DECK_PLATE ->               deckPlate.asQuantity(quantity)

            AKItemType.PIPE ->                     pipe.asQuantity(quantity)
            else ->                                ItemStack(Material.AIR)
        }
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
        lime = ItemStack(Material.SUGAR)
        lime.editMeta {
            it.displayName(Component.text("석회").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.LIME
            )
        }

        cementPowder = ItemStack(Material.GUNPOWDER)
        cementPowder.editMeta {
            it.displayName(Component.text("시멘트 가루").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.CEMENT_POWDER
            )
        }

        cementClinker = ItemStack(Material.LIGHT_GRAY_CONCRETE_POWDER)
        cementClinker.editMeta {
            it.displayName(Component.text("시멘트 클린커").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.CEMENT_CLINKER
            )
        }

        cement = ItemStack(Material.WHITE_CONCRETE_POWDER)
        cement.editMeta {
            it.displayName(Component.text("시멘트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.CEMENT
            )
        }

        readyMixedConcrete = ItemStack(Material.GRAY_CONCRETE_POWDER)
        readyMixedConcrete.editMeta {
            it.displayName(Component.text("레미콘").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.READY_MIXED_CONCRETE
            )
        }

        redCement = ItemStack(Material.RED_CONCRETE_POWDER)
        redCement.editMeta {
            it.displayName(Component.text("피가 묻은 시멘트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.RED_CEMENT
            )
        }

        gypsum = ItemStack(Material.AMETHYST_SHARD)
        gypsum.editMeta {
            it.displayName(Component.text("석고").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.GYPSUM
            )
        }

        concrete = ItemStack(Material.LIGHT_GRAY_CONCRETE)
        concrete.editMeta {
            it.displayName(Component.text("콘크리트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.CONCRETE
            )
        }

        rebar = ItemStack(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
        rebar.editMeta {
            it.displayName(Component.text("철근").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.REBAR
            )
            it.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)
        }

        rebarPillar = ItemStack(Material.RED_NETHER_BRICK_WALL)
        rebarPillar.editMeta {
            it.displayName(Component.text("철근 기둥").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.REBAR_PILLAR
            )
        }

        rebarBeam = ItemStack(Material.RED_NETHER_BRICK_WALL)
        rebarBeam.editMeta {
            it.displayName(Component.text("철근 보").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.REBAR_BEAM
            )
        }

        rebarSlab = ItemStack(Material.MAGENTA_CANDLE)
        rebarSlab.editMeta {
            it.displayName(Component.text("철근 슬래브").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.REBAR_SLAB
            )
        }

        pipe = ItemStack(Material.MUD_BRICK_WALL)
        pipe.editMeta {
            it.displayName(Component.text("강관").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.PIPE
            )
        }

        steelFrame = ItemStack(Material.NETHER_BRICK_WALL)
        steelFrame.editMeta {
            it.displayName(Component.text("철골").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.STEEL_FRAME
            )
        }

        deckPlate = ItemStack(Material.IRON_TRAPDOOR)
        deckPlate.editMeta {
            it.displayName(Component.text("데크플레이트").decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(NamespacedKey(plugin, CUSTOM_ITEM), PersistentDataType.INTEGER,
                AKItemType.DECK_PLATE
            )
        }
    }
}