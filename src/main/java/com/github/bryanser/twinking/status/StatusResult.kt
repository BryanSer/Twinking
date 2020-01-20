package com.github.bryanser.twinking.status

import com.github.bryanser.twinking.ItemInfo
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

class StatusResult(map: Map<String, *>, val variables: MutableMap<String, Variable>) {
    val weight: Int = (map.get("weight") as Number).toInt()
    val result = ItemInfo(map.get("result") as Map<String, *>)

    init {
        val lv = map.get("locVariable") as Map<String, String>
        for ((k, v) in lv) {
            variables += ("<" + k + ">") to Variable(v)
        }
    }

    fun createLoreWithName(p: Player): Pair<List<String>, String> {
        return (result.lore?.map {
            replace(it, p)
        } ?: mutableListOf()) to replace(result.name ?: "", p)
    }

    fun replace(str: String, p: Player): String {
        var s = str
        for ((k, v) in variables) {
            if (s.contains(k)) {
                s = s.replace(k, v(p))
            }
        }
        s = PlaceholderAPI.setPlaceholders(p, s)
        return s
    }
}