package org.datepollsystems.waiterrobot.android.ui.stripe

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.ramcosta.composedestinations.annotation.Destination
import org.datepollsystems.waiterrobot.android.ui.core.LocalSnackbarHostState
import org.datepollsystems.waiterrobot.android.ui.core.handleSideEffects
import org.datepollsystems.waiterrobot.android.util.PermissionsControllerBindEffect
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.features.stripe.viewmodel.StripeInitializationState
import org.datepollsystems.waiterrobot.shared.features.stripe.viewmodel.StripeInitializationViewModel
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Destination
fun StripeInitializationScreen(
    navigator: NavController,
    vm: StripeInitializationViewModel = koinViewModel()
) {
    val state by vm.collectAsState()
    vm.handleSideEffects(navigator)

    PermissionsControllerBindEffect(vm.permissionsController)

    val event by CommonApp.selectedEvent.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        topBar = {
            TopAppBar(
                title = { Text("Initialize Contactless Payment") },
                navigationIcon = {
                    if (navigator.previousBackStackEntry != null) {
                        IconButton(onClick = { navigator.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { state.step.stepIndex / StripeInitializationState.Step.count },
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                when (val step = state.step) {
                    StripeInitializationState.Step.Start -> {
                        Text(
                            text = "The event ${event?.name?.let { "\"$it\" " }.orEmpty()}" +
                                "is set up for contactless payment. To accept contactless payments with your device " +
                                "the contactless payment module must be initialized."
                        )
                        Button(onClick = vm::startInitialization) {
                            Text("Initialize Contactless Payment")
                        }
                    }

                    StripeInitializationState.Step.GrantLocationPermission -> {
                        Text(text = "To ensure secure payments Location permission is required.")
                        Button(onClick = vm::grantLocationPermission) {
                            Text("Grant Location Permission")
                        }
                    }

                    StripeInitializationState.Step.EnableGeoLocation -> {
                        val intentSenderRequestLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartIntentSenderForResult()
                        ) {
                            vm.enableGeoLocation()
                        }
                        val intentLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) {
                            vm.enableGeoLocation()
                        }
                        val context: Context = LocalContext.current
                        Text(text = "To ensure secure payments Location services must be enabled.")
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                checkLocationSetting(
                                    context = context,
                                    onDisabled = intentSenderRequestLauncher::launch,
                                    onEnabled = vm::enableGeoLocation
                                )
                            } else {
                                intentLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        }) {
                            Text("Enable Location Services")
                        }
                    }

                    StripeInitializationState.Step.EnableNfc -> {
                        val launcher = rememberLauncherForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) {
                            vm.enableNfc()
                        }
                        Text(text = "To accept contactless payments NFC needs to be enabled.")
                        Button(onClick = {
                            launcher.launch(Intent(Settings.ACTION_NFC_SETTINGS))
                        }) {
                            Text("Enable NFC")
                        }
                    }

                    is StripeInitializationState.Step.Error -> {
                        Text(text = "An error occurred while initializing contactless payment.")
                        Text(text = step.description)
                        if (step.retryAble) {
                            Button(onClick = vm::startInitialization) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    StripeInitializationState.Step.Finished -> {
                        Text(text = "Contactless payment is initialized.")
                        Button(onClick = vm::onContinueClick) {
                            Text(text = "Let's go!")
                        }
                    }
                }

                if (state.step != StripeInitializationState.Step.Finished) {
                    TextButton(onClick = vm::onContinueClick) {
                        Text("Continue without Contactless Payment")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun checkLocationSetting(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit
) {
    val gpsSettingTask: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(context)
        .checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.Builder(10000).build())
                .build()
        )

    gpsSettingTask.addOnSuccessListener { onEnabled() }
    gpsSettingTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest = IntentSenderRequest
                    .Builder(exception.resolution)
                    .build()
                onDisabled(intentSenderRequest)
            } catch (sendEx: IntentSender.SendIntentException) {
                // ignore here
            }
        }
    }
}
