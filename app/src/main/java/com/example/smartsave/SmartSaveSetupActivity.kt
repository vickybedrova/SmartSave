package com.example.smartsave;


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity // Import AppCompatActivity
import androidx.activity.compose.setContent // For Jetpack Compose UI
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import com.example.smartsave.ui.theme.SmartSaveTheme // Assuming you have a theme

class SmartSaveSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If you plan to use Jetpack Compose for this screen's UI:
        setContent {
            SmartSaveTheme {
                SmartSaveSetupScreen()
            }
        }
        // If you plan to use XML layouts (traditional view system):
        // setContentView(R.layout.activity_smart_save_setup) // You'd need to create this XML layout file
    }
}