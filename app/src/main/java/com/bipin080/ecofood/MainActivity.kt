package com.bipin080.ecofood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bipin080.ecofood.ui.theme.EcoFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoFoodTheme {
                AppRoot()
            }
        }
    }
}
