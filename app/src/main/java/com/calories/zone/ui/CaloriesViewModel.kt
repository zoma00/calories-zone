package com.calories.zone.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.calories.zone.domain.AiChatEngine
import com.calories.zone.data.LocalAppStorage
import com.calories.zone.domain.AiGuidanceEngine
import com.calories.zone.domain.CalorieCalculator
import com.calories.zone.domain.ChatContext
import com.calories.zone.domain.MealNutritionEstimate
import com.calories.zone.domain.MealNutritionEstimator
import com.calories.zone.domain.NutritionGuideCalculator
import com.calories.zone.domain.PolicyAwareAiChatEngine
import com.calories.zone.domain.RuleBasedAiGuidanceEngine
import com.calories.zone.domain.RuleBasedMealNutritionEstimator
import com.calories.zone.domain.RuleBasedNutritionGuideCalculator
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.ChatMessage
import com.calories.zone.model.ChatRole
import com.calories.zone.model.CustomFoodProfile
import com.calories.zone.model.Goal
import com.calories.zone.model.MealEntryUnit
import com.calories.zone.model.MealLogEntry
import com.calories.zone.model.MealTotals
import com.calories.zone.model.NutritionGuideScore
import com.calories.zone.model.SavedProfile
import com.calories.zone.model.Sex
import com.calories.zone.model.UserProfileInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CaloriesUiState(
    val profileName: String = "My profile",
    val age: String = "28",
    val heightCm: String = "175",
    val weightKg: String = "78",
    val sex: Sex = Sex.Male,
    val activityLevel: ActivityLevel = ActivityLevel.Moderate,
    val goal: Goal = Goal.Maintain,
    val customFoods: List<CustomFoodProfile> = emptyList(),
    val customFoodName: String = "",
    val customFoodCalories: String = "",
    val customFoodProtein: String = "",
    val customFoodCarbs: String = "",
    val customFoodFat: String = "",
    val meals: List<MealLogEntry> = emptyList(),
    val mealTotals: MealTotals = MealTotals(),
    val mealName: String = "",
    val mealEntryAmount: String = "",
    val mealEntryUnit: MealEntryUnit = MealEntryUnit.Grams,
    val mealCalories: String = "",
    val mealProtein: String = "",
    val mealCarbs: String = "",
    val mealFat: String = "",
    val result: CaloriePlan? = null,
    val guidanceNotes: List<String> = emptyList(),
    val nutritionGuideScore: NutritionGuideScore = NutritionGuideScore(),
    val chatInput: String = "",
    val chatMessages: List<ChatMessage> = emptyList(),
    val chatStatusMessage: String? = null,
    val validationMessage: String? = null,
    val statusMessage: String? = null
)

