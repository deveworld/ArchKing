package dev.worldsw.archKing.command

import dev.worldsw.archKing.ArchKingPlugin
import dev.worldsw.archKing.data.AKStorage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AKCommand(private val plugin: ArchKingPlugin): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Please use this command in-game.").color(TextColor.color(255, 100, 100)))
            return true
        }

        val getAkiCommands = listOf("akitem")
        if (label in getAkiCommands) return getAKItem(sender, args)

        if (label == "akgravity") return gravity(sender, args)

        return false
    }

    private fun getAKItem(sender: Player, args: Array<out String>?): Boolean {
        if (args?.size != 2) return false

        try {
            val itemId = plugin.akItem.getId(args[0])
            if (itemId == null) {
                sender.sendMessage(Component.text("Unknown Item.").color(TextColor.color(255, 100, 100)))
                return true
            }
            val item = plugin.akItem.getItem(itemId, args[1].toInt())
            sender.inventory.addItem(item)
        } catch (e: NumberFormatException) {
            sender.sendMessage(Component.text("Please write all in integer.").color(TextColor.color(255, 100, 100)))
        }
        return true
    }

    private fun gravity(sender: Player, args: Array<out String>?): Boolean {
        if (args?.size != 1) return false

        when (args[0]) {
            "wood" -> plugin.storage.setData(
                AKStorage.GRAVITY,
                AKStorage.WOOD_GRAVITY,
                !plugin.storage.getData(AKStorage.GRAVITY, AKStorage.WOOD_GRAVITY)!!.asBoolean
            )
            "concrete" -> plugin.storage.setData(
                AKStorage.GRAVITY,
                AKStorage.CONCRETE_GRAVITY,
                !plugin.storage.getData(AKStorage.GRAVITY, AKStorage.CONCRETE_GRAVITY)!!.asBoolean
            )
            else -> return false
        }
        plugin.akBlock.renderGravity(sender)

        return true
    }
}