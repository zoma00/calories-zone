package com.calories.zone.model

enum class Sex(val label: String) {
    Male("Male"),
    Female("Female")
}

enum class ActivityLevel(val label: String, val multiplier: Double) {
    Sedentary("Sedentary", 1.2),
    Light("Light", 1.375),
    Moderate("Moderate", 1.55),
    VeryActive("Very active", 1.725),
    Athlete("Athlete", 1.9)
}

enum class Goal(val label: String, val calorieMultiplier: Double, val proteinMultiplier: Double) {
    LoseFat("Lose fat", 0.85, 2.0),
    Maintain("Maintain", 1.0, 1.8),
    BuildMuscle("Build muscle", 1.1, 2.2)
}

enum class MealEntryUnit(val label: String, val shortLabel: String, val estimatorToken: String) {
    Grams("Grams (g)", "g", "g"),
    Milliliters("Milliliters (ml)", "ml", "ml"),
    Pounds("Pounds (lb)", "lb", "lb")
}

data class CustomFoodProfile(
    val id: String,
    val name: String,
    val caloriesPer100g: Int,
    val proteinPer100g: Int,
    val carbsPer100g: Int,
    val fatPer100g: Int
)

data class UserProfileInput(
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val sex: Sex,
    val activityLevel: ActivityLevel,
    val goal: Goal
)

data class CaloriePlan(
    val bmi: Double,
    val bmr: Int,
    val maintenanceCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

data class SavedProfile(
    val name: String,
    val age: String,
    val heightCm: String,
    val weightKg: String,
    val sex: Sex,
    val activityLevel: ActivityLevel,
    val goal: Goal
)

data class MealLogEntry(
    val id: String,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val loggedAtLabel: String
)

data class MealTotals(
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0
)
