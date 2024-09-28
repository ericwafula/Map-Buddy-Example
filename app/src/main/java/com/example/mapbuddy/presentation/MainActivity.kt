@file:RequiresApi(Build.VERSION_CODES.R)

package com.example.mapbuddy.presentation

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mapbuddy.R
import com.example.mapbuddy.presentation.ui.theme.MapBuddyTheme
import com.example.mapbuddy.presentation.util.createTypeMap
import com.example.mapbuddy.presentation.util.hasLocationPermission
import com.example.mapbuddy.presentation.util.shouldShowLocationPermissionRationale
import com.example.mapbuddy.presentation.util.shouldShowNotificationPermissionRationale
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapBuddyTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = Routes.Home
                    ) {
                        composable<Routes.Home> {
                            MapScreen(onSuccess = { navController.navigate(Routes.Details(it)) })
                        }

                        composable<Routes.Details>(
                            typeMap = Routes.Details.TypeMap
                        ) {
                            DetailsScreen()
                        }
                    }
                }
            }
        }
    }
}

sealed interface Routes {
    @Serializable
    data object Home

    @Serializable
    data class Details(val location: DisplayViewModel.Location) {
        companion object {
            val TypeMap = createTypeMap<DisplayViewModel.Location>()
        }
    }
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onSuccess: (DisplayViewModel.Location) -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current
    val location by viewModel.locationFlow.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val hasCourseLocationPermission = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val hasFineLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true

        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()

        viewModel.onAction(
            MainAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = hasCourseLocationPermission && hasFineLocationPermission,
                showLocationRationale = showLocationRationale
            )
        )
    }

    LaunchedEffect(key1 = viewModel.event, key2 = lifecycle) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is MainEvent.OnSuccess -> onSuccess(event.location)
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        viewModel.onAction(
            MainAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = context.hasLocationPermission(),
                showLocationRationale = showLocationRationale
            )
        )

        if (!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestMapBuddyPermissions(context)
        }
    }

    if (viewModel.state.showLocationRationale) {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.permission_required))
            },
            text = {
                Text(
                    text = stringResource(id = R.string.location_rationale)
                )
            },
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { viewModel.onAction(MainAction.DismissRationaleDialog) }) {
                    permissionLauncher.requestMapBuddyPermissions(context)
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(text = "lat=${location?.latitude}, lon=${location?.longitude}")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.onAction(MainAction.OnNavigate) }) {
                Text(text = "Go to screen 2")
            }
        }
    }
}

private fun ActivityResultLauncher<Array<String>>.requestMapBuddyPermissions(
    context: Context
) {
    val hasLocationPermission = context.hasLocationPermission()

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    when {
        !hasLocationPermission -> launch(locationPermissions)
    }
}

@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: DisplayViewModel = koinViewModel()
) {
    val location by viewModel.location.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "lat: ${location?.lat}, lon: ${location?.lon}")
    }
}