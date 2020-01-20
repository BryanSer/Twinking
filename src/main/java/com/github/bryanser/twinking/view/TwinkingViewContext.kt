package com.github.bryanser.twinking.view

import com.github.bryanser.brapi.ItemBuilder
import com.github.bryanser.brapi.Utils
import com.github.bryanser.brapi.kview.KViewContext
import com.github.bryanser.brapi.kview.KViewHandler
import com.github.bryanser.twinking.ItemData
import com.github.bryanser.twinking.Main
import com.github.bryanser.twinking.Sequence
import com.github.bryanser.twinking.status.Status
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class TwinkingViewContext(p: Player) : KViewContext("§6装备进阶") {
    var item: ItemStack? = null
    var page = 0

    var sequence: Sequence? = null
    var currKey: String? = null
    val statusList = mutableListOf<Status>()
    var select: Int? = null
    var selectStatus: Status? = null

    inner class ItemAccessor : Iterable<ItemStack?> {
        override fun iterator(): Iterator<ItemStack?> = listIterator()

        fun listIterator(): MutableListIterator<ItemStack?> {
            return object : MutableListIterator<ItemStack?> {
                var curr = -1

                override fun hasNext(): Boolean = curr < emptySlots.size-1

                override fun next(): ItemStack? {
                    return inventory.getItem(emptySlots[++curr])
                }

                override fun remove() {
                    inventory.setItem(emptySlots[curr], null)
                }

                override fun hasPrevious(): Boolean = curr >= 0

                override fun nextIndex(): Int = curr + 1

                override fun previous(): ItemStack? {
                    return inventory.getItem(emptySlots[--curr])
                }

                override fun previousIndex(): Int = curr - 1

                override fun add(element: ItemStack?) {
                    TODO("not implemented")
                }

                override fun set(element: ItemStack?) {
                    inventory.setItem(emptySlots[curr], element)
                }

            }
        }

    }

    fun update() {
        item = inventory.getItem(ITEM_SLOT)
        statusList.clear()
        page = 0
        select = null
        if (item == null) {
            sequence = null
            currKey = null
            selectStatus = null
            return
        }
        val data = ItemData(item!!)
        if (data.sequence == null) {
            sequence = Sequence.findSequence(item!!)
            if (sequence != null) {
                currKey = "start"
            }
        } else {
            sequence = Sequence.sequences[data.sequence!!]
            currKey = data.statusKey
        }
        if (sequence != null) {
            if(sequence?.findMap?.get(currKey) == null){
                return
            }
            statusList.addAll(sequence?.findMap?.get(currKey)?.map { it.status }
                    ?: throw IllegalStateException("错误 找不到状态$currKey")
            )
        }
        KViewHandler.updateUI(player)
    }

    fun click() {
        this.update()
        if (selectStatus == null) {
            return
        }
        if (item == null) {
            return
        }
        if (!Utils.economy!!.has(player.name, selectStatus!!.cost.money)) {
            player.sendMessage("§c你没有足够的金钱来升阶")
            return
        }
        if (!hasEnoughItems(player, selectStatus!!.cost.items.map {
                    val f = it.get()
                    if (Sequence.DEBUG) {
                        player.sendMessage("§6升阶需求物品: $f")
                    }
                    f
                }, ItemAccessor())) {
            player.sendMessage("§c你放入的物品不正确")
            return
        }
        removeItem(player, selectStatus!!.cost.items.map { it.get() }, ItemAccessor())
        val id = ItemData()
        id.sequence = this.sequence!!.name
        id.statusKey = this.selectStatus!!.key
        val result = id.writeNbt(item!!, this.selectStatus!!, player)
        inventory.setItem(ITEM_SLOT, result)
        this.update()
    }

    companion object {
        const val ITEM_SLOT = 10
        const val PAGE_UP = 17
        const val PAGE_DOWN = 12
        const val BUTTON = 44

        val emptySlots = intArrayOf(*(27..34).toList().toIntArray(),
                *(36..43).toList().toIntArray(),
                *(45..52).toList().toIntArray())
        val view = KViewHandler.createKView("TwinkingView", 6, ::TwinkingViewContext) {

            onClose {
                val default = ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 5) {
                    name = "§6请放入装备"
                }
                this.inventory.getItem(ITEM_SLOT)?.also {
                    if(!it.isSimilar(default)){
                        Utils.safeGiveItem(player, it)
                    }
                }
                for (i in emptySlots) {
                    this.inventory.getItem(i)?.also {
                        Utils.safeGiveItem(player, it)
                    }
                }
            }
            for (i in 0..26) {
                icon(i) {
                    initDisplay(ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 1) { name = " " })
                }
            }
            for (index in 0..3) {
                val icon = icon {
                    val off = ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 1) { name = " " }
                    val on = ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 4) { name = " " }
                    initDisplay {
                        if (index == select) {
                            return@initDisplay on
                        }
                        off
                    }
                }
                (index + 4) += icon
                (index + 22) += icon
                icon(index + 13) {
                    val none = ItemBuilder.createItem(Material.BARRIER) {
                        name = " "
                    }
                    initDisplay {
                        val i = index + page
                        statusList.getOrNull(i)?.display?.getItem(player) ?: none
                    }
                    click {
                        if (sequence != null) {
                            val i = index + page
                            val select = statusList.getOrNull(i)
                            if (select != null) {
                                this.selectStatus = select
                                this.select = i
                            }
                        }
                    }
                }
            }
            for (i in arrayOf(0, 1, 2, 9, 11, 18, 19, 20)) {
                icon(i) {
                    initDisplay(ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 1) { name = " " })
                }
            }
            icon(PAGE_DOWN) {
                val down = ItemBuilder.createItem(Material.ARROW) {
                    name = "§6往左"
                }
                initDisplay {
                    if (page == 0) {
                        return@initDisplay null
                    }
                    down
                }
                click {
                    if (page != 0) {
                        page--
                    }
                    if (select != null) {
                        select = select!! + 1
                    }
                }
            }
            icon(PAGE_UP) {
                initDisplay(ItemBuilder.createItem(Material.ARROW) {
                    name = "§b向右"
                })
                click {
                    page++
                    if (select != null) {
                        select = select!! - 1
                    }
                }
            }
            slotIcon(ITEM_SLOT) {
                val default = ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 5) {
                    name = "§6请放入装备"
                }
                initDisplay {
                    item ?: default
                }
                cancelClick {
                    if (inventory.getItem(ITEM_SLOT)?.isSimilar(default) == true) {
                        inventory.setItem(ITEM_SLOT, null)
                    }
                    Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                        update()
                    }, 2)
                    false
                }
            }
            for (i in emptySlots) {
                slotIcon(i) {
                }
            }
            icon(35) {
                initDisplay(ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 6) { name = "§6请在左侧放入物品" })
            }
            icon(53) {
                initDisplay(ItemBuilder.createItem(Material.STAINED_GLASS_PANE, durability = 6) { name = "§6请在左侧放入物品" })
            }
            icon(BUTTON) {
                initDisplay(ItemBuilder.createItem(Material.ANVIL) {
                    name = "§6点击升阶"
                })
                click {
                    this.click()
                }
            }
        }
    }
}


