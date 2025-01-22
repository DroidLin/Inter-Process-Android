package com.android.inter.process.framework.annotation

/**
 * this should only be annotated on interfaces, any other type of class
 * is not allowed.
 *
 * kotlin code generator will automatically generate the caller and the receiver
 * for the interfaces this annotation is placed to improve running performance.
 *
 * **Note**: we highly recommend you to use this annotation for better performance.
 */
@Retention(value = AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class IPCService
