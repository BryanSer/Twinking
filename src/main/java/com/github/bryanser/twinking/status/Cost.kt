package com.github.bryanser.twinking.status

import com.github.bryanser.brapi.Utils
import io.lumine.xikage.mythicmobs.MythicMobs
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Cost(config: ConfigurationSection) {
    val money = config.getDouble("money", 0.0)
    val items: List<Supplier<ItemStack>> = config.getStringList("items")?.map {
        if (it.startsWith("mm:")) {
            val str = it.replaceFirst("mm:", "").split("*")
            var amount = 1
            if (str.size >= 2) {
                amount = str[1].toInt()
            }
            val item = MythicMobs.inst().itemManager.getItemStack(str[0])?.clone()
                    ?: throw IllegalArgumentException("找不到MM物品:${str[0]}")
            item.amount = amount
            return@map Supplier {
                item.clone()
            }
        }
        val item = Utils.readItemStack(it)
        Supplier {
            item.clone()
        }
    } ?: listOf()
}