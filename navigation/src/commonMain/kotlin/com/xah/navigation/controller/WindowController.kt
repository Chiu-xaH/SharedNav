package com.xah.navigation.controller

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.xah.navigation.model.window.StackEntry

class WindowController(
    private val _stack: SnapshotStateList<StackEntry> = mutableStateListOf(),
) {
    val stack: List<StackEntry> get() = _stack
}