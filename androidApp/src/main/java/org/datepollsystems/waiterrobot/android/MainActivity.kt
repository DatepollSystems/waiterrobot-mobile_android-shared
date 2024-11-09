package org.datepollsystems.waiterrobot.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.view.WindowCompat
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until
import org.datepollsystems.waiterrobot.android.ui.RootView
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.CommonApp.MIN_UPDATE_INFO_HOURS
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme
import org.datepollsystems.waiterrobot.shared.root.RootViewModel
import org.datepollsystems.waiterrobot.shared.utils.extensions.defaultOnNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : AppCompatActivity(), KoinComponent {

    private val vm: RootViewModel by viewModel()
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val logger by injectLoggerForClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false) // Support ime padding

        // Switch from SplashScreenTheme to AppTheme
        setTheme(R.style.Theme_WaiterRobot_Main)

        setContent {
            RootView(vm, ::onAppThemeChange)
        }

        checkForUpdate()

        // Only handle the deep-links when the app is created the first time (but not when it gets recreated)
        if (savedInstanceState == null) {
            intent?.data?.let {
                logger.d("Started with intent: $it")
                vm.onDeepLink(it.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        this,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                    )
                }
            }
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
                CommonApp.settings.lastUpdateAvailableNote // Show max once a day
                    .defaultOnNull(Instant.DISTANT_PAST)
                    .until(Clock.System.now(), DateTimeUnit.HOUR) > MIN_UPDATE_INFO_HOURS
            ) {
                CommonApp.settings.lastUpdateAvailableNote = Clock.System.now()
                appUpdateManager.startUpdateFlow(
                    appUpdateInfo,
                    this,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                )
            }
        }
    }

    private fun onAppThemeChange(theme: AppTheme) {
        when (theme) {
            AppTheme.SYSTEM -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            AppTheme.LIGHT -> setDefaultNightMode(MODE_NIGHT_NO)
            AppTheme.DARK -> setDefaultNightMode(MODE_NIGHT_YES)
        }
        delegate.applyDayNight()
    }
}
