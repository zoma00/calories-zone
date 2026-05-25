package com.calories.zone.data

import android.content.Context
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.Goal
import com.calories.zone.model.MealLogEntry
import com.calories.zone.model.SavedProfile
import com.calories.zone.model.Sex
import org.json.JSONArray
import org.json.JSONObject

class LocalAppStorage(context: Context) {
    private val preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)

    fun loadProfile(): SavedProfile? = runCatching {
        val rawProfile = preferences.getString(KEY_PROFILE, null) ?: return null
        val payload = JSONObject(rawProfile)
        SavedProfile(
            name = payload.getString("name"),
            age = payload.getString("age"),
            heightCm = payload.getString("heightCm"),
            weightKg = payload.getString("weightKg"),
            sex = Sex.valueOf(payload.getString("sex")),
            activityLevel = ActivityLevel.valueOf(payload.getString("activityLevel")),
            goal = Goal.valueOf(payload.getString("goal"))
        )
    }.getOrNull()

    fun saveProfile(profile: SavedProfile) {
        val payload = JSONObject()
            .put("name", profile.name)
            .put("age", profile.age)
            .put("heightCm", profile.heightCm)
            .put("weightKg", profile.weightKg)
            .put("sex", profile.sex.name)
            .put("activityLevel", profile.activityLevel.name)
            .put("goal", profile.goal.name)

        preferences.edit().putString(KEY_PROFILE, payload.toString()).apply()
    }

    fun loadMeals(): List<MealLogEntry> = runCatching {
        val rawMeals = preferences.getString(KEY_MEALS, null) ?: return emptyList()
        val mealsArray = JSONArray(rawMeals)
        buildList {
            for (index in 0 until mealsArray.length()) {
                val item = mealsArray.getJSONObject(index)
                add(
                    MealLogEntry(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        calories = item.getInt("calories"),
                        proteinGrams = item.getInt("proteinGrams"),
                        carbsGrams = item.getInt("carbsGrams"),
                        fatGrams = item.getInt("fatGrams"),
                        loggedAtLabel = item.getString("loggedAtLabel")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

    fun saveMeals(meals: List<MealLogEntry>) {
        val payload = JSONArray()
        meals.forEach { meal ->
            payload.put(
                JSONObject()
                    .put("id", meal.id)
                    .put("name", meal.name)
                    .put("calories", meal.calories)
                    .put("proteinGrams", meal.proteinGrams)
                    .put("carbsGrams", meal.carbsGrams)
                    .put("fatGrams", meal.fatGrams)
                    .put("loggedAtLabel", meal.loggedAtLabel)
            )
        }

        preferences.edit().putString(KEY_MEALS, payload.toString()).apply()
    }

    private companion object {
        const val STORAGE_NAME = "calories_zone_storage"
        const val KEY_PROFILE = "saved_profile"
        const val KEY_MEALS = "meal_logs"
    }
}