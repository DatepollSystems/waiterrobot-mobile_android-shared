package org.datepollsystems.waiterrobot.android

import android.app.Application
import android.os.Build
import com.stripe.stripeterminal.TerminalApplicationDelegate
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import org.datepollsystems.waiterrobot.android.stripe.Stripe
import org.datepollsystems.waiterrobot.android.stripe.StripeTokenProvider
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.OS
import org.datepollsystems.waiterrobot.shared.generated.localization.localizationContext
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class WaiterRobotApp : Application() {
    override fun onCreate() {
        super.onCreate()

        localizationContext = this

        val phoneModel = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() } + " " +
            Build.MODEL.replaceFirstChar { it.uppercaseChar() }

        // Init CommonApp right at the start as e.g. koin might depend on some properties of it
        CommonApp.init(
            os = OS.Android(Build.VERSION.RELEASE),
            appVersion = BuildConfig.VERSION_NAME.substringBeforeLast("-"), // Remove appBuild from version
            appBuild = BuildConfig.VERSION_CODE,
            phoneModel = phoneModel,
            allowedHostsCsv = BuildConfig.ALLOWED_HOSTS_CSV,
            stripeProvider = Stripe,
        ) {
            androidContext(this@WaiterRobotApp)
            val androidModule = module {
                single<ConnectionTokenProvider> { StripeTokenProvider(get()) }
            }

            modules(androidModule)
        }

        TerminalApplicationDelegate.onCreate(this)
    }
}
