package com.android.inter.process.framework.content

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.inter.process.framework.connector.FUNCTION_CONTENT_PROVIDER_CONNECT
import com.android.inter.process.framework.connector.connectContext
import com.android.inter.process.framework.metadata.ConnectContext

abstract class ContentAndroidProvider : ContentProvider() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method == FUNCTION_CONTENT_PROVIDER_CONNECT) {
            return onIPConnectionEstablished(extras)
        }
        return super.call(method, arg, extras)
    }

    private fun onIPConnectionEstablished(extras: Bundle?): Bundle? {
        val connectContext = extras?.connectContext
        return if (connectContext != null) {
            val newBundle = Bundle()
            newBundle.connectContext = handleConnectAndReturn(connectContext)
            newBundle
        } else null
    }
}