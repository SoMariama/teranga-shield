package com.terangashield.app.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.db.entity.SmsRecordEntity
import com.terangashield.app.data.repository.ReportRepository
import com.terangashield.app.data.repository.SmsRepository
import com.terangashield.app.domain.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val smsRepository: SmsRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    val messages: StateFlow<List<SmsRecordEntity>> =
        smsRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMessage = MutableStateFlow<SmsRecordEntity?>(null)
    val selectedMessage: StateFlow<SmsRecordEntity?> = _selectedMessage

    fun loadMessage(id: Long) = viewModelScope.launch {
        smsRepository.markOpened(id)
        _selectedMessage.value = smsRepository.getById(id)
    }

    fun submitFeedback(messageId: Long, wasScam: Boolean) = viewModelScope.launch {
        smsRepository.recordFeedback(messageId, wasScam)
        reportRepository.submit(EventType.SMS, messageId, wasScam)
        _selectedMessage.value = smsRepository.getById(messageId)
    }
}
