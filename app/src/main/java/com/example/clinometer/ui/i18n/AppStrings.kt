package com.example.clinometer.ui.i18n

import android.content.Context
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.NotationFormat
import java.util.Locale
import org.json.JSONObject

/**
 * 多言語 (i18n) 文字列リソースデータクラス
 *
 * すべての言語データは `app/src/main/assets/i18n/<code.json>` の単一 JSON ファイルで管理されており、
 * 外部コントリビューターが 1 ファイルを翻訳するだけで Pull Request を作成できる構造になっています。
 */
data class AppStrings(
  val appTitle: String,
  val clinoCompassPlate: String,
  val standardCompassPlate: String,
  val declinationPrefix: String,
  val modeSurfaceLabel: String,
  val modeEdgeLabel: String,
  val formatJapaneseShort: String,
  val formatRhrShort: String,
  val formatDipDirShort: String,
  val flatDipWord: String,
  val verticalDipWord: String,
  val strikeLabel: String,
  val dipDirectionLabel: String,
  val dipLabel: String,
  val rhrHeadingSub: (Int, Int) -> String,
  val dipDirSub: (Int, String) -> String,
  val holdLockedBadge: String,
  val holdReleaseBtn: String,
  val holdLockBtn: String,
  val saveToFieldNoteBtn: String,
  val magneticInterferenceWarning: String,
  val levelCalibratedBadge: String,
  val simTitle: String,
  val simStrikeSlider: (Int) -> String,
  val simDipSlider: (Int) -> String,
  val saveDialogTitle: String,
  val saveDialogNoteHint: String,
  val saveBtn: String,
  val cancelBtn: String,
  val closeBtn: String,
  val savedToast: String,
  val csvCopiedToast: String,
  val settingsTitle: String,
  val languageSectionTitle: String,
  val declinationSectionTitle: (Float) -> String,
  val declinationHint: String,
  val gpsAutoDeclinationBtn: String,
  val gpsCalculating: String,
  val gpsSuccessToast: (Float, Double, Double) -> String,
  val gpsFallbackToast: String,
  val reverseEwTitle: String,
  val reverseEwDesc: String,
  val levelCalibrationTitle: String,
  val levelCalibrationDesc: (Float, Float) -> String,
  val calibrateZeroBtn: String,
  val resetCalibrationBtn: String,
  val calibratedToast: String,
  val voiceReadoutTitle: String,
  val voiceReadoutDesc: String,
  val simModeTitle: String,
  val simModeDesc: String,
  val openHelpBtn: String,
  val openOnboardingDebugBtn: String,
  val fieldNoteSheetTitle: (Int) -> String,
  val copyCsvBtn: String,
  val emptyFieldNoteText: String,
  // 初回限定オンボーディングカルーセル用
  val onboardingStep1Title: String,
  val onboardingStep1Body: String,
  val onboardingStep2Title: String,
  val onboardingStep2Body: String,
  val onboardingStep3Title: String,
  val onboardingStep3Body: String,
  val onboardingStep4Title: String,
  val onboardingStep4Body: String,
  val nextBtn: String,
  val backBtn: String,
  val startBtn: String,
  val skipBtn: String,
  // 使い方ヘルプシート (ClinometerHelpSheet) 全項目
  val helpPageTitle: String,
  val helpPageSubtitle: String,
  val helpSec1Title: String,
  val helpStep1Title: String,
  val helpStep1Desc: String,
  val helpStep2Title: String,
  val helpStep2Desc: String,
  val helpStep3Title: String,
  val helpStep3Desc: String,
  val helpStep4Title: String,
  val helpStep4Desc: String,
  val helpSec2Title: String,
  val helpSurfaceModeHeading: String,
  val helpSurfaceModeIntro: String,
  val helpDiagramTitle: String,
  val helpDiagramBadge: String,
  val helpDiagramNormalLabel: String,
  val helpDiagramPlaneLabel: String,
  val helpDiagramStrikeLabel: String,
  val helpDiagramDipLabel: String,
  val helpDiagramPoint1Title: String,
  val helpDiagramPoint1Desc: String,
  val helpDiagramPoint2Title: String,
  val helpDiagramPoint2Desc: String,
  val helpDiagramPoint3Title: String,
  val helpDiagramPoint3Desc: String,
  val helpEdgeModeHeading: String,
  val helpEdgeModeIntro: String,
  val helpSec3Title: String,
  val helpNotation1Name: String,
  val helpNotation1Desc: String,
  val helpNotation2Name: String,
  val helpNotation2Desc: String,
  val helpNotation3Name: String,
  val helpNotation3Desc: String,
  val helpSec4Title: String,
  val helpSec4Body: String,
  val helpSec5Title: String,
  val helpFeat1Title: String,
  val helpFeat1Desc: String,
  val helpFeat2Title: String,
  val helpFeat2Desc: String,
  val helpFeat3Title: String,
  val helpFeat3Desc: String,
  val helpFeat4Title: String,
  val helpFeat4Desc: String,
  val helpWebCardTitle: String,
  val helpWebCardDesc: String,
  val helpWikiBtn: String,
  val helpWikiUrl: String,
  val helpSurveyBtn: String,
  val helpSurveyUrl: String,
  // アプリ情報・クレジット・リリースノート・ライセンス用
  val aboutSectionTitle: String,
  val madeInOkinawaBadge: String,
  val authorLabel: String,
  val whatsNewBtn: String,
  val githubSourceBtn: String,
  val licenseBtn: String,
  val whatsNewDialogTitle: String,
  val whatsNewV110Title: String,
  val whatsNewV110Body: String,
  val whatsNewV100Title: String,
  val whatsNewV100Body: String,
  val openGithubReleasesBtn: String,
  val licenseDialogTitle: String,
  val whatIsKayoBtn: String,
  val whatIsKayoTitle: String,
  val whatIsKayoSubtitle: String,
  val whatIsKayoSec1Title: String,
  val whatIsKayoSec1Body: String,
  val whatIsKayoSec2Title: String,
  val whatIsKayoSec2Body: String,
  val whatIsKayoSec3Title: String,
  val whatIsKayoSec3Body: String,
  val whatIsKayoPhotoCaption: String,
  val whatIsKayoPhotoCredit: String,
  val whatIsKayoPhotoUrl: String,
  val whatIsKayoLinkBtn: String,
  val whatIsKayoLinkUrl: String
) {
  fun modeLabel(mode: MeasureMode): String = when (mode) {
    MeasureMode.SURFACE -> modeSurfaceLabel
    MeasureMode.EDGE -> modeEdgeLabel
  }

  fun formatShortLabel(format: NotationFormat): String = when (format) {
    NotationFormat.JAPANESE -> formatJapaneseShort
    NotationFormat.RIGHT_HAND_RULE -> formatRhrShort
    NotationFormat.DIP_DIRECTION -> formatDipDirShort
  }
}

