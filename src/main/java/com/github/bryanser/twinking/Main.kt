package com.github.bryanser.twinking

import com.github.bryanser.brapi.kview.KViewHandler
import com.github.bryanser.twinking.status.Status
import com.github.bryanser.twinking.view.TwinkingViewContext
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        Plugin = this
        Status.loadConfig()
        Sequence.loadConfig()
    }

    override fun onDisable() {
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.isOp && sender is Player) {
            KViewHandler.openUI(sender, TwinkingViewContext.view)
            return true
        }
        if(args.isNotEmpty() && args[0].equals("reload",true) && sender.isOp){
            Status.loadConfig()
            Sequence.loadConfig()
            sender.sendMessage("§6重载完成")
            return true
        }
        if(args.isEmpty()&& sender is Player){
            KViewHandler.openUI(sender, TwinkingViewContext.view)
            return true
        }

        return false
    }

    companion object {
        lateinit var Plugin: Main
    }
}