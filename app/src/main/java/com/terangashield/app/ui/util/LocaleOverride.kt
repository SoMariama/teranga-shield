package com.terangashield.app.ui.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.terangashield.app.domain.model.AppLanguage
import java.util.Locale

/**
 * Applique la langue choisie par l'utilisateur à tout l'arbre Compose, indépendamment de la
 * langue système — nécessaire car `MainActivity` est une `ComponentActivity` pure (pas
 * `AppCompatActivity`), donc le mécanisme `AppCompatDelegate.setApplicationLocales` seul ne
 * met pas à jour les ressources vues par Compose. `stringResource()` lit `LocalContext`, donc
 * fournir un `Context` dont les ressources portent la bonne locale suffit à tout traduire.
 *
 * Un simple `ContextWrapper` (pas `createConfigurationContext()` utilisé directement comme
 * `LocalContext`) : `createConfigurationContext()` retourne un `Context` qui ne délègue plus à
 * l'Activity d'origine, ce qui casse les mécanismes internes de Compose qui remontent la chaîne
 * `ContextWrapper.baseContext` pour retrouver l'Activity (ex. `rememberLauncherForActivityResult`,
 * utilisé par l'écran de permissions) — d'où un crash juste après l'écran de consentement.
 * `ContextWrapper` préserve cette chaîne : seule `getResources()` est redéfinie.
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
        val localizedResources = baseContext.createConfigurationContext(configuration).resources
        object : ContextWrapper(baseContext) {
            override fun getResources(): Resources = localizedResources
        }
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
    ) {
        content()
    }
}
