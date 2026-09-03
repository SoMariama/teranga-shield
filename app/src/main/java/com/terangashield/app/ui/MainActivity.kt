package com.terangashield.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terangashield.app.ServiceLocator
import com.terangashield.app.TerangaShieldApp
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.ui.navigation.TerangaNavGraph
import com.terangashield.app.ui.theme.TerangaShieldTheme
import com.terangashield.app.ui.util.ProvideAppLocale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locator: ServiceLocator = (application as TerangaShieldApp).serviceLocator

        setContent {
            TerangaApp(locator)
        }
    }

    @Composable
    private fun TerangaApp(locator: ServiceLocator) {
        val language by locator.userPreferencesRepository.language
            .collectAsStateWithLifecycle(initialValue = AppLanguage.FRENCH)
        ProvideAppLocale(language) {
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
