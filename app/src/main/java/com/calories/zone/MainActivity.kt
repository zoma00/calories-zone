package com.calories.zone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.calories.zone.data.LocalAppStorage
import com.calories.zone.ui.CaloriesRoute
import com.calories.zone.ui.theme.CaloriesZoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val storage = remember { LocalAppStorage(applicationContext) }
            CaloriesZoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CaloriesRoute(storage = storage)
                }
            }
        }
    }
}
