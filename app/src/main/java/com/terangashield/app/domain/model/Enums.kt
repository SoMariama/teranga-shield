package com.terangashield.app.domain.model

/** Langue pilotant l'interface ET le jeu de données de détection (patterns_xx.json). Indépendante du pays. */
enum class AppLanguage(val code: String, val displayName: String) {
    FRENCH("fr", "Français"),
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: FRENCH
    }
}

/** Pays de l'utilisateur — détermine le format d'affichage/validation des numéros de téléphone. */
enum class Country(val isoCode: String, val dialCode: String, val displayName: String) {
    SENEGAL("SN", "+221", "Sénégal"),
    FRANCE("FR", "+33", "France"),
    UNITED_STATES("US", "+1", "United States"),
    UNITED_KINGDOM("GB", "+44", "United Kingdom"),
    RUSSIA("RU", "+7", "Россия"),
    IVORY_COAST("CI", "+225", "Côte d'Ivoire"),
    MALI("ML", "+223", "Mali"),
    MOROCCO("MA", "+212", "Maroc"),
    OTHER("XX", "+", "Autre");

    companion object {
        fun fromIso(isoCode: String): Country = entries.firstOrNull { it.isoCode == isoCode } ?: OTHER
    }
}

enum class EventType { CALL, SMS }

/** Catégories de scénario utilisées à la fois par le NLU et par les jeux de données patterns_xx.json. */
enum class ScenarioCategory {
    TRUST_BUILDING,
    FABRICATED_URGENCY,
    SENSITIVE_INFO_REQUEST,
    INSTITUTION_IMPERSONATION,
}

enum class SmsRiskReason {
    SUSPICIOUS_LINK,
    OTP_REQUEST,
    BRAND_IMPERSONATION,
    NONE,
}
