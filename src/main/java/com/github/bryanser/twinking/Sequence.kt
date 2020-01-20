package com.github.bryanser.twinking

import com.github.bryanser.brapi.Utils
import com.github.bryanser.twinking.status.Status
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

class Sequence(config: ConfigurationSection) {
    constructor(file: File) : this(YamlConfiguration.loadConfiguration(file))

    val name = config.getString("Setting.name")

    val item = ItemInfo(config.getConfigurationSection("Setting.item"))

    val entries = mutableListOf<Entry>()

    val findMap = mutableMapOf<String, MutableSet<Entry>>()

    init {
        val list = config.getMapList("Sequences")
        for (map in list) {
            entries += Entry(map as Map<String, String>)
        }
        for (e in entries) {
            findMap.getOrPut(e.depend) { mutableSetOf() }.add(e)
        }
    }

    inner class Entry(map: Map<String, String>) {
        val depend = map["depend"] ?: error("配置缺少依赖")
        val key = map["key"] ?: error("配置缺少key")

        val isStart: Boolean by lazy {
            depend.equals("start", true)
        }
        val status:Status = Status.status[key]?: throw IllegalArgumentException("缺少key指向: $key")
    }


    companion object {

        const val DEBUG = false

        val sequences = mutableMapOf<String, Sequence>()

        fun findSequence(item: ItemStack): Sequence? {
            if(DEBUG){
                Bukkit.broadcastMessage("§6开始查询物品序列: $item")
            }
            val im = item.itemMeta
            for (seq in sequences.values) {
                if(DEBUG){
                    Bukkit.broadcastMessage("§b即将匹配的物品: ${seq.item}")
                }
                if (seq.item.id != item.typeId) {
                    continue
                }
                if (seq.item.data != item.durability) {
                    continue
                }
                if (seq.item.name!!.replace("§.".toRegex(),"").equals(im.displayName!!.replace("§.".toRegex(),""))) {
                    return seq
                }
            }
            return null
        }

        fun loadConfig() {
            sequences.clear()
            val folder = File(Main.Plugin.dataFolder, "/Sequence/")
            if (!folder.exists()) {
                folder.mkdirs()
                Utils.saveResource(Main.Plugin, "example_sequence.yml", folder)
            }
            load(folder)
        }

        private fun load(file: File) {
            if (file.isDirectory) {
                for (f in file.listFiles()) {
                    load(f)
                }
                return
            }
            val s = Sequence(file)
            sequences[s.name] = s
        }
    }
}