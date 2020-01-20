package com.github.bryanser.twinking

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemInfo(config: ConfigurationSection) {

    constructor(map: Map<String, *>) : this(toConfig(map))

    val id = config.getInt("id")
    val data: Short = config.getInt("data", 0).toShort()
    val name: String? = config.getString("name")?.let { ChatColor.translateAlternateColorCodes('&', it) }
    val lore: List<String>? = config.getStringList("lore")?.map {
        ChatColor.translateAlternateColorCodes('&', it)
    }



    fun getItem(p: Player): ItemStack {
        val item = ItemStack(id, 1, data)
        val im = item.itemMeta
        im.displayName = name
        im.lore = this.lore?.map {
            PlaceholderAPI.setPlaceholders(p, it)
        }
        im.isUnbreakable = true
        item.itemMeta = im
        return item
    }

    override fun toString(): String {
        return "ItemInfo(id=$id, data=$data, name=$name, lore=$lore)"
    }

    companion object {
        fun toConfig(map: Map<String, *>, cs: ConfigurationSection? = null): ConfigurationSection {
            var config = cs ?: YamlConfiguration()
            for ((k, v) in map) {
                if (v is Map<*, *>) {
                    val sub = config.createSection(k)
                    toConfig(v as Map<String, *>, sub)
                } else {
                    config.set(k, v)
                }
            }
            return config
        }
    }


}