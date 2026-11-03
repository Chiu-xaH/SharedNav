package com.xah.navigation.controller

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.xah.navigation.model.dest.StackEntry
import kotlin.reflect.KClass

class NavigationViewModel : ViewModel() {
    val stack = mutableStateListOf<StackEntry>()
    val transitionProgress = Animatable(0f)

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return NavigationViewModel() as T
        }
    }
}