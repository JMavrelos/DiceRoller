package gr.blackswamp.diceroller.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import gr.blackswamp.diceroller.BuildConfig
import java.io.File

@Suppress("unused")
object Logger {
    private val name by lazy { this::class.java.name }
    private var debug = BuildConfig.DEBUG
    private var testing = false
    private var appName = "AppLogging"
    private var contextProvider: (() -> Context?)? = null

    fun enable(debug: Boolean = false, testing: Boolean = false, appName: String) {
        this.appName = appName
        this.debug = debug
        this.testing = testing
    }

    fun setContext(contextProvider: (() -> Context?)?) {
        this.contextProvider = contextProvider
    }


    fun log(tag: String, message: () -> String) =
        doLog(tag, message)

    fun log(message: () -> String) {
        if (testing || debug) {
            val tag = getTag()
            doLog(tag, message)
        }
    }

    private fun doLog(tag: String?, message: () -> String) {
        synchronized(this) {
            val printTag = tag?.prependIndent(":") ?: ""
            val ctx = contextProvider?.invoke()
            if (testing)
                println("$appName$printTag : ${message.invoke()}")
            if (debug) {
                val msgTag = "$appName:$printTag"
                val msgText = message.invoke()

                if (ctx != null && ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    try {
//                        val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (dir != null) {
                            val file = File(dir, "$appName.log")
                            if (!file.exists())
                                file.createNewFile()
//                        File(ctx.getExternalFilesDir(null), "/downloads/$appName.log")
                            file.appendText("$msgTag:$msgText\n")
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getTag(): String? {
        try {
            val trace = Thread.currentThread().stackTrace
            val idx = trace.indexOfLast { it.className.equals(name, true) } + 1
            if (idx == 0)
                return null
            val current = trace.getOrNull(idx) ?: return null
            val fullName = current.className.substring(current.className.lastIndexOf('.') + 1)
            val method: String
            val name: String
            if (fullName.contains('$')) {
                name = fullName.substring(0 until fullName.indexOf('$'))
                val after = fullName.substring(fullName.indexOf('$') + 1)
                method = if (after.contains('$')) {
                    after.substring(0 until after.indexOf('$'))
                } else {
                    after
                }
            } else {
                name = fullName
                method = current.methodName
            }
            return "$name:$method"
        } catch (ignored: Throwable) {
            return null
        }
    }


}