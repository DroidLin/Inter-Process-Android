package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.BroadcastAndroidAddress
import com.android.inter.process.framework.address.ContentProviderAndroidAddress

/**
 * entry point of installation.
 *
 * @author: liuzhongao
 * @since: 2024/9/15 01:25
 */
fun InterProcessCenter.installAndroid(address: AndroidAddress) {
    installAddress(address)

    val androidComponent = InterProcessAndroidComponent()
    installComponent(AndroidAddress::class.java, androidComponent)
    installComponent(BroadcastAndroidAddress::class.java, androidComponent)
    installComponent(ContentProviderAndroidAddress::class.java, androidComponent)

    objectPool.putCallerFactory(BasicConnection::class.java) { BasicConnection(it as AndroidAddress) }
    objectPool.putReceiver(BasicConnection::class.java, BasicConnection())
}