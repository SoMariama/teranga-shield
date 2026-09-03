package com.terangashield.app.ui.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.db.entity.CallRecordEntity
import com.terangashield.app.data.repository.CallRepository
import com.terangashield.app.data.repository.ReportRepository
import com.terangashield.app.domain.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallsViewModel(
    private val callRepository: CallRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    val calls: StateFlow<List<CallRecordEntity>> =
        callRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCall = MutableStateFlow<CallRecordEntity?>(null)
    val selectedCall: StateFlow<CallRecordEntity?> = _selectedCall

    fun loadCall(id: Long) = viewModelScope.launch {
        _selectedCall.value = callRepository.getById(id)
    }

    fun submitFeedback(callId: Long, wasScam: Boolean) = viewModelScope.launch {
        callRepository.recordFeedback(callId, wasScam)
        reportRepository.submit(EventType.CALL, callId, wasScam)
        _selectedCall.value = callRepository.getById(callId)
    }
}
