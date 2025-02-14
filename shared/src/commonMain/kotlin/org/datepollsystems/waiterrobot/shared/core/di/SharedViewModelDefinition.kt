package org.datepollsystems.waiterrobot.shared.core.di

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.module.dsl.DefinitionOptions
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.onOptions
import org.koin.core.qualifier.Qualifier

internal expect inline fun <reified T : ViewModel> Module.sharedViewModel(
    qualifier: Qualifier? = null,
    noinline definition: Definition<T>
): KoinDefinition<T>

internal inline fun <reified R : ViewModel> Module.sharedViewModelOf(
    crossinline constructor: () -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <reified R : ViewModel, reified T1> Module.sharedViewModelOf(
    crossinline constructor: (T1) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <reified R : ViewModel, reified T1, reified T2> Module.sharedViewModelOf(
    crossinline constructor: (T1, T2) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <reified R : ViewModel, reified T1, reified T2, reified T3> Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <reified R : ViewModel, reified T1, reified T2, reified T3, reified T4> Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)

/**
 * @see sharedViewModelOf
 */
internal inline fun <
    reified R : ViewModel,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10
    > Module.sharedViewModelOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
    noinline options: DefinitionOptions<R>? = null,
): KoinDefinition<R> = sharedViewModel { new(constructor) }.onOptions(options)
