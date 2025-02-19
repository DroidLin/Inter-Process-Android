package com.android.inter.process.framework

import com.android.inter.process.framework.address.AndroidAddress
import com.android.inter.process.framework.address.BroadcastAddress
import com.android.inter.process.framework.address.ContentProviderAddress
import com.android.inter.process.framework.metadata.InitConfig

/**
 * entry point of installation.
 *
 * @author: liuzhongao
 * @since: 2024/9/15 01:25
 */
fun IPCManager.install(config: InitConfig) {
    val androidComponent = AndroidComponent()
    installComponent(AndroidAddress::class.java, androidComponent)
    installComponent(BroadcastAddress::class.java, androidComponent)
    installComponent(ContentProviderAddress::class.java, androidComponent)
    installConfig(config)
    installDependencies()
}