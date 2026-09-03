package com.terangashield.app.data.remote

/**
 * Récupère un delta (nouveaux numéros signalés uniquement) depuis le service communautaire.
 * Aucune implémentation réseau réelle n'est fournie dans ce scaffold V1 — l'URL du service et
 * le format d'authentification restent à définir avec le backend. [NoOpReportedNumbersRemoteDataSource]
 * permet au [com.terangashield.app.worker.ReportedNumbersUpdateWorker] de fonctionner sans erreur
 * en attendant cette intégration ; la fonctionnalité réseau reste strictement optionnelle.
 */
interface ReportedNumbersRemoteDataSource {
    suspend fun fetchDeltaSince(lastUpdateMillis: Long?): List<String>
}

class NoOpReportedNumbersRemoteDataSource : ReportedNumbersRemoteDataSource {
    override suspend fun fetchDeltaSince(lastUpdateMillis: Long?): List<String> = emptyList()
}
