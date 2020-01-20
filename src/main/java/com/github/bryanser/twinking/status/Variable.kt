package com.github.bryanser.twinking.status

import Br.API.Calculator
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.regex.Pattern

class Variable(
        val script: String
) {
    val intRange = mutableSetOf<Pair<String, IntRange>>()
    val pVariable = mutableSetOf<String>()

    init {
        val rm = range.matcher(script)
        while (rm.find()) {
            val pattern = rm.group("pattern")
            val first = rm.group("first").toInt()
            val second = rm.group("second").toInt()
            intRange += pattern to (first..second)
        }
        val pm = pPattern.matcher(script)
        while (pm.find()) {
            pVariable += pm.group("pattern")
        }
    }


    operator fun invoke(p: Player): String {
        var src = script
        for ((p, r) in intRange) {
            src = src.replace(p, "${r.random()}")
        }
        for (papi in pVariable) {
            src = src.replace(papi, PlaceholderAPI.setPlaceholders(p, papi))
        }
        src = src.replace("level()", "${p.level}")
        return decimalField.format(Calculator.conversion(src))
    }

    companion object {
        val decimalField = DecimalFormat("#.##")

        val range = Pattern.compile("(?<pattern>\\[(?<first>[+-]?[0-9]+):(?<second>[+-]?[0-9]+)\\])")
        val pPattern = Pattern.compile("(?<pattern>%[^%]+%)")
    }

}