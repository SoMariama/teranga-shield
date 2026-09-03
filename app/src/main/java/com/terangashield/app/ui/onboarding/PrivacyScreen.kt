package com.terangashield.app.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.terangashield.app.R

@Composable
fun PrivacyScreen(onNext: () -> Unit) {
    OnboardingScaffold(
        title = stringResource(R.string.onboarding_privacy_title),
        body = stringResource(R.string.onboarding_privacy_body),
        nextLabel = stringResource(R.string.onboarding_next),
        onNext = onNext,
    )
}
