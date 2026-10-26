package com.example.clinometer.data

import android.content.Context
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.MeasurementRecord
import com.example.clinometer.model.NotationFormat
import org.json.JSONArray
import org.json.JSONObject

/**
 * アプリ設定およびデジタル野帳レコードをローカルに永続化するストレージクラス
 */
class ClinometerStorage(context: Context) {

  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  data class SavedSettings(
    val hasCompletedOnboarding: Boolean,
    val language: AppLanguage,
    val measureMode: MeasureMode,
    val notationFormat: NotationFormat,
    val reverseEastWest: Boolean,
    val declinationDeg: Float,
    val pitchOffsetDeg: Float,
    val rollOffsetDeg: Float,
    val voiceReadoutEnabled: Boolean
  )

  fun loadSettings(): SavedSettings {
    val langCode = prefs.getString(KEY_LANGUAGE, AppLanguage.JA.code) ?: AppLanguage.JA.code
    val language = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.JA

    val modeName = prefs.getString(KEY_MEASURE_MODE, MeasureMode.SURFACE.name)
    val measureMode = MeasureMode.entries.find { it.name == modeName } ?: MeasureMode.SURFACE

    val formatName = prefs.getString(KEY_NOTATION_FORMAT, NotationFormat.JAPANESE.name)
    val notationFormat =
      NotationFormat.entries.find { it.name == formatName } ?: NotationFormat.JAPANESE

    return SavedSettings(
      hasCompletedOnboarding = prefs.getBoolean(KEY_ONBOARDING_DONE, false),
      language = language,
      measureMode = measureMode,
      notationFormat = notationFormat,
      reverseEastWest = prefs.getBoolean(KEY_REVERSE_EW, true),
      declinationDeg = prefs.getFloat(KEY_DECLINATION, -5.5f),
      pitchOffsetDeg = prefs.getFloat(KEY_PITCH_OFFSET, 0f),
      rollOffsetDeg = prefs.getFloat(KEY_ROLL_OFFSET, 0f),
      voiceReadoutEnabled = prefs.getBoolean(KEY_VOICE_READOUT, false)
    )
  }

  fun saveOnboardingCompleted(completed: Boolean) {
    prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
  }

  fun saveLanguage(language: AppLanguage) {
    prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
  }

  fun saveMeasureMode(mode: MeasureMode) {
    prefs.edit().putString(KEY_MEASURE_MODE, mode.name).apply()
  }

  fun saveNotationFormat(format: NotationFormat) {
    prefs.edit().putString(KEY_NOTATION_FORMAT, format.name).apply()
  }

  fun saveReverseEastWest(reverse: Boolean) {
    prefs.edit().putBoolean(KEY_REVERSE_EW, reverse).apply()
  }

  fun saveDeclination(declinationDeg: Float) {
    prefs.edit().putFloat(KEY_DECLINATION, declinationDeg).apply()
  }

  fun saveLevelCalibration(pitchOffsetDeg: Float, rollOffsetDeg: Float) {
    prefs.edit()
      .putFloat(KEY_PITCH_OFFSET, pitchOffsetDeg)
      .putFloat(KEY_ROLL_OFFSET, rollOffsetDeg)
      .apply()
  }

  fun saveVoiceReadout(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_VOICE_READOUT, enabled).apply()
  }

  fun loadRecords(): List<MeasurementRecord> {
    val rawJson = prefs.getString(KEY_RECORDS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(rawJson)
      val list = ArrayList<MeasurementRecord>(array.length())
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val modeStr = obj.optString("mode", MeasureMode.SURFACE.name)
        val mode = MeasureMode.entries.find { it.name == modeStr } ?: MeasureMode.SURFACE
        val lat = if (obj.has("latitude") && !obj.isNull("latitude")) {
          obj.getDouble("latitude")
        } else {
          null
        }
        val lon = if (obj.has("longitude") && !obj.isNull("longitude")) {
          obj.getDouble("longitude")
        } else {
          null
        }
        list.add(
          MeasurementRecord(
            id = obj.getLong("id"),
            timestampMillis = obj.getLong("timestampMillis"),
            strikeFormatted = obj.getString("strikeFormatted"),
            dipFormatted = obj.getString("dipFormatted"),
            strikeAzimuth = obj.getDouble("strikeAzimuth").toFloat(),
            dipAngle = obj.getDouble("dipAngle").toFloat(),
            dipDirection = obj.getDouble("dipDirection").toFloat(),
            mode = mode,
            note = obj.optString("note", ""),
            latitude = lat,
            longitude = lon
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveRecords(records: List<MeasurementRecord>) {
    val array = JSONArray()
    for (rec in records) {
      val obj = JSONObject().apply {
        put("id", rec.id)
        put("timestampMillis", rec.timestampMillis)
        put("strikeFormatted", rec.strikeFormatted)
        put("dipFormatted", rec.dipFormatted)
        put("strikeAzimuth", rec.strikeAzimuth.toDouble())
        put("dipAngle", rec.dipAngle.toDouble())
        put("dipDirection", rec.dipDirection.toDouble())
        put("mode", rec.mode.name)
        put("note", rec.note)
        if (rec.latitude != null) put("latitude", rec.latitude)
        if (rec.longitude != null) put("longitude", rec.longitude)
      }
      array.put(obj)
    }
    prefs.edit().putString(KEY_RECORDS_JSON, array.toString()).apply()
  }

  companion object {
    private const val PREFS_NAME = "clinometer_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_MEASURE_MODE = "measure_mode"
    private const val KEY_NOTATION_FORMAT = "notation_format"
    private const val KEY_REVERSE_EW = "reverse_ew"
    private const val KEY_DECLINATION = "declination_deg"
    private const val KEY_PITCH_OFFSET = "pitch_offset_deg"
    private const val KEY_ROLL_OFFSET = "roll_offset_deg"
    private const val KEY_VOICE_READOUT = "voice_readout"
    private const val KEY_RECORDS_JSON = "records_json"
  }
}
