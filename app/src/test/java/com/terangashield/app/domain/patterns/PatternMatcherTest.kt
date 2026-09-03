package com.terangashield.app.domain.patterns

import com.terangashield.app.domain.model.ScenarioCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternMatcherTest {

    private val dataset = PatternsDataset(
        language = "fr",
        categories = mapOf(
            "TRUST_BUILDING" to listOf(PatternEntry("je suis votre conseiller", 0.4f)),
            "FABRICATED_URGENCY" to listOf(PatternEntry("vous devez agir immédiatement", 0.6f)),
            "SENSITIVE_INFO_REQUEST" to listOf(PatternEntry("donnez-moi votre mot de passe", 0.85f)),
            "INSTITUTION_IMPERSONATION" to listOf(PatternEntry("je vous appelle de la banque", 0.4f)),
        ),
        legitimateContextAllowlist = listOf(
            LegitimateContextEntry("je vous appelle de l'hopital", "FABRICATED_URGENCY", 0.3f),
        ),
    )

    @Test
    fun `blank text yields zero score`() {
        val result = PatternMatcher.score("", dataset)
        assertEquals(0f, result.riskScore)
        assertTrue(result.matchedCategories.isEmpty())
    }

    @Test
    fun `unrelated text yields zero score`() {
        val result = PatternMatcher.score("Bonjour, comment vas-tu aujourd'hui ?", dataset)
        assertEquals(0f, result.riskScore)
    }

    @Test
    fun `matching phrase triggers its category`() {
        val result = PatternMatcher.score("Je suis votre conseiller personnel, tout va bien.", dataset)
        assertTrue(ScenarioCategory.TRUST_BUILDING in result.matchedCategories)
        assertTrue(result.riskScore > 0f)
    }

    @Test
    fun `matching is accent-insensitive`() {
        val result = PatternMatcher.score("JE SUIS VOTRE CONSEILLER, aucun accent ici", dataset)
        assertTrue(ScenarioCategory.TRUST_BUILDING in result.matchedCategories)
    }

    @Test
    fun `legitimate context dampens the targeted category score`() {
        val withoutContext = PatternMatcher.score("Vous devez agir immédiatement sinon...", dataset)
        val withContext = PatternMatcher.score(
            "Je vous appelle de l'hopital, vous devez agir immédiatement sinon...",
            dataset,
        )
        val withoutScore = withoutContext.riskScore
        val withScore = withContext.riskScore
        assertTrue("le score dampé ($withScore) doit être inférieur au score brut ($withoutScore)", withScore < withoutScore)
    }

    @Test
    fun `multiple categories accumulate`() {
        val result = PatternMatcher.score(
            "Je suis votre conseiller. Vous devez agir immédiatement. Donnez-moi votre mot de passe.",
            dataset,
        )
        assertEquals(3, result.matchedCategories.size)
    }
}
