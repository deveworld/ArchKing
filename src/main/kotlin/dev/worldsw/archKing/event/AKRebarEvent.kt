package dev.worldsw.archKing.event

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

class AKRebarEvent(private val plugin: ArchKingPlugin) : Listener {
    /**
     * On Break Rebar
     */
    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        plugin.storage.getData(AKStorage.REBARS, event.entity.location.toBlockLocation().toString()) ?: return

        plugin.akRebar.onBreakRebar(event.entity)

        val player = (event.damager as Player)
        player.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
        if (player.gameMode != GameMode.CREATIVE) player.inventory.addItem(ItemStack(Material.IRON_BARS))
    }

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.block.type == Material.IRON_BARS) {
            event.isCancelled = true
            plugin.akRebar.onPlaceRebar(event.block)
            if (event.player.gameMode != GameMode.CREATIVE) event.player.inventory.removeItem(ItemStack(Material.IRON_BARS))
        }
    }
}