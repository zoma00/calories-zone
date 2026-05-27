package com.calories.zone.data

import android.content.Context
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.ChatMessage
import com.calories.zone.model.ChatRole
import com.calories.zone.model.CustomFoodProfile
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
                        loggedAtLabel = item.getString("loggedAtLabel"),
                        matchedFoods = item.optJSONArray("matchedFoods")?.toStringList().orEmpty()
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
                    .put("matchedFoods", JSONArray(meal.matchedFoods))
            )
        }

        preferences.edit().putString(KEY_MEALS, payload.toString()).apply()
    }

    fun loadCustomFoods(): List<CustomFoodProfile> = runCatching {
        val rawFoods = preferences.getString(KEY_CUSTOM_FOODS, null) ?: return emptyList()
        val foodsArray = JSONArray(rawFoods)
        buildList {
            for (index in 0 until foodsArray.length()) {
                val item = foodsArray.getJSONObject(index)
                add(
                    CustomFoodProfile(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        caloriesPer100g = item.getInt("caloriesPer100g"),
                        proteinPer100g = item.getInt("proteinPer100g"),
                        carbsPer100g = item.getInt("carbsPer100g"),
                        fatPer100g = item.getInt("fatPer100g")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

    fun saveCustomFoods(foods: List<CustomFoodProfile>) {
        val payload = JSONArray()
        foods.forEach { food ->
            payload.put(
                JSONObject()
                    .put("id", food.id)
                    .put("name", food.name)
                    .put("caloriesPer100g", food.caloriesPer100g)
                    .put("proteinPer100g", food.proteinPer100g)
                    .put("carbsPer100g", food.carbsPer100g)
                    .put("fatPer100g", food.fatPer100g)
            )
        }

        preferences.edit().putString(KEY_CUSTOM_FOODS, payload.toString()).apply()
    }

    fun loadChatMessages(): List<ChatMessage> = runCatching {
        val rawMessages = preferences.getString(KEY_CHAT_MESSAGES, null) ?: return emptyList()
        val messagesArray = JSONArray(rawMessages)
        buildList {
            for (index in 0 until messagesArray.length()) {
                val item = messagesArray.getJSONObject(index)
                add(
                    ChatMessage(
                        id = item.getString("id"),
                        role = ChatRole.valueOf(item.getString("role")),
                        text = item.getString("text"),
                        createdAtLabel = item.getString("createdAtLabel")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val payload = JSONArray()
        messages.forEach { message ->
            payload.put(
                JSONObject()
                    .put("id", message.id)
                    .put("role", message.role.name)
                    .put("text", message.text)
                    .put("createdAtLabel", message.createdAtLabel)
            )
        }

        preferences.edit().putString(KEY_CHAT_MESSAGES, payload.toString()).apply()
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(optString(index))
        }
    }

    private companion object {
        const val STORAGE_NAME = "calories_zone_storage"
        const val KEY_PROFILE = "saved_profile"
        const val KEY_MEALS = "meal_logs"
        const val KEY_CUSTOM_FOODS = "custom_foods"
        const val KEY_CHAT_MESSAGES = "chat_messages"
    }
}
