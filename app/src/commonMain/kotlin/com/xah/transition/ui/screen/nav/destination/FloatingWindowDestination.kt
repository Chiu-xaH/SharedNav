package com.xah.transition.ui.screen.nav.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xah.navigation.util.LocalNavController
import com.xah.transition.ui.component.TopBarNavigationIcon
import com.xah.transition.ui.screen.nav.destination.base.NavDestination
import org.jetbrains.compose.resources.painterResource
import sharednav.app.generated.resources.Res
import sharednav.app.generated.resources.ic_texture

data class FloatingWindowDestination(val num : Int) : NavDestination() {
    override val key = "FloatingWindowDestination_$num"
    override val title = "FW_$num"
    override val icon = Res.drawable.ic_texture

    @Composable
    override fun Content() {
        val navController = LocalNavController.current
        Box(modifier = Modifier.size(150.dp).background(
            when(num) {
                1 -> Color.Red
                2 -> Color.Blue
                3 -> Color.Green
                4 -> Color.Yellow
                else -> Color.Gray
            }
        )) {
            Row {
                TopBarNavigationIcon()
                Text(title)
                IconButton(
                    onClick = {
                        navController.push(
                            FloatingWindowDestination(num+1),
                        )
                    }
                ) {
                    Icon(painterResource(Res.drawable.ic_texture),null)
                }
            }
        }
    }
}