object AppStringsProvider {

  private val cache = mutableMapOf<AppLanguage, AppStrings>()

  /**
   * `assets/i18n/<code>.json` から該当言語の文字列リソースを読み込む。
   * 万が一翻訳ファイルに未翻訳キーがある場合は `en.json` の値に自動フォールバックするため、
   * 部分的な翻訳 PR でもクラッシュしない安全な設計。
   */
  fun load(context: Context, language: AppLanguage): AppStrings {
    cache[language]?.let { return it }

    val fallbackJson = readAssetJson(context, AppLanguage.EN.code)
    val targetJson = if (language == AppLanguage.EN) {
      fallbackJson
    } else {
      readAssetJson(context, language.code)
    }

    fun str(key: String, defaultVal: String = key): String {
      return targetJson?.optString(key)?.takeIf { it.isNotEmpty() }
        ?: fallbackJson?.optString(key)?.takeIf { it.isNotEmpty() }
        ?: defaultVal
    }

    val loaded = AppStrings(
      appTitle = str("appTitle", "Kayo-Clino"),
      clinoCompassPlate = str("clinoCompassPlate", "E/W Swapped"),
      standardCompassPlate = str("standardCompassPlate", "Standard"),
      declinationPrefix = str("declinationPrefix", "Decl."),
      modeSurfaceLabel = str("modeSurfaceLabel", "Planar (Back Flat)"),
      modeEdgeLabel = str("modeEdgeLabel", "Edge (Classic)"),
      formatJapaneseShort = str("formatJapaneseShort", "Quadrant (N30°E)"),
      formatRhrShort = str("formatRhrShort", "RHR (030/45)"),
      formatDipDirShort = str("formatDipDirShort", "Dip/DipDir"),
      flatDipWord = str("flatDipWord", "Flat"),
      verticalDipWord = str("verticalDipWord", "Vert"),
      strikeLabel = str("strikeLabel", "Strike"),
      dipDirectionLabel = str("dipDirectionLabel", "Dip Direction"),
      dipLabel = str("dipLabel", "Dip Angle"),
      rhrHeadingSub = { rhr, dev ->
        str("rhrHeadingPattern", "RHR: %1\$03d° / Heading: %2\$03d°").format(Locale.US, rhr, dev)
      },
      dipDirSub = { dir, card ->
        str("dipDirPattern", "Dip Dir: %1\$03d° (%2\$s)").format(Locale.US, dir, card)
      },
      holdLockedBadge = str("holdLockedBadge", "HOLD LOCKED"),
      holdReleaseBtn = str("holdReleaseBtn", "Release Hold"),
      holdLockBtn = str("holdLockBtn", "HOLD (or Vol Key)"),
      saveToFieldNoteBtn = str("saveToFieldNoteBtn", "Log Data"),
      magneticInterferenceWarning = str("magneticInterferenceWarning"),
      levelCalibratedBadge = str("levelCalibratedBadge", "Zero-Calibrated"),
      simTitle = str("simTitle", "Simulation Controls (Desk Test)"),
      simStrikeSlider = { deg ->
        str("simStrikePattern", "Strike Azimuth (RHR): %1\$d°").format(Locale.US, deg)
      },
      simDipSlider = { deg ->
        str("simDipPattern", "Dip Angle: %1\$d°").format(Locale.US, deg)
      },
      saveDialogTitle = str("saveDialogTitle", "Save to Field Note"),
      saveDialogNoteHint = str("saveDialogNoteHint"),
      saveBtn = str("saveBtn", "Save"),
      cancelBtn = str("cancelBtn", "Cancel"),
      closeBtn = str("closeBtn", "Close"),
      savedToast = str("savedToast"),
      csvCopiedToast = str("csvCopiedToast"),
      settingsTitle = str("settingsTitle", "Clinometer Settings"),
      languageSectionTitle = str("languageSectionTitle", "Language / 表示言語"),
      declinationSectionTitle = { dec ->
        str("declinationSectionPattern", "Magnetic Declination: %1\$.1f°").format(Locale.US, dec)
      },
      declinationHint = str("declinationHint"),
      gpsAutoDeclinationBtn = str("gpsAutoDeclinationBtn"),
      gpsCalculating = str("gpsCalculating"),
      gpsSuccessToast = { dec, lat, lon ->
        str("gpsSuccessPattern", "Set declination to %1\$.1f° at (%2\$.2f, %3\$.2f)")
          .format(Locale.US, dec, lat, lon)
      },
      gpsFallbackToast = str("gpsFallbackToast"),
      reverseEwTitle = str("reverseEwTitle"),
      reverseEwDesc = str("reverseEwDesc"),
      levelCalibrationTitle = str("levelCalibrationTitle"),
      levelCalibrationDesc = { p, r ->
        str("levelCalibrationPattern", "Offset: P=%1\$.1f°, R=%2\$.1f°").format(Locale.US, p, r)
      },
      calibrateZeroBtn = str("calibrateZeroBtn"),
      resetCalibrationBtn = str("resetCalibrationBtn"),
      calibratedToast = str("calibratedToast"),
      voiceReadoutTitle = str("voiceReadoutTitle"),
      voiceReadoutDesc = str("voiceReadoutDesc"),
      simModeTitle = str("simModeTitle"),
      simModeDesc = str("simModeDesc"),
      openHelpBtn = str("openHelpBtn"),
      openOnboardingDebugBtn = str("openOnboardingDebugBtn"),
      fieldNoteSheetTitle = { count ->
        str("fieldNoteSheetPattern", "Digital Field Note (%1\$d)").format(Locale.US, count)
      },
      copyCsvBtn = str("copyCsvBtn"),
      emptyFieldNoteText = str("emptyFieldNoteText"),
      onboardingStep1Title = str("onboardingStep1Title"),
      onboardingStep1Body = str("onboardingStep1Body"),
      onboardingStep2Title = str("onboardingStep2Title"),
      onboardingStep2Body = str("onboardingStep2Body"),
      onboardingStep3Title = str("onboardingStep3Title"),
      onboardingStep3Body = str("onboardingStep3Body"),
      onboardingStep4Title = str("onboardingStep4Title"),
      onboardingStep4Body = str("onboardingStep4Body"),
      nextBtn = str("nextBtn", "Next"),
      backBtn = str("backBtn", "Back"),
      startBtn = str("startBtn", "Start"),
      skipBtn = str("skipBtn", "Skip"),
      helpPageTitle = str("helpPageTitle"),
      helpPageSubtitle = str("helpPageSubtitle"),
      helpSec1Title = str("helpSec1Title"),
      helpStep1Title = str("helpStep1Title"),
      helpStep1Desc = str("helpStep1Desc"),
      helpStep2Title = str("helpStep2Title"),
      helpStep2Desc = str("helpStep2Desc"),
      helpStep3Title = str("helpStep3Title"),
      helpStep3Desc = str("helpStep3Desc"),
      helpStep4Title = str("helpStep4Title"),
      helpStep4Desc = str("helpStep4Desc"),
      helpSec2Title = str("helpSec2Title"),
      helpSurfaceModeHeading = str("helpSurfaceModeHeading"),
      helpSurfaceModeIntro = str("helpSurfaceModeIntro"),
      helpDiagramTitle = str("helpDiagramTitle"),
      helpDiagramBadge = str("helpDiagramBadge"),
      helpDiagramNormalLabel = str("helpDiagramNormalLabel"),
      helpDiagramPlaneLabel = str("helpDiagramPlaneLabel"),
      helpDiagramStrikeLabel = str("helpDiagramStrikeLabel"),
      helpDiagramDipLabel = str("helpDiagramDipLabel"),
      helpDiagramPoint1Title = str("helpDiagramPoint1Title"),
      helpDiagramPoint1Desc = str("helpDiagramPoint1Desc"),
      helpDiagramPoint2Title = str("helpDiagramPoint2Title"),
      helpDiagramPoint2Desc = str("helpDiagramPoint2Desc"),
      helpDiagramPoint3Title = str("helpDiagramPoint3Title"),
      helpDiagramPoint3Desc = str("helpDiagramPoint3Desc"),
      helpEdgeModeHeading = str("helpEdgeModeHeading"),
      helpEdgeModeIntro = str("helpEdgeModeIntro"),
      helpSec3Title = str("helpSec3Title"),
      helpNotation1Name = str("helpNotation1Name"),
      helpNotation1Desc = str("helpNotation1Desc"),
      helpNotation2Name = str("helpNotation2Name"),
      helpNotation2Desc = str("helpNotation2Desc"),
      helpNotation3Name = str("helpNotation3Name"),
      helpNotation3Desc = str("helpNotation3Desc"),
      helpSec4Title = str("helpSec4Title"),
      helpSec4Body = str("helpSec4Body"),
      helpSec5Title = str("helpSec5Title"),
      helpFeat1Title = str("helpFeat1Title"),
      helpFeat1Desc = str("helpFeat1Desc"),
      helpFeat2Title = str("helpFeat2Title"),
      helpFeat2Desc = str("helpFeat2Desc"),
      helpFeat3Title = str("helpFeat3Title"),
      helpFeat3Desc = str("helpFeat3Desc"),
      helpFeat4Title = str("helpFeat4Title"),
      helpFeat4Desc = str("helpFeat4Desc"),
      helpWebCardTitle = str("helpWebCardTitle"),
      helpWebCardDesc = str("helpWebCardDesc"),
      helpWikiBtn = str("helpWikiBtn"),
      helpWikiUrl = str("helpWikiUrl", "https://en.wikipedia.org/wiki/Strike_and_dip"),
      helpSurveyBtn = str("helpSurveyBtn"),
      helpSurveyUrl = str("helpSurveyUrl", "https://www.gsj.jp/"),
      aboutSectionTitle = str("aboutSectionTitle", "About & Credits"),
      madeInOkinawaBadge = str("madeInOkinawaBadge", "🌺 Made in Okinawa, Japan"),
      authorLabel = str("authorLabel", "Author: @JustCat3456"),
      whatsNewBtn = str("whatsNewBtn", "What's New / Release Notes"),
      githubSourceBtn = str("githubSourceBtn", "GitHub Source Code"),
      licenseBtn = str("licenseBtn", "License (MIT)"),
      whatsNewDialogTitle = str("whatsNewDialogTitle", "What's New"),
      whatsNewV110Title = str("whatsNewV110Title"),
      whatsNewV110Body = str("whatsNewV110Body"),
      whatsNewV100Title = str("whatsNewV100Title"),
      whatsNewV100Body = str("whatsNewV100Body"),
      openGithubReleasesBtn = str("openGithubReleasesBtn", "View GitHub Releases"),
      licenseDialogTitle = str("licenseDialogTitle", "Open Source License (MIT)"),
      whatIsKayoBtn = str("whatIsKayoBtn", "🌺 What is \"Kayo\"?"),
      whatIsKayoTitle = str("whatIsKayoTitle"),
      whatIsKayoSubtitle = str("whatIsKayoSubtitle"),
      whatIsKayoSec1Title = str("whatIsKayoSec1Title"),
      whatIsKayoSec1Body = str("whatIsKayoSec1Body"),
      whatIsKayoSec2Title = str("whatIsKayoSec2Title"),
      whatIsKayoSec2Body = str("whatIsKayoSec2Body"),
      whatIsKayoSec3Title = str("whatIsKayoSec3Title"),
      whatIsKayoSec3Body = str("whatIsKayoSec3Body"),
      whatIsKayoPhotoCaption = str("whatIsKayoPhotoCaption"),
      whatIsKayoPhotoCredit = str("whatIsKayoPhotoCredit"),
      whatIsKayoPhotoUrl = str(
        "whatIsKayoPhotoUrl",
        "https://commons.wikimedia.org/wiki/File:Outcrop_of_folded_turbidite_at_Cape_Ban_in_Nago_202609_01.jpg"
      ),
      whatIsKayoLinkBtn = str("whatIsKayoLinkBtn"),
      whatIsKayoLinkUrl = str("whatIsKayoLinkUrl", "https://oki-park.jp/sp/bunkazai/detail/106")
    )

    cache[language] = loaded
    return loaded
  }

  private fun readAssetJson(context: Context, langCode: String): JSONObject? {
    return try {
      val jsonText = context.assets.open("i18n/$langCode.json")
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }
      JSONObject(jsonText)
    } catch (_: Exception) {
      null
    }
  }
}
