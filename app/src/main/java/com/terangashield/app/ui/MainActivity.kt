package com.terangashield.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.terangashield.app.ServiceLocator
import com.terangashield.app.TerangaShieldApp
import com.terangashield.app.ui.navigation.TerangaNavGraph
import com.terangashield.app.ui.theme.TerangaShieldTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locator: ServiceLocator = (application as TerangaShieldApp).serviceLocator

        setContent {
            TerangaShieldTheme {
                TerangaNavGraph(locator = locator)
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
