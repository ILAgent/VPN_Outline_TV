package com.ilagent.nativeoutline.data.remote

import android.content.Context
import android.os.Build
import android.content.res.Configuration
import fi.iki.elonen.NanoHTTPD
import com.ilagent.nativeoutline.R
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class TvLocalServer(
    private val appContext: Context,
    port: Int = 0,
    private val token: String,
    private val onKeyReceived: (String) -> Unit,
    private val preferClientLanguage: Boolean = false,
) : NanoHTTPD(port) {

    private val used = AtomicBoolean(false)

    override fun start(timeout: Int, daemon: Boolean) {
        used.set(false)
        super.start(timeout, daemon)
    }

    override fun stop() {
        super.stop()
        used.set(false)
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        return when {
            session.method == Method.GET && uri == "/session/$token" && !used.get() -> {
                val ctxForStrings = if (preferClientLanguage) {
                    val acceptLang = session.headers["accept-language"]
                    localeContext(appContext, parseFirstLocale(acceptLang))
                } else {
                    appContext
                }

                val html = renderPairPage(ctxForStrings, token)
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/html; charset=utf-8",
                    html
                )
            }

            session.method == Method.POST && uri == "/api/session/$token/submit" && !used.get() -> {
                val body = HashMap<String, String>()
                return try {
                    session.parseBody(body)
                    val json = body["postData"] ?: ""
                    val key = Regex("\"key\"\\s*:\\s*\"(.*?)\"")
                        .find(json)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.trim()
                        .orEmpty()

                    if (key.isBlank()) {
                        newFixedLengthResponse(
                            Response.Status.BAD_REQUEST,
                            "application/json",
                            """{"ok":false,"error":"empty_key"}"""
                        )
                    } else {
                        used.set(true)
                        onKeyReceived(key)
                        newFixedLengthResponse(
                            Response.Status.OK,
                            "application/json",
                            """{"ok":true}"""
                        )
                    }
                } catch (t: Throwable) {
                    newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        """{"ok":false,"error":"${t.message ?: "parse_error"}"}"""
                    )
                }
            }

            else -> {
                val ctxForStrings = if (preferClientLanguage) {
                    val acceptLang = session.headers["accept-language"]
                    localeContext(appContext, parseFirstLocale(acceptLang))
                } else appContext

                val notFound = ctxForStrings.getString(R.string.pair_not_found)
                newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "text/plain; charset=utf-8",
                    notFound
                )
            }
        }
    }

    private fun renderPairPage(ctx: Context, token: String): String {
        val template = ctx.resources.openRawResource(R.raw.pair_page)
            .bufferedReader(charset = Charsets.UTF_8)
            .use { it.readText() }

        return template
            .replace("{{title}}", ctx.getString(R.string.pair_title))
            .replace("{{placeholder}}", ctx.getString(R.string.pair_placeholder))
            .replace("{{send}}", ctx.getString(R.string.pair_send))
            .replace("{{enter_key_alert}}", ctx.getString(R.string.pair_enter_key_alert))
            .replace("{{key_sent_msg}}", ctx.getString(R.string.pair_key_sent_msg))
            .replace("{{error_prefix}}", ctx.getString(R.string.pair_error_prefix))
            .replace("{{submit_url}}", "/api/session/$token/submit")
    }

    private fun parseFirstLocale(acceptLanguage: String?): Locale? {
        val raw = acceptLanguage?.split(',')?.firstOrNull()?.trim() ?: return null
        val langTag = raw.split(';').first().trim()
        return try {
            Locale.forLanguageTag(langTag).takeIf { it.language.isNotBlank() }
        } catch (_: Throwable) { null }
    }

    private fun localeContext(base: Context, locale: Locale?): Context {
        if (locale == null) return base
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

