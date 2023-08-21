package dev.worldsw.archKing.recipe

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.item.AKItemType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.StonecuttingRecipe

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

        for (recipe in recipes) {
            Bukkit.addRecipe(recipe)
        }
    }
}