package com.android.inter.process.framework.annotation

import com.android.inter.process.framework.ObjectPool.Collector

/**
 * collect implementations of [Collector] automatically.
 *
 * sample code:
 * ```
 * @CustomCollector
 * class TestCollectorFactory : ObjectPool.Collector {
 *     override fun collect() {
 *         // your code
 *     }
 * }
 * ```
 */
@Retention(value = AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CustomCollector
