package com.hinnka.speedtest5g.widget

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SpeedAppbar(state: ScaffoldState) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = {
            Text(text = "SPEEDTEST 5G")
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch {
                state.drawerState.open()
            } }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                )
            }
        },
        elevation = 0.dp
    )
}