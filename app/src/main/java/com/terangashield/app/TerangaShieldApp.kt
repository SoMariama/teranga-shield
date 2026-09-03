package com.terangashield.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.terangashield.app.worker.ReportedNumbersUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TerangaShieldApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator.get(this)

        applicationScope.launch {
            val language = serviceLocator.userPreferencesRepository.currentLanguageBlocking()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
        }

        // Mise à jour delta non bloquante, uniquement si une connexion est disponible.
        ReportedNumbersUpdateWorker.schedulePeriodic(this)

        // Historique des SMS déjà présents sur l'appareil : sans ça, l'onglet Messages reste
        // vide tant qu'aucun nouveau SMS n'arrive après le passage en app par défaut.
        applicationScope.launch { serviceLocator.smsHistoryImporter.importIfNeeded() }
    }
}