@JvmField
val defaultMatcher: (ItemMatcher, ItemMatcher) -> Boolean = d@{ i1, i2 ->
    //    i1.id == i2.id && i1.durability == i2.durability && Bukkit.getItemFactory().equals(i1.meta, i2.meta)
    if (i1.id == i2.id && i1.durability == i2.durability) {
        if (i1.meta != null && i2.meta != null) {
            if (i1.meta?.displayName?.replace("§.".toRegex(),"")
                    ==
                    i2.meta?.displayName?.replace("§.".toRegex(),"")) {
                return@d true
            }
        } else {
            return@d true
        }
    }
    false
}

fun hasEnoughItems(p: Player, items: Iterable<ItemStack?>, accessor: TwinkingViewContext.ItemAccessor, matcher: (ItemMatcher, ItemMatcher) -> Boolean = defaultMatcher): Boolean {
    val map: MutableMap<ItemMatcher, Int> = HashMap()
    items.forEach { item: ItemStack? ->
        if (item == null) {
            return@forEach
        }
        val i = ItemMatcher(item, matcher)
        if (map.containsKey(i)) {
            map[i] = map[i]!! + item.amount
        } else {
            map[i] = item.amount
        }
    }
    for (`is` in accessor) {
        if (`is` == null || `is`.amount == 0 || `is`.type == Material.AIR) {
            continue
        }
        for (item in map.keys) {
            if (item.isSame(`is`)) {
                map[item] = map[item]!! - `is`.amount
                break
            }
        }
    }
    return map.values.stream().noneMatch { a: Int -> a > 0 }
}

fun removeItem(p: Player, items: Iterable<ItemStack?>, accessor: TwinkingViewContext.ItemAccessor, matcher: (ItemMatcher, ItemMatcher) -> Boolean = defaultMatcher) {
    val map: MutableMap<ItemMatcher, Int> = HashMap()
    items.forEach { item: ItemStack? ->
        if (item == null) {
            return@forEach
        }
        val i = ItemMatcher(item, matcher)
        if (map.containsKey(i)) {
            map[i] = map[i]!! + item.amount
        } else {
            map[i] = item.amount
        }
    }
    A@ for (e in map.entries) {
        val cl = e.key
        var amount = e.value
        val iter = accessor.listIterator()
        while (iter.hasNext()) {
            val item = iter.next()
            if (amount <= 0) {
                continue@A
            }
            if (item == null) {
                continue
            }
            if (cl.isSame(item)) {
                if (amount - item.amount < 0) {
                    item.amount = item.amount - amount
                    iter.set(item)
//                    p.inventory.setItem(i, item)
                    continue@A
                }
                if (amount == item.amount) {
                    iter.remove()
//                    p.inventory.setItem(i, null)
                    continue@A
                }
                if (amount > item.amount) {
                    amount -= item.amount
                    iter.remove()
//                    p.inventory.setItem(i, null)
                }
            }
        }
    }
}
