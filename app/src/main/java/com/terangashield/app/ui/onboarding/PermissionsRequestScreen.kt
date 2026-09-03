package com.terangashield.app.ui.onboarding

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.TerangaViewModelFactory

private const val STEP_SMS_ROLE = 0
private const val STEP_CALL_ROLE = 1
private const val STEP_DIALER_ROLE = 2
private const val STEP_RUNTIME_PERMISSIONS = 3
private const val STEP_DONE = 4

private val RUNTIME_PERMISSIONS = buildList {
    add(android.Manifest.permission.RECORD_AUDIO)
    add(android.Manifest.permission.READ_CONTACTS)
    add(android.Manifest.permission.READ_PHONE_STATE)
    add(android.Manifest.permission.READ_CALL_LOG)
    add(android.Manifest.permission.CALL_PHONE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

/**
 * Dernière étape de l'onboarding : demande des rôles par défaut (SMS, filtrage d'appels) puis
 * des permissions runtime nécessaires. Chaque demande système est déclenchée explicitement,
 * jamais automatiquement, et seulement après l'écran de consentement dédié.
 */
@Composable
fun PermissionsRequestScreen(locator: ServiceLocator, onFinish: () -> Unit) {
    val context = LocalContext.current
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    var step by remember { mutableIntStateOf(STEP_SMS_ROLE) }

    val roleManager = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else {
            null
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        step += 1
    }
    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        step = STEP_DONE
    }

    LaunchedEffect(step) {
        if (step == STEP_DONE) {
            viewModel.completeOnboarding()
            onFinish()
        }
    }

    fun requestRole(role: String) {
        when {
            roleManager != null && roleManager.isRoleHeld(role) -> {
                // Déjà accordé (ex. sélectionné manuellement lors d'un essai précédent).
                step += 1
            }
            roleManager != null && roleManager.isRoleAvailable(role) -> {
                roleLauncher.launch(roleManager.createRequestRoleIntent(role))
            }
            else -> {
                // API RoleManager indisponible ou rôle non proposable automatiquement sur cet
                // appareil (observé sur certains OEM) : on oriente vers les réglages système
                // pour une sélection manuelle, plutôt que de continuer silencieusement sans le
                // rôle — voir le retour utilisateur qui a dû l'activer à la main.
                runCatching {
                    roleLauncher.launch(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                }.onFailure { step += 1 }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.permissions_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.permissions_body), style = MaterialTheme.typography.bodyLarge)

            Button(
                onClick = {
                    when (step) {
                        STEP_SMS_ROLE -> requestRole(RoleManager.ROLE_SMS)
                        STEP_CALL_ROLE -> requestRole(RoleManager.ROLE_CALL_SCREENING)
                        STEP_DIALER_ROLE -> requestRole(RoleManager.ROLE_DIALER)
                        STEP_RUNTIME_PERMISSIONS -> permissionsLauncher.launch(RUNTIME_PERMISSIONS)
                        else -> Unit
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.permissions_grant))
            }
        }
    }
}