class CaloriesViewModel(
    private val calculator: CalorieCalculator = CalorieCalculator(),
    private val aiGuidanceEngine: AiGuidanceEngine = RuleBasedAiGuidanceEngine(),
    private val aiChatEngine: AiChatEngine = PolicyAwareAiChatEngine(),
    private val mealNutritionEstimator: MealNutritionEstimator = RuleBasedMealNutritionEstimator(),
    private val nutritionGuideCalculator: NutritionGuideCalculator = RuleBasedNutritionGuideCalculator(),
    private val storage: LocalAppStorage? = null
) : ViewModel() {
    var uiState by mutableStateOf(CaloriesUiState())
        private set

    private var localMessageSequence: Long = 0

    init {
        val savedMeals = storage?.loadMeals().orEmpty()
        val savedCustomFoods = storage?.loadCustomFoods().orEmpty()
        val savedChatMessages = storage?.loadChatMessages().orEmpty()
        val initialChatMessages = savedChatMessages.ifEmpty {
            listOf(createChatMessage(role = ChatRole.Assistant, text = DEFAULT_CHAT_GREETING))
        }
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
                customFoods = savedCustomFoods,
                meals = savedMeals,
                mealTotals = calculateMealTotals(savedMeals),
                chatMessages = initialChatMessages
            )
        } else {
            CaloriesUiState(
                customFoods = savedCustomFoods,
                meals = savedMeals,
                mealTotals = calculateMealTotals(savedMeals),
                chatMessages = initialChatMessages
            )
        }

        if (savedChatMessages.isEmpty()) {
            storage?.saveChatMessages(initialChatMessages)
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

    fun updateChatInput(value: String) {
        uiState = uiState.copy(chatInput = value, chatStatusMessage = null)
    }

    fun sendChatMessage() {
        val userText = uiState.chatInput.trim()
        if (userText.isBlank()) {
            uiState = uiState.copy(chatStatusMessage = "Type a message before sending.")
            return
        }

        val userMessage = createChatMessage(role = ChatRole.User, text = userText)
        val withUserMessage = (uiState.chatMessages + userMessage).takeLast(MAX_CHAT_MESSAGES)
        uiState = uiState.copy(
            chatMessages = withUserMessage,
            chatInput = "",
            chatStatusMessage = null,
            validationMessage = null,
            statusMessage = null
        )

        val reply = aiChatEngine.reply(
            userMessage = userText,
            context = buildChatContext(),
            recentMessages = withUserMessage
        )

        val assistantMessage = createChatMessage(role = ChatRole.Assistant, text = reply.message)
        val updatedMessages = (withUserMessage + assistantMessage).takeLast(MAX_CHAT_MESSAGES)
        storage?.saveChatMessages(updatedMessages)

        uiState = uiState.copy(
            chatMessages = updatedMessages,
            chatStatusMessage = if (reply.blocked) {
                "Medical requests are limited to general wellness safety guidance."
            } else {
                null
            },
            validationMessage = null,
            statusMessage = null
        )
    }

    fun updateMealName(value: String) {
        uiState = uiState.copy(mealName = value, validationMessage = null, statusMessage = null)
        recalculateMealEstimate()
    }

    fun updateMealEntryAmount(value: String) {
        uiState = uiState.copy(
            mealEntryAmount = sanitizeDecimal(value),
            validationMessage = null,
            statusMessage = null
        )
        recalculateMealEstimate()
    }

    fun updateMealEntryUnit(value: MealEntryUnit) {
        uiState = uiState.copy(mealEntryUnit = value, validationMessage = null, statusMessage = null)
        recalculateMealEstimate()
    }

    fun updateCustomFoodName(value: String) {
        uiState = uiState.copy(customFoodName = value, validationMessage = null, statusMessage = null)
    }

    fun updateCustomFoodCalories(value: String) {
        uiState = uiState.copy(customFoodCalories = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateCustomFoodProtein(value: String) {
        uiState = uiState.copy(customFoodProtein = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateCustomFoodCarbs(value: String) {
        uiState = uiState.copy(customFoodCarbs = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun updateCustomFoodFat(value: String) {
        uiState = uiState.copy(customFoodFat = value.filter(Char::isDigit), validationMessage = null, statusMessage = null)
    }

    fun addCustomFood() {
        val name = uiState.customFoodName.trim()
        val calories = uiState.customFoodCalories.toIntOrNull()
        val protein = parseOptionalWholeNumber(uiState.customFoodProtein)
        val carbs = parseOptionalWholeNumber(uiState.customFoodCarbs)
        val fat = parseOptionalWholeNumber(uiState.customFoodFat)

        if (name.isBlank() || calories == null || calories <= 0 || protein == null || carbs == null || fat == null) {
            uiState = uiState.copy(
                validationMessage = "Enter custom food name, calories per 100g, and optional non-negative macros.",
                statusMessage = null
            )
            return
        }

        val customFood = CustomFoodProfile(
            id = System.currentTimeMillis().toString(),
            name = name,
            caloriesPer100g = calories,
            proteinPer100g = protein,
            carbsPer100g = carbs,
            fatPer100g = fat
        )

        val updatedFoods = listOf(customFood) + uiState.customFoods
        storage?.saveCustomFoods(updatedFoods)

        uiState = uiState.copy(
            customFoods = updatedFoods,
            customFoodName = "",
            customFoodCalories = "",
            customFoodProtein = "",
            customFoodCarbs = "",
            customFoodFat = "",
            validationMessage = null,
            statusMessage = null
        )
        recalculateMealEstimate()
        uiState = uiState.copy(statusMessage = "Custom food saved locally.", validationMessage = null)
    }

    fun deleteCustomFood(foodId: String) {
        val updatedFoods = uiState.customFoods.filterNot { it.id == foodId }
        if (updatedFoods.size == uiState.customFoods.size) {
            return
        }

        storage?.saveCustomFoods(updatedFoods)
        uiState = uiState.copy(customFoods = updatedFoods, validationMessage = null, statusMessage = null)
        recalculateMealEstimate()
        uiState = uiState.copy(statusMessage = "Custom food removed.", validationMessage = null)
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
        val mealAmount = uiState.mealEntryAmount.toDoubleOrNull()

        if (mealName.isBlank() || mealAmount == null || mealAmount <= 0.0) {
            uiState = uiState.copy(
                validationMessage = "Enter a meal name and meal entry amount before logging the meal.",
                statusMessage = null
            )
            return
        }

        val estimate = estimateNutrition(mealName = mealName, mealAmount = mealAmount, mealEntryUnit = uiState.mealEntryUnit)
        if (estimate == null) {
            uiState = uiState.copy(
                validationMessage = "Could not auto-estimate this meal. Include foods like eggs, rice, chicken, milk, or fruit.",
                statusMessage = null
            )
            return
        }

        val mealEntryName = "$mealName (${formatAmount(mealAmount)} ${uiState.mealEntryUnit.shortLabel})"

        val mealEntry = MealLogEntry(
            id = System.currentTimeMillis().toString(),
            name = mealEntryName,
            calories = estimate.calories,
            proteinGrams = estimate.proteinGrams,
            carbsGrams = estimate.carbsGrams,
            fatGrams = estimate.fatGrams,
            loggedAtLabel = LocalDateTime.now().format(TIME_LABEL_FORMATTER),
            matchedFoods = estimate.matchedFoods
        )
        val updatedMeals = listOf(mealEntry) + uiState.meals
        storage?.saveMeals(updatedMeals)
        val updatedGuideScore = nutritionGuideCalculator.calculate(parseInput(), uiState.result, updatedMeals)

        uiState = uiState.copy(
            meals = updatedMeals,
            mealTotals = calculateMealTotals(updatedMeals),
            nutritionGuideScore = updatedGuideScore,
            mealName = "",
            mealEntryAmount = "",
            mealCalories = "",
            mealProtein = "",
            mealCarbs = "",
            mealFat = "",
            validationMessage = null,
            statusMessage = "Meal logged locally."
        )
    }

    fun deleteMeal(mealId: String) {
        val updatedMeals = uiState.meals.filterNot { it.id == mealId }
        if (updatedMeals.size == uiState.meals.size) {
            return
        }

        storage?.saveMeals(updatedMeals)
        uiState = uiState.copy(
            meals = updatedMeals,
            mealTotals = calculateMealTotals(updatedMeals),
            nutritionGuideScore = nutritionGuideCalculator.calculate(parseInput(), uiState.result, updatedMeals),
            validationMessage = null,
            statusMessage = "Meal removed."
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
            nutritionGuideScore = nutritionGuideCalculator.calculate(input, result, uiState.meals),
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

    private fun recalculateMealEstimate() {
        val mealName = uiState.mealName.trim()
        val mealAmount = uiState.mealEntryAmount.toDoubleOrNull()

        if (mealName.isBlank() || mealAmount == null || mealAmount <= 0.0) {
            uiState = uiState.copy(
                mealCalories = "",
                mealProtein = "",
                mealCarbs = "",
                mealFat = "",
                validationMessage = null,
                statusMessage = null
            )
            return
        }

        val estimate = estimateNutrition(mealName, mealAmount, uiState.mealEntryUnit)
        if (estimate == null) {
            uiState = uiState.copy(
                mealCalories = "",
                mealProtein = "",
                mealCarbs = "",
                mealFat = "",
                validationMessage = null,
                statusMessage = null
            )
            return
        }

        uiState = uiState.copy(
            mealCalories = estimate.calories.toString(),
            mealProtein = estimate.proteinGrams.toString(),
            mealCarbs = estimate.carbsGrams.toString(),
            mealFat = estimate.fatGrams.toString(),
            validationMessage = null,
            statusMessage = null
        )
    }

    private fun estimateNutrition(
        mealName: String,
        mealAmount: Double,
        mealEntryUnit: MealEntryUnit,
        customFoods: List<CustomFoodProfile> = uiState.customFoods
    ): MealNutritionEstimate? {
        val estimatorPrompt = "${formatAmount(mealAmount)} ${mealEntryUnit.estimatorToken} $mealName"
        return mealNutritionEstimator.estimateFromText(estimatorPrompt, customFoods)
    }

    private fun formatAmount(amount: Double): String {
        val whole = amount.toInt().toDouble()
        return if (amount == whole) {
            amount.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", amount)
        }
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

    private fun buildChatContext(): ChatContext {
        return ChatContext(
            profileName = uiState.profileName,
            goal = uiState.goal,
            targetCalories = uiState.result?.targetCalories,
            mealTotals = uiState.mealTotals
        )
    }

    private fun createChatMessage(role: ChatRole, text: String): ChatMessage {
        localMessageSequence += 1
        return ChatMessage(
            id = "${System.currentTimeMillis()}-$localMessageSequence",
            role = role,
            text = text,
            createdAtLabel = LocalDateTime.now().format(TIME_LABEL_FORMATTER)
        )
    }

    companion object {
        private const val MAX_CHAT_MESSAGES = 30
        private const val DEFAULT_CHAT_GREETING = "I am your local wellness assistant. I can help with meal tracking, target progress, and general nutrition habits. I cannot provide medical advice."
        private val TIME_LABEL_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")

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
