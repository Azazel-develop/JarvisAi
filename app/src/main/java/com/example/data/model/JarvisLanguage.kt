package com.example.data.model

import java.util.Locale

data class JarvisLanguage(
    val code: String,            // e.g., "EN_GB", "ES_ES", "FR_FR", "DE_DE", "JA_JP", "IT_IT", "PT_BR", "HI_IN", "KO_KR", "ZH_CN", "RU_RU", "EN_US"
    val displayName: String,     // e.g., "Spanish (Español)"
    val nativeName: String,      // e.g., "Español"
    val locale: Locale,
    val flagEmoji: String,
    val greetingConfirmation: String,
    val voiceKeywords: List<String>
)

object LanguageRegistry {
    val ALL_LANGUAGES = listOf(
        JarvisLanguage(
            code = "EN_GB",
            displayName = "English (UK)",
            nativeName = "English (UK)",
            locale = Locale.UK,
            flagEmoji = "🇬🇧",
            greetingConfirmation = "Language set to British English, Sir. Speech synthesis and neural matrix re-aligned.",
            voiceKeywords = listOf("english uk", "british english", "english", "uk english", "english british", "en_gb")
        ),
        JarvisLanguage(
            code = "EN_US",
            displayName = "English (US)",
            nativeName = "English (US)",
            locale = Locale.US,
            flagEmoji = "🇺🇸",
            greetingConfirmation = "Language set to American English, Sir. Speech synthesis updated.",
            voiceKeywords = listOf("english us", "american english", "us english", "en_us")
        ),
        JarvisLanguage(
            code = "ES_ES",
            displayName = "Spanish (Español)",
            nativeName = "Español",
            locale = Locale("es", "ES"),
            flagEmoji = "🇪🇸",
            greetingConfirmation = "Idioma cambiado a Español, Señor. Matriz neuronal y síntesis de voz configuradas.",
            voiceKeywords = listOf("spanish", "espanol", "español", "habla espanol", "cambiar a espanol", "idioma espanol", "spanish language", "es_es", "es")
        ),
        JarvisLanguage(
            code = "FR_FR",
            displayName = "French (Français)",
            nativeName = "Français",
            locale = Locale.FRANCE,
            flagEmoji = "🇫🇷",
            greetingConfirmation = "Langue configurée en Français, Monsieur. Synthèse vocale mise à jour.",
            voiceKeywords = listOf("french", "francais", "français", "parle francais", "langue francaise", "fr_fr", "fr")
        ),
        JarvisLanguage(
            code = "DE_DE",
            displayName = "German (Deutsch)",
            nativeName = "Deutsch",
            locale = Locale.GERMANY,
            flagEmoji = "🇩🇪",
            greetingConfirmation = "Sprache auf Deutsch umgestellt, Sir. Systemprotokolle wurden aktualisiert.",
            voiceKeywords = listOf("german", "deutsch", "sprich deutsch", "deutsche sprache", "de_de", "de")
        ),
        JarvisLanguage(
            code = "IT_IT",
            displayName = "Italian (Italiano)",
            nativeName = "Italiano",
            locale = Locale.ITALY,
            flagEmoji = "🇮🇹",
            greetingConfirmation = "Lingua impostata su Italiano, Signore. Sintesi vocale aggiornata.",
            voiceKeywords = listOf("italian", "italiano", "parla italiano", "lingua italiana", "it_it", "it")
        ),
        JarvisLanguage(
            code = "PT_BR",
            displayName = "Portuguese (Português)",
            nativeName = "Português",
            locale = Locale("pt", "BR"),
            flagEmoji = "🇧🇷",
            greetingConfirmation = "Idioma alterado para Português, Senhor. Matriz de voz configurada.",
            voiceKeywords = listOf("portuguese", "portugues", "português", "fala portugues", "pt_br", "pt")
        ),
        JarvisLanguage(
            code = "JA_JP",
            displayName = "Japanese (日本語)",
            nativeName = "日本語",
            locale = Locale.JAPAN,
            flagEmoji = "🇯🇵",
            greetingConfirmation = "言語を日本語に設定しました、サー。音声合成システムを更新しました。",
            voiceKeywords = listOf("japanese", "nihongo", "日本語", "japan", "japanese language", "ja_jp", "ja")
        ),
        JarvisLanguage(
            code = "KO_KR",
            displayName = "Korean (한국어)",
            nativeName = "한국어",
            locale = Locale.KOREA,
            flagEmoji = "🇰🇷",
            greetingConfirmation = "언어가 한국어로 설정되었습니다, 보스. 신경 음성 엔진이 업데이트되었습니다.",
            voiceKeywords = listOf("korean", "hangul", "한국어", "korean language", "ko_kr", "ko")
        ),
        JarvisLanguage(
            code = "ZH_CN",
            displayName = "Chinese (中文)",
            nativeName = "中文",
            locale = Locale.CHINA,
            flagEmoji = "🇨🇳",
            greetingConfirmation = "语言已切换为中文，先生。神经语音合成系统已就绪。",
            voiceKeywords = listOf("chinese", "mandarin", "中文", "chinese language", "zh_cn", "zh")
        ),
        JarvisLanguage(
            code = "HI_IN",
            displayName = "Hindi (हिन्दी)",
            nativeName = "हिन्दी",
            locale = Locale("hi", "IN"),
            flagEmoji = "🇮🇳",
            greetingConfirmation = "भाषा को हिन्दी में बदल दिया गया है, सर। स्पीच इंजन अपडेट कर दिया गया है।",
            voiceKeywords = listOf("hindi", "हिन्दी", "hindustani", "hindi language", "hi_in", "hi")
        ),
        JarvisLanguage(
            code = "RU_RU",
            displayName = "Russian (Русский)",
            nativeName = "Русский",
            locale = Locale("ru", "RU"),
            flagEmoji = "🇷🇺",
            greetingConfirmation = "Язык переключен на Русский, сэр. Речевой синтез обновлен.",
            voiceKeywords = listOf("russian", "русский", "russian language", "ru_ru", "ru")
        )
    )

    fun findLanguageByCode(code: String): JarvisLanguage {
        val upper = code.uppercase().trim()
        return ALL_LANGUAGES.firstOrNull { 
            it.code.uppercase() == upper || 
            it.code.take(2).uppercase() == upper 
        } ?: ALL_LANGUAGES.first()
    }

    fun findLanguageByKeyword(text: String): JarvisLanguage? {
        val lower = text.lowercase().trim()
        // Check exact slash or keyword matches first
        return ALL_LANGUAGES.firstOrNull { lang ->
            lang.voiceKeywords.any { kw -> 
                lower == kw || lower.endsWith(" $kw") || lower.startsWith("$kw ") || lower.contains(" $kw ")
            }
        } ?: ALL_LANGUAGES.firstOrNull { lang ->
            lang.voiceKeywords.any { kw -> lower.contains(kw) } ||
            lower.contains(lang.displayName.lowercase()) ||
            lower.contains(lang.nativeName.lowercase())
        }
    }
}
