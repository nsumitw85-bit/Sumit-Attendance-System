package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLanguageCode: String = "mr"

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("TTSManager", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage(currentLanguageCode)
        } else {
            Log.w("TTSManager", "TTS initialization failed status: $status")
        }
    }

    fun setLanguage(langCode: String) {
        currentLanguageCode = langCode
        if (!isInitialized || tts == null) return

        try {
            val locale = when (langCode.lowercase()) {
                "mr" -> Locale.forLanguageTag("mr-IN")
                "hi" -> Locale.forLanguageTag("hi-IN")
                else -> Locale.ENGLISH
            }
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if regional engine is unavailable
                tts?.language = Locale.ENGLISH
            }
        } catch (e: Exception) {
            Log.w("TTSManager", "Failed to set language $langCode", e)
        }
    }

    fun speakAttendanceStatus(status: String, langCode: String = currentLanguageCode) {
        if (!isInitialized || tts == null) return

        setLanguage(langCode)

        val textToSpeak = when (langCode.lowercase()) {
            "mr" -> when (status) {
                "P" -> "हजर"
                "A" -> "गैरहजर"
                "H" -> "अर्धा दिवस"
                "D" -> "डबल ड्युटी"
                else -> ""
            }
            "hi" -> when (status) {
                "P" -> "उपस्थित"
                "A" -> "अनुपस्थित"
                "H" -> "आधा दिन"
                "D" -> "डबल ड्यूटी"
                else -> ""
            }
            else -> when (status) {
                "P" -> "Present"
                "A" -> "Absent"
                "H" -> "Half Day"
                "D" -> "Double Duty"
                else -> ""
            }
        }

        if (textToSpeak.isNotEmpty()) {
            try {
                tts?.stop()
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "attendance_tts")
            } catch (e: Exception) {
                Log.e("TTSManager", "Error speaking text", e)
            }
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w("TTSManager", "TTS shutdown error", e)
        }
    }
}
