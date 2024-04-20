package org.datepollsystems.waiterrobot.android.stripe

import android.content.Context
import android.location.LocationManager
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.PaymentStatus
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import kotlinx.coroutines.flow.first
import org.datepollsystems.waiterrobot.android.BuildConfig
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.shared.features.billing.repository.NoReaderFoundException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.ReaderConnectionFailedException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.ReaderDiscoveryFailedException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.StripeProvider
import org.datepollsystems.waiterrobot.shared.features.stripe.api.models.PaymentIntent
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

object Stripe : KoinComponent, TerminalListener, StripeProvider {
    private val logger by injectLoggerForClass()
    private val context by inject<Context>()

    @Suppress("ReturnCount")
    suspend fun connectLocalReader() {
        if (!isGeoLocationEnabled()) {
            logger.i("GPS not enabled")
            // TODO notify user
            return
        }

        if (isInitialized()) {
            logger.i("Terminal is already initialized")
            return
        }

        val locationId =
            (CommonApp.settings.selectedEvent?.stripeSettings as? Event.StripeSettings.Enabled)?.locationId
        if (locationId == null) {
            logger.w("Wanted to connect to local reader, but locationId was null")
            return
        }

        try {
            initialize()
        } catch (e: TerminalException) {
            logger.e("Terminal initialization failed", e)
            return
        }

        try {
            connectLocalReader(locationId)
        } catch (e: ReaderDiscoveryFailedException) {
            logger.e("Reader discovery failed", e)
        } catch (e: ReaderConnectionFailedException) {
            logger.e("Reader connection failed", e)
        }
    }

    // TODO error handling & retry
    suspend fun startPayment(clientSecret: String) {
        val paymentIntent = Terminal.retrievePaymentIntent(clientSecret)

        val collectConfig = CollectConfiguration.Builder()
            .skipTipping(false) // TODO from settings (organization &&/|| wen initializing the reader)?
            .build()

        val collectedIntent = paymentIntent.collectPaymentMethod(collectConfig)

        collectedIntent.confirm()
    }

    override fun onUnexpectedReaderDisconnect(reader: Reader) {
        // TODO handle (or is this already covered by autoReconnectOnUnexpectedDisconnect?)
        logger.w("Reader disconnected")
    }

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        logger.i("Reader status changed to $status")
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        logger.d("Payment status changed to $status")
    }

    override suspend fun initiatePayment(intent: PaymentIntent) {
        startPayment(intent.clientSecret)
    }

    override suspend fun cancelPayment(intent: PaymentIntent) {
        val paymentIntent = Terminal.retrievePaymentIntent(intent.clientSecret)
        paymentIntent.cancel()
    }

    override fun isGeoLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        return runCatching {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }.getOrNull() ?: false
    }

    override fun isInitialized(): Boolean = Terminal.isInitialized()

    override fun initialize(): Unit = Terminal.initTerminal(
        context = context,
        logLevel = LogLevel.VERBOSE,
        tokenProvider = get(),
        listener = this
    )

    override suspend fun disconnectReader(): Unit = Terminal.disconnectReader()

    override suspend fun connectLocalReader(locationId: String) {
        val discoverConfig = DiscoveryConfiguration.LocalMobileDiscoveryConfiguration(
            isSimulated = BuildConfig.DEBUG // In debug mode only simulated readers are supported
        )

        val reader = try {
            Terminal.discoverReaders(discoverConfig).first().firstOrNull()
                ?: throw NoReaderFoundException()
        } catch (e: TerminalException) {
            throw ReaderDiscoveryFailedException(e)
        }

        val connectConfig = ConnectionConfiguration.LocalMobileConnectionConfiguration(
            locationId,
            autoReconnectOnUnexpectedDisconnect = true
        )

        try {
            reader.connect(connectConfig)
        } catch (e: TerminalException) {
            throw ReaderConnectionFailedException(e)
        }

        logger.i("Connected to reader ${reader.id}")
    }
}
