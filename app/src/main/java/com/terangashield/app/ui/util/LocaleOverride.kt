package com.terangashield.app.ui.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import com.terangashield.app.domain.model.AppLanguage
import java.util.Locale

/**
 * Applique la langue choisie par l'utilisateur à tout l'arbre Compose, indépendamment de la
 * langue système — nécessaire car `MainActivity` est une `ComponentActivity` pure (pas
 * `AppCompatActivity`), donc le mécanisme `AppCompatDelegate.setApplicationLocales` seul ne
 * met pas à jour les ressources vues par Compose. `stringResource()` lit `LocalContext`, donc
 * fournir un `Context` dont la configuration porte la bonne locale suffit à tout traduire.
 */
@Composable
fun ProvideAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val configuration = Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }
        baseContext.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
    ) {
        content()
    }
}
