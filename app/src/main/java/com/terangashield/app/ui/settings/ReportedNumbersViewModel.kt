package com.terangashield.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.repository.CallRepository
import com.terangashield.app.data.repository.ReportRepository
import com.terangashield.app.data.repository.SmsRepository
import com.terangashield.app.domain.model.EventType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ReportedNumberItem(val phoneNumber: String, val eventType: EventType, val timestampMillis: Long)

/**
 * Numéros que L'UTILISATEUR a lui-même signalés comme arnaque (feedback post-appel/SMS) — pas
 * une base communautaire partagée entre utilisateurs, qui nécessiterait un vrai backend (voir
 * `ReportedNumbersRemoteDataSource`, non implémenté). [com.terangashield.app.data.db.entity.UserReportEntity]
 * ne stocke que l'id de l'événement lié ; on va chercher le numéro associé à la volée.
 */
class ReportedNumbersViewModel(
    reportRepository: ReportRepository,
    private val callRepository: CallRepository,
    private val smsRepository: SmsRepository,
) : ViewModel() {

    val myReportedNumbers: StateFlow<List<ReportedNumberItem>> = reportRepository.observeAll()
        .map { reports ->
            reports.filter { it.wasActuallyScam }
                .mapNotNull { report ->
                    val phoneNumber = when (report.eventType) {
                        EventType.CALL -> callRepository.getById(report.relatedEventId)?.phoneNumber
                        EventType.SMS -> smsRepository.getById(report.relatedEventId)?.sender
                    }
                    phoneNumber?.let { ReportedNumberItem(it, report.eventType, report.timestampMillis) }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
