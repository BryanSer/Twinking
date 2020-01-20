package com.github.bryanser.twinking.view

import com.github.bryanser.twinking.Sequence
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemMatcher internal constructor(item: ItemStack, val matcher: (ItemMatcher, ItemMatcher) -> Boolean) {
    var id: Int
    var durability: Short
    var meta: ItemMeta?
    fun isSame(i: ItemMatcher): Boolean {
        return matcher(this, i)
    }

    fun isSame(item: ItemStack): Boolean {
        if(Sequence.DEBUG){
            Bukkit.broadcastMessage("尝试比较物品: $item")
            Bukkit.broadcastMessage("与: $this")
        }
        return isSame(ItemMatcher(item, matcher))
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 61 * hash + id
        hash = 61 * hash + durability
        return hash
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as ItemMatcher
        return matcher(this, other)
    }

    override fun toString(): String {
        return "ItemMatcher(id=$id, durability=$durability, meta=$meta)"
    }

    init {
        id = item.typeId
        durability = item.durability
        meta = item.itemMeta?.clone()
    }


}