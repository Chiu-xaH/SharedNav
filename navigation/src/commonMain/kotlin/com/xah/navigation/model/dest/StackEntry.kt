package com.xah.navigation.model.dest

import com.sharednav.common.helper.randomUUID
import com.xah.navigation.model.anim.TransitionEffect

class StackEntry(
    val destination: Destination,
    val transitionMode: TransitionEffect,
    /**
     * 要求前一个页面保留UI不被销毁，仅enableKeepAlive=false时生效
     */
    var keepPreviousAlive : Boolean
) {
    var id : String = initId()
        private set

    fun resetState() {
        id = initId()
    }

    private fun initId() = randomUUID()

    override fun toString(): String {
        return "StackEntry(id=$id, destination_key=${destination.key}, keepPreviousAlive=$keepPreviousAlive)"
    }

    override fun equals(other: Any?): Boolean = (other as? StackEntry)?.id == this.id

    override fun hashCode(): Int {
        var result = keepPreviousAlive.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + transitionMode.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}