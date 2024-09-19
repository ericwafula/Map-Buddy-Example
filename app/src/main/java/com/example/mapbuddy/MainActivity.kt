@file:RequiresApi(Build.VERSION_CODES.R)

package com.example.mapbuddy

import android.os.Build
import android.os.Bundle
import android.Manifest
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mapbuddy.ui.theme.MapBuddyTheme
import com.example.mapbuddy.util.getLocation
import com.example.mapbuddy.util.hasLocationPermission
import com.example.mapbuddy.util.hasNotificationPermission
import com.example.mapbuddy.util.shouldShowLocationPermissionRationale
import com.example.mapbuddy.util.shouldShowNotificationPermissionRationale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapScreen(
                        modifier = Modifier.padding(innerPadding),
                        onAction = { action ->
                            viewModel.onAction(action)
                        },
                        state = viewModel.state
                    )
                }
            }
        }
    }
}


@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onAction: (MainAction) -> Unit,
    state: MainState
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val hasCourseLocationPermission = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val hasFineLocationPermission = perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= 33) {
            perms[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            MainAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = hasCourseLocationPermission && hasFineLocationPermission,
                showLocationRationale = showLocationRationale
            )
        )
        onAction(
            MainAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = hasNotificationPermission,
                showNotificationPermissionRationale = showNotificationRationale
            )
        )
    }

    LaunchedEffect(key1 = true) {
        scope.launch {
            val location = context.getLocation()

            onAction(MainAction.OnGetLocationUpdate(location))
        }
    }

    LaunchedEffect(key1 = true) {
        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            MainAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = context.hasLocationPermission(),
                showLocationRationale = showLocationRationale
            )
        )
        onAction(
            MainAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = context.hasNotificationPermission(),
                showNotificationPermissionRationale = showNotificationRationale
            )
        )

        if(!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestMapBuddyPermissions(context)
        }
    }

    if (state.showLocationRationale || state.showNotificationRationale) {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.permission_required))
            },
            text = {
                Text(
                    text = when {
                        state.showLocationRationale && state.showNotificationRationale -> {
                            stringResource(id = R.string.location_notification_rationale)
                        }
                        state.showLocationRationale -> {
                            stringResource(id = R.string.location_rationale)
                        }
                        else -> {
                            stringResource(id = R.string.notification_rationale)
                        }
                    }
                )
            },
            onDismissRequest = { /*TODO*/ },
            confirmButton = {
                TextButton(onClick = { onAction(MainAction.DismissRationaleDialog) }) {
                    permissionLauncher.requestMapBuddyPermissions(context)
                }
            }
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "lat=${state.location?.latitude}, lon=${state.location?.longitude}")
    }
}

private fun ActivityResultLauncher<Array<String>>.requestMapBuddyPermissions(
    context: Context
) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasNotificationPermission()

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    val notificationPermission = if(Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else arrayOf()

    when {
        !hasLocationPermission && !hasNotificationPermission -> {
            launch(locationPermissions + notificationPermission)
        }
        !hasLocationPermission -> launch(locationPermissions)
        !hasNotificationPermission -> launch(notificationPermission)
    }
}