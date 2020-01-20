package com.github.bryanser.twinking

import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import com.github.bryanser.twinking.status.Status
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandles

class ItemData() {
    var sequence: String? = null
    var statusKey: String? = null

    constructor(item: ItemStack) : this() {
        if (item.type == Material.AIR) {
            return
        }
        if (!MinecraftReflection.isCraftItemStack(item)) {
            return
        }
        val tag = NbtFactory.fromItemTag(item) ?: return
        val nbt = NbtFactory.asCompound(tag) ?: return
        if (nbt.containsKey(NBT_KEY)) {
            val com = nbt.getCompound(NBT_KEY)!!
            sequence = com.getString(NBT_SEQ)
            statusKey = com.getString(NBT_STA)
        }
    }

    fun writeNbt(i: ItemStack, status: Status, player: Player): ItemStack {
        val i = i.clone()
        val sr = status.getStatusResult()
        i.durability = sr.result.data
        val item = toCraftItem(i)
        val tag = NbtFactory.fromItemTag(item) ?: NbtFactory.ofCompound("tag")
        val nbt = NbtFactory.asCompound(tag)!!
        val com = NbtFactory.ofCompound(NBT_KEY)
        com.put(NBT_SEQ, sequence)
        com.put(NBT_STA, statusKey)
        nbt.put(NBT_KEY, com)
        NbtFactory.setItemTag(item, nbt)
        val im = item.itemMeta
        val lore = im.lore ?: mutableListOf()
        val iter = lore.listIterator()
        while (iter.hasNext()) {
            val s = iter.next()
            if (s.contains(LORE_PREV)) {
                iter.remove()
            }
        }
        val (nlore, name) = sr.createLoreWithName(player)
        for(s in nlore){
            lore += LORE_PREV + s
        }
        im.displayName = name
        im.lore = lore
        try{
            im.isUnbreakable = true
        }catch (e:Throwable){
            im.spigot().isUnbreakable = true
        }
        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        item.itemMeta = im
        return item
    }

    companion object {
        const val LORE_PREV = "§r§c§e§a§b§6§2§9§r"

        const val NBT_KEY = "TWINKING_COM"
        const val NBT_SEQ = "TWINKING_SQE"
        const val NBT_STA = "TWINKING_STA"

        val toCraftItem: (ItemStack) -> ItemStack by lazy {
            val func = MinecraftReflection.getCraftItemStackClass().getMethod("asCraftCopy", ItemStack::class.java)
            func.isAccessible = true
            val invo = MethodHandles.lookup().unreflect(func).bindTo(null)
            val lam: (ItemStack) -> ItemStack = d@{ item ->
                return@d if (MinecraftReflection.isCraftItemStack(item)) {
                    item
                } else {
                    invo.invoke(item) as ItemStack
                }
            }
            lam
        }
    }
}