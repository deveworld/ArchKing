package dev.worldsw.archKing.recipe

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*

class AKRecipe(plugin: ArchKingPlugin) {
    init {
        val recipes = mutableListOf<Recipe>()

        recipes.add(StonecuttingRecipe(
            NamespacedKey(plugin, "lime_dripstone_three_ak"),
            plugin.akItem.getItem(AKItemType.LIME, 3),
            Material.POINTED_DRIPSTONE
        ))
        recipes.add(StonecuttingRecipe(
            NamespacedKey(plugin, "lime_dripstone_twelve_ak"),
            plugin.akItem.getItem(AKItemType.LIME, 12),
            Material.DRIPSTONE_BLOCK
        ))

        recipes.add(StonecuttingRecipe(
            NamespacedKey(plugin, "lime_calcite_ak"),
            plugin.akItem.getItem(AKItemType.LIME, 9),
            Material.CALCITE
        ))

        val cementPowderRecipe = ShapedRecipe(
            NamespacedKey(plugin, "cement_powder_ak"),
            plugin.akItem.getItem(AKItemType.CEMENT_POWDER, 9)
        )
        cementPowderRecipe.shape("LLL", "LLC", "SSS")
        cementPowderRecipe.setIngredient('L', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.LIME)))
        cementPowderRecipe.setIngredient('C', Material.CLAY_BALL)
        cementPowderRecipe.setIngredient('S', Material.SAND)
        recipes.add(cementPowderRecipe)

        recipes.add(BlastingRecipe(
            NamespacedKey(plugin, "cement_clinker_ak"),
            plugin.akItem.getItem(AKItemType.CEMENT_CLINKER),
            RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.CEMENT_POWDER)),
            1f,
            20*60
        ))

        val cementRecipe = ShapedRecipe(
            NamespacedKey(plugin, "cement_ak"),
            plugin.akItem.getItem(AKItemType.CEMENT, 8)
        )
        cementRecipe.shape("GCC", "CCC", "CCC")
        cementRecipe.setIngredient('G', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.GYPSUM)))
        cementRecipe.setIngredient('C', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.CEMENT_CLINKER)))
        recipes.add(cementRecipe)

        val concreteRecipe = ShapedRecipe(
            NamespacedKey(plugin, "concrete_ak"),
            plugin.akItem.getItem(AKItemType.READY_MIXED_CONCRETE, 8)
        )
        concreteRecipe.shape("WCC", "GGG", "SSS")
        concreteRecipe.setIngredient('W', Material.WATER_BUCKET)
        concreteRecipe.setIngredient('C', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.CEMENT)))
        concreteRecipe.setIngredient('G', Material.GRAVEL)
        concreteRecipe.setIngredient('S', Material.COBBLESTONE)
        recipes.add(concreteRecipe)

        val ancientRecipe = ShapelessRecipe(
            NamespacedKey(plugin, "red_concrete_ak"),
            plugin.akItem.getItem(AKItemType.RED_CEMENT)
        )
        ancientRecipe.addIngredient(RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.CEMENT)))
        ancientRecipe.addIngredient(Material.ROTTEN_FLESH)
        recipes.add(ancientRecipe)

        val rebarRecipe = ShapedRecipe(
            NamespacedKey(plugin, "rebar_ak"),
            plugin.akItem.getItem(AKItemType.REBAR, 4)
        )
        rebarRecipe.shape("   ", " I ", " I ")
        rebarRecipe.setIngredient('I', Material.IRON_INGOT)
        recipes.add(rebarRecipe)

        val steelFrameRecipe = ShapedRecipe(
            NamespacedKey(plugin, "steel_frame_ak"),
            plugin.akItem.getItem(AKItemType.STEEL_FRAME, 2)
        )
        steelFrameRecipe.shape("III", " I ", "III")
        steelFrameRecipe.setIngredient('I', Material.IRON_INGOT)
        recipes.add(steelFrameRecipe)

        val rebarPillarRecipe = ShapedRecipe(
            NamespacedKey(plugin, "rebar_pillar_ak"),
            plugin.akItem.getItem(AKItemType.REBAR_PILLAR, 2)
        )
        rebarPillarRecipe.shape("I I", "III", "I I")
        rebarPillarRecipe.setIngredient('I', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.REBAR)))
        recipes.add(rebarPillarRecipe)

        val rebarBeamRecipe = ShapedRecipe(
            NamespacedKey(plugin, "rebar_beam_ak"),
            plugin.akItem.getItem(AKItemType.REBAR_BEAM, 2)
        )
        rebarBeamRecipe.shape("III", " I ", "III")
        rebarBeamRecipe.setIngredient('I', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.REBAR)))
        recipes.add(rebarBeamRecipe)

        val rebarSlabRecipe = ShapedRecipe(
            NamespacedKey(plugin, "rebar_slab_ak"),
            plugin.akItem.getItem(AKItemType.REBAR_SLAB, 12)
        )
        rebarSlabRecipe.shape("III", "III", "III")
        rebarSlabRecipe.setIngredient('I', RecipeChoice.ExactChoice(plugin.akItem.getItem(AKItemType.REBAR)))
        recipes.add(rebarSlabRecipe)

        val pipeRecipe = ShapedRecipe(
            NamespacedKey(plugin, "pipe_ak"),
            plugin.akItem.getItem(AKItemType.PIPE, 8)
        )
        pipeRecipe.shape("III", "I I", "III")
        pipeRecipe.setIngredient('I', Material.IRON_INGOT)
        recipes.add(pipeRecipe)

        val deckPlateRecipe = ShapedRecipe(
            NamespacedKey(plugin, "deck_plate_ak"),
            plugin.akItem.getItem(AKItemType.DECK_PLATE, 3)
        )
        deckPlateRecipe.shape("   ", "   ", "III")
        deckPlateRecipe.setIngredient('I', Material.IRON_INGOT)
        recipes.add(deckPlateRecipe)

        for (recipe in recipes) {
            Bukkit.addRecipe(recipe)
        }
    }
}