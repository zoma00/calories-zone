package com.zomba.cal.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zomba.cal.data.LocalAppStorage
import com.zomba.cal.domain.AiGuidanceEngine
import com.zomba.cal.domain.CalorieCalculator
import com.zomba.cal.domain.RuleBasedAiGuidanceEngine
import com.zomba.cal.model.ActivityLevel
import com.zomba.cal.model.CaloriePlan
import com.zomba.cal.model.Goal
import com.zomba.cal.model.MealLogEntry
import com.zomba.cal.model.MealTotals
import com.zomba.cal.model.SavedProfile
import com.zomba.cal.model.Sex
import com.zomba.cal.model.UserProfileInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CaloriesUiState(
    val profileName: String = "My profile",
    val age: String = "28",
    val heightCm: String = "175",
    val weightKg: String = "78",
    val sex: Sex = Sex.Male,
    val activityLevel: ActivityLevel = ActivityLevel.Moderate,
    val goal: Goal = Goal.Maintain,
    val meals: List<MealLogEntry> = emptyList(),
    val mealTotals: MealTotals = MealTotals(),
    val mealName: String = "",
    val mealCalories: String = "",
    val mealProtein: String = "",
    val mealCarbs: String = "",
    val mealFat: String = "",
    val result: CaloriePlan? = null,
    val guidanceNotes: List<String> = emptyList(),
    val validationMessage: String? = null,
    val statusMessage: String? = null
)

class CaloriesViewModel(
    private val calculator: CalorieCalculator = CalorieCalculator(),
    private val aiGuidanceEngine: AiGuidanceEngine = RuleBasedAiGuidanceEngine(),
    private val storage: LocalAppStorage? = null
) : ViewModel() {
    var uiState by mutableStateOf(CaloriesUiState())
        private set

    init {
        val savedMeals = storage?.loadMeals().orEmpty()
        val savedProfile = storage?.loadProfile()
        uiState = if (savedProfile != null) {
            CaloriesUiState(
                profileName = savedProfile.name,
                age = savedProfile.age,
                heightCm = savedProfile.heightCm,
                weightKg = savedProfile.weightKg,
                sex = savedProfile.sex,
                activityLevel = savedProfile.activityLevel,
                goal = savedProfile.goal,
                meals = savedMeals,
                mealTotals = calculateMealTotals(savedMeals)
            )
        } else {
            CaloriesUiState(
                meals = savedMeals,
                mealTotals = calculateMealTotals(savedMeals)
            )
        }
        generatePlan()
    }

    fun updateProfileName(value: String) {
        uiState = uiState.copy(profileName = value, validationMessage = null, statusMessage = null)
    }

    fun updateAge(value: String) {
        updateState(age = value.filter(Char::isDigit))
    }

    fun updateHeight(value: String) {
        updateState(heightCm = sanitizeDecimal(value))
    }

    fun updateWeight(value: String) {
        updateState(weightKg = sanitizeDecimal(value))
    }

    fun updateSex(value: Sex) {
        uiState = uiState.copy(sex = value, validationMessage = null, statusMessage = null)
    }

    fun updateActivityLevel(value: ActivityLevel) {
        uiState = uiState.copy(activityLevel = value, validationMessage = null, statusMessage = null)
    }

    fun updateGoal(value: Goal) {
        uiState = uiState.copy(goal = value, validationMessage = null, statusMessage = null)
    }

    fun updateMealName(value: String) {
        uiState = uiState.copy(mealName = value, validationMessage = null, statusMessage = null)
    }

    fun updateMealCalories(value: String) {
        uiState = uiState.copy(mealCalories = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateMealProtein(value: String) {
        uiState = uiState.copy(mealProtein = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateMealCarbs(value: String) {
        uiState = uiState.copy(mealCarbs = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateMealFat(value: String) {
        uiState = uiState.copy(mealFat = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun saveProfile() {
        val profileName = uiState.profileName.trim()
        val input = parseInput()

        if (profileName.isBlank()) {
            uiState = uiState.copy(
                validationMessage = "Enter a profile name before saving.",
                statusMessage = null
            )
            return
        }

        if (input == null) {
            uiState = uiState.copy(
                result = null,
                guidanceNotes = emptyList(),
                validationMessage = "Enter a realistic age, height, and weight before saving a profile.",
                statusMessage = null
            )
            return
        }

        storage?.saveProfile(
            SavedProfile(
                name = profileName,
                age = uiState.age,
                heightCm = uiState.heightCm,
                weightKg = uiState.weightKg,
                sex = uiState.sex,
                activityLevel = uiState.activityLevel,
                goal = uiState.goal
            )
        )
        applyPlan(input, statusMessage = "Profile saved on this device.")
    }

    fun addMeal() {
        val mealName = uiState.mealName.trim()
        val mealCalories = uiState.mealCalories.toIntOrNull()
        val mealProtein = parseOptionalWholeNumber(uiState.mealProtein)
        val mealCarbs = parseOptionalWholeNumber(uiState.mealCarbs)
        val mealFat = parseOptionalWholeNumber(uiState.mealFat)

        if (mealName.isBlank() || mealCalories == null || mealCalories <= 0 || mealProtein == null || mealCarbs == null || mealFat == null) {
            uiState = uiState.copy(
                validationMessage = "Enter a meal name, calories, and optional non-negative macros before logging the meal.",
                statusMessage = null
            )
            return
        }

        val mealEntry = MealLogEntry(
            id = System.currentTimeMillis().toString(),
            name = mealName,
            calories = mealCalories,
            proteinGrams = mealProtein,
            carbsGrams = mealCarbs,
            fatGrams = mealFat,
            loggedAtLabel = LocalDateTime.now().format(MEAL_TIME_FORMATTER)
        )
        val updatedMeals = listOf(mealEntry) + uiState.meals
        storage?.saveMeals(updatedMeals)

        uiState = uiState.copy(
            meals = updatedMeals,
            mealTotals = calculateMealTotals(updatedMeals),
            mealName = "",
            mealCalories = "",
            mealProtein = "",
            mealCarbs = "",
            mealFat = "",
            validationMessage = null,
            statusMessage = "Meal logged locally."
        )
    }

    fun generatePlan() {
        val input = parseInput()
        if (input == null) {
            uiState = uiState.copy(
                result = null,
                guidanceNotes = emptyList(),
                validationMessage = "Enter a realistic age, height, and weight before generating a plan.",
                statusMessage = null
            )
            return
        }

        applyPlan(input)
    }

    private fun updateState(
        age: String = uiState.age,
        heightCm: String = uiState.heightCm,
        weightKg: String = uiState.weightKg
    ) {
        uiState = uiState.copy(
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            validationMessage = null,
            statusMessage = null
        )
    }

    private fun applyPlan(input: UserProfileInput, statusMessage: String? = null) {
        val result = calculator.createPlan(input)
        uiState = uiState.copy(
            result = result,
            guidanceNotes = aiGuidanceEngine.buildInsights(input, result),
            validationMessage = null,
            statusMessage = statusMessage
        )
    }

    private fun parseInput(): UserProfileInput? {
        val age = uiState.age.toIntOrNull()
        val heightCm = uiState.heightCm.toDoubleOrNull()
        val weightKg = uiState.weightKg.toDoubleOrNull()

        if (age == null || heightCm == null || weightKg == null) {
            return null
        }

        if (age !in 15..90 || heightCm !in 120.0..240.0 || weightKg !in 35.0..300.0) {
            return null
        }

        return UserProfileInput(
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            sex = uiState.sex,
            activityLevel = uiState.activityLevel,
            goal = uiState.goal
        )
    }

    private fun parseOptionalWholeNumber(value: String): Int? {
        if (value.isBlank()) {
            return 0
        }
        return value.toIntOrNull()?.takeIf { it >= 0 }
    }

    private fun calculateMealTotals(meals: List<MealLogEntry>): MealTotals = meals.fold(MealTotals()) { totals, meal ->
        MealTotals(
            calories = totals.calories + meal.calories,
            proteinGrams = totals.proteinGrams + meal.proteinGrams,
            carbsGrams = totals.carbsGrams + meal.carbsGrams,
            fatGrams = totals.fatGrams + meal.fatGrams
        )
    }

    private fun sanitizeDecimal(value: String): String {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val dotIndex = filtered.indexOf('.')
        if (dotIndex == -1) {
            return filtered
        }

        val whole = filtered.substring(0, dotIndex + 1)
        val fraction = filtered.substring(dotIndex + 1).replace(".", "")
        return whole + fraction
    }

    companion object {
        private val MEAL_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

        fun factory(storage: LocalAppStorage): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CaloriesViewModel::class.java)) {
                    return CaloriesViewModel(storage = storage) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
