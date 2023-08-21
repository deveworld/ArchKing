package dev.worldsw.archKing.command

import dev.worldsw.archKing.ArchKingPlugin
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

        val getAkiCommands = listOf("archkingitem")
        if (label in getAkiCommands) return getAKItem(sender, args)

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
            return true
        }
        return true
    }

}