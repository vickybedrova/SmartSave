package com.example.smartsave;


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartsave.ui.theme.SmartSaveTheme

class SmartSaveSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSaveTheme {
                SmartSaveSetupScreen()
            }
        }
    }
}