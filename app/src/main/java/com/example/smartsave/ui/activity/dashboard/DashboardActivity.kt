package com.example.smartsave.ui.activity.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartsave.ui.theme.SmartSaveTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSaveTheme {
                DashboardScreen()
            }
        }
    }
}
