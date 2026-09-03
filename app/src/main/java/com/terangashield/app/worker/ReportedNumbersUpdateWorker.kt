package com.terangashield.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.terangashield.app.ServiceLocator
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Mise à jour incrémentale (delta) de la base de numéros signalés, uniquement si une connexion
 * est disponible (`NetworkType.CONNECTED`), jamais bloquante pour l'usage normal de l'app.
 */
class ReportedNumbersUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val locator = ServiceLocator.get(applicationContext)
        return try {
            val lastUpdate = locator.userPreferencesRepository.reportedDatabaseLastUpdateMillis.first()
            val delta = locator.reportedNumbersRemoteDataSource.fetchDeltaSince(lastUpdate)
            if (delta.isNotEmpty()) {
                locator.reportedNumbersIndex.applyDelta(delta)
            }
            locator.userPreferencesRepository.setReportedDatabaseLastUpdate(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "reported_numbers_delta_sync"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<ReportedNumbersUpdateWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
