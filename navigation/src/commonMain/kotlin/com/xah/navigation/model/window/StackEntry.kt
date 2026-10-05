package com.xah.navigation.model.window

import com.sharednav.common.helper.randomUUID
import com.xah.navigation.model.anim.TransitionEffect

class StackEntry(
    val window: Window,
    val transitionMode: TransitionEffect
) {
    var id : String = randomUUID()
        private set

    override fun toString(): String {
        return "StackEntry(id=$id, window_key=${window.key})"
    }

    override fun equals(other: Any?): Boolean = (other as? StackEntry)?.id == this.id
    override fun hashCode(): Int {
        var result = window.hashCode()
        result = 31 * result + transitionMode.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}