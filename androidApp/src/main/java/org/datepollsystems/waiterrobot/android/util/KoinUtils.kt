package org.datepollsystems.waiterrobot.android.util

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun getLogger(tag: String): Logger = koinInject { parametersOf(tag) }
