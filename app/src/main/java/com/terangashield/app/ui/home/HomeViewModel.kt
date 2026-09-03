package com.terangashield.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.data.repository.CallRepository
import com.terangashield.app.data.repository.SmsRepository
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.ui.model.ActivityItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val firstName: String = "",
    val callsBlocked7d: Int = 0,
    val smsFiltered7d: Int = 0,
    val recentActivity: List<ActivityItem> = emptyList(),
)

class HomeViewModel(
    callRepository: CallRepository,
    smsRepository: SmsRepository,
    prefs: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        prefs.userFirstName,
        callRepository.observeBlockedCountLast7Days(),
        smsRepository.observeFilteredCountLast7Days(),
        callRepository.observeRecent(10),
        smsRepository.observeRecent(10),
    ) { firstName, callsBlocked, smsFiltered, calls, sms ->
        val activity = (
            calls.map { ActivityItem(it.id, EventType.CALL, it.phoneNumber, it.timestampMillis, it.riskLevel) } +
                sms.map { ActivityItem(it.id, EventType.SMS, it.sender, it.timestampMillis, it.riskLevel) }
            ).sortedByDescending { it.timestampMillis }.take(10)

        HomeUiState(
            firstName = firstName,
            callsBlocked7d = callsBlocked,
            smsFiltered7d = smsFiltered,
            recentActivity = activity,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
