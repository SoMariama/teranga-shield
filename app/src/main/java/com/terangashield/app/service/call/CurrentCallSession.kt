package com.terangashield.app.service.call

/**
 * Pont léger entre [com.terangashield.app.service.TerangaCallScreeningService] (décision au
 * moment du filtrage, avant la sonnerie) et [com.terangashield.app.service.CallAudioAnalysisService]
 * (analyse pendant l'appel actif). Un appel Android à la fois, donc un état global suffit.
 */
object CurrentCallSession {
    @Volatile var phoneNumber: String? = null
    @Volatile var isKnownContact: Boolean = false
    @Volatile var isReportedNumber: Boolean = false
    @Volatile var callStartMillis: Long = 0L
    @Volatile var trustedContactAlreadyNotified: Boolean = false

    fun start(phoneNumber: String, isKnownContact: Boolean, isReportedNumber: Boolean) {
        this.phoneNumber = phoneNumber
        this.isKnownContact = isKnownContact
        this.isReportedNumber = isReportedNumber
        this.callStartMillis = System.currentTimeMillis()
        this.trustedContactAlreadyNotified = false
    }

    fun reset() {
        phoneNumber = null
        isKnownContact = false
        isReportedNumber = false
        callStartMillis = 0L
        trustedContactAlreadyNotified = false
    }
}
