package com.moulgus.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moulgus.macrotracker.ui.navigation.MacroTrackerNavGraph
import com.moulgus.macrotracker.ui.theme.MacroTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MacroTrackerTheme {
                MacroTrackerNavGraph()
            }
        }
    }
}