package com.terangashield.app.ui.navigation

object Destinations {
    const val ONBOARDING_WELCOME = "onboarding_welcome"
    const val ONBOARDING_COUNTRY_LANGUAGE = "onboarding_country_language"
    const val ONBOARDING_WHY_DEFAULT_ROLES = "onboarding_why_default_roles"
    const val ONBOARDING_PRIVACY = "onboarding_privacy"
    const val ONBOARDING_TRUSTED_CONTACT = "onboarding_trusted_contact"
    const val ONBOARDING_CONSENT = "onboarding_consent"
    const val ONBOARDING_PERMISSIONS = "onboarding_permissions"

    const val HOME = "home"
    const val CALLS = "calls"
    const val CALL_DETAIL = "call_detail/{callId}"
    fun callDetail(id: Long) = "call_detail/$id"
    const val MESSAGES = "messages"
    const val MESSAGE_DETAIL = "message_detail/{messageId}"
    fun messageDetail(id: Long) = "message_detail/$id"
    const val SETTINGS = "settings"
    const val DIALER = "dialer"
    const val CONTACTS = "contacts"
    const val NEW_MESSAGE = "new_message"

    val bottomBarRoutes = listOf(HOME, CALLS, MESSAGES, SETTINGS)
}
