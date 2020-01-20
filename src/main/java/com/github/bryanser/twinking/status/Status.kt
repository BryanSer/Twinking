package com.github.bryanser.twinking.status

import com.github.bryanser.brapi.Utils
import com.github.bryanser.twinking.ItemInfo
import com.github.bryanser.twinking.Main
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.random.Random

class Status(config: YamlConfiguration) {
    constructor(file: File) : this(YamlConfiguration.loadConfiguration(file))

    val key: String
    val cost: Cost
    val display: ItemInfo

    val variables = mutableMapOf<String, Variable>()

    val items = mutableListOf<StatusResult>()
    val totalWeight: Int

    init {
        val setting = config.getConfigurationSection("Setting")
        key = setting.getString("key")
        cost = Cost(setting.getConfigurationSection("cost"))
        display = ItemInfo(setting.getConfigurationSection("display"))
        val variable = setting.getConfigurationSection("Variable")
        for (key in variable.getKeys(false)) {
            variables += "<$key>" to Variable(variable.getString(key))
        }
        for (map in config.getMapList("Item")) {
            items += StatusResult(map as Map<String, *>, LinkedHashMap(variables))
        }
        if (items.isEmpty()) {
            throw IllegalArgumentException("配置编写错误 Item")
        }
        totalWeight = items.sumBy(StatusResult::weight)
    }

    fun getStatusResult(): StatusResult {
        var r = Random.Default.nextInt(totalWeight)
        for (sr in this.items) {
            r -= sr.weight
            if (r < 0) {
                return sr
            }
        }
        throw IllegalArgumentException("总权重异常")
    }

    companion object {
        val status = mutableMapOf<String, Status>()

        fun loadConfig() {
            status.clear()
            val folder = File(Main.Plugin.dataFolder, "/Status/")
            if (!folder.exists()) {
                folder.mkdirs()
                Utils.saveResource(Main.Plugin, "example_status.yml", folder)
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
            val s = Status(file)
            status[s.key] = s
        }
    }

}

