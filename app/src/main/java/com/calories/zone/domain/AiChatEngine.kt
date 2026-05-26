package com.calories.zone.domain

import com.calories.zone.model.ChatMessage
import com.calories.zone.model.Goal
import com.calories.zone.model.MealTotals

data class ChatContext(
    val profileName: String,
    val goal: Goal,
    val targetCalories: Int?,
    val mealTotals: MealTotals
)

data class ChatPrompt(
    val systemInstruction: String,
    val userMessage: String,
    val recentMessages: List<ChatMessage>,
    val context: ChatContext
)

data class ChatReply(
    val message: String,
    val blocked: Boolean = false
)

interface ChatCompletionAdapter {
    fun complete(prompt: ChatPrompt): String
}

interface AiChatEngine {
    fun reply(userMessage: String, context: ChatContext, recentMessages: List<ChatMessage>): ChatReply
}

class PolicyAwareAiChatEngine(
    private val completionAdapter: ChatCompletionAdapter = RuleBasedChatCompletionAdapter()
) : AiChatEngine {

    override fun reply(userMessage: String, context: ChatContext, recentMessages: List<ChatMessage>): ChatReply {
        val normalizedUserMessage = userMessage.trim().lowercase()
        if (normalizedUserMessage.isBlank()) {
            return ChatReply(
                message = "Type a question and I can help with meal tracking and general wellness guidance.",
                blocked = false
            )
        }

        if (containsMedicalTerms(normalizedUserMessage)) {
            return ChatReply(message = NON_MEDICAL_BOUNDARY_MESSAGE, blocked = true)
        }

        val prompt = ChatPrompt(
            systemInstruction = SYSTEM_INSTRUCTION,
            userMessage = userMessage.trim(),
            recentMessages = recentMessages.takeLast(MAX_CONTEXT_MESSAGES),
            context = context
        )
        val rawResponse = completionAdapter.complete(prompt).trim()

        if (rawResponse.isBlank()) {
            return ChatReply(message = FALLBACK_RESPONSE, blocked = false)
        }

        if (containsMedicalTerms(rawResponse.lowercase())) {
            return ChatReply(message = NON_MEDICAL_BOUNDARY_MESSAGE, blocked = true)
        }

        return ChatReply(message = rawResponse, blocked = false)
    }

    private fun containsMedicalTerms(text: String): Boolean {
        return MEDICAL_RISK_TERMS.any { term ->
            Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)
        }
    }

    private companion object {
        const val MAX_CONTEXT_MESSAGES = 10

        val MEDICAL_RISK_TERMS = listOf(
            "diagnose",
            "diagnosis",
            "treat",
            "treatment",
            "cure",
            "prevent",
            "prescription",
            "medication",
            "medicine",
            "disease",
            "diabetes",
            "hypertension",
            "blood pressure",
            "cancer",
            "asthma",
            "thyroid",
            "pcos",
            "anemia",
            "heart attack",
            "stroke"
        )

        const val NON_MEDICAL_BOUNDARY_MESSAGE = "I can only provide general wellness guidance for meal tracking and nutrition habits. Calories Zone is not a medical device and does not diagnose, treat, cure, or prevent medical conditions. For medical questions, please consult a qualified healthcare professional."

        const val FALLBACK_RESPONSE = "I can help with meal logging, calorie targets, and simple daily nutrition habits. Ask about meal ideas, target progress, or macro planning."

        const val SYSTEM_INSTRUCTION = "You are a local wellness assistant for Calories Zone. Keep answers short and practical. You must only provide general wellness guidance for meal tracking, calorie awareness, hydration, activity, and consistency. Do not provide medical, diagnostic, treatment, cure, or disease-specific advice."
    }
}

class RuleBasedChatCompletionAdapter : ChatCompletionAdapter {

    private val mealNutritionEstimator: MealNutritionEstimator = RuleBasedMealNutritionEstimator()

    override fun complete(prompt: ChatPrompt): String {
        val normalized = prompt.userMessage.lowercase()
        val context = prompt.context

        if (containsAny(normalized, "remaining", "left", "over", "target", "today")) {
            return calorieStatusReply(context)
        }

        if (containsAny(normalized, "calorie", "calories", "kcal")) {
            val estimateReply = calorieEstimateReply(prompt.userMessage)
            if (estimateReply != null) {
                return estimateReply
            }
        }

        return when {
            containsAny(normalized, "macro", "protein", "carb", "fat") -> macroReply(context)
            containsAny(normalized, "meal", "breakfast", "lunch", "dinner", "snack", "idea") -> mealIdeaReply(context)
            containsAny(normalized, "water", "hydrate", "hydration") -> "Hydration baseline: aim for steady water intake through the day and add extra around training or very hot weather."
            containsAny(normalized, "data", "privacy", "store", "offline", "local") -> "Your profile, meals, and chat stay local on this device in this version."
            else -> generalReply(context)
        }
    }

    private fun calorieEstimateReply(userMessage: String): String? {
        val estimatorInput = toEstimatorInput(userMessage)
        if (estimatorInput.isBlank()) {
            return null
        }

        val estimate = mealNutritionEstimator.estimateFromText(estimatorInput) ?: return null
        return "Estimated for $estimatorInput: ${estimate.calories} kcal • P ${estimate.proteinGrams}g • C ${estimate.carbsGrams}g • F ${estimate.fatGrams}g."
    }

    private fun toEstimatorInput(userMessage: String): String {
        return userMessage
            .lowercase()
            .replace(Regex("[?!.:,;]"), " ")
            .replace(Regex("\\bhow\\s+much\\s+(?:calories?|kcal)\\s+(?:in|for)\\b"), " ")
            .replace(Regex("\\bhow\\s+many\\s+(?:calories?|kcal)\\s+(?:in|for)\\b"), " ")
            .replace(Regex("\\b(?:calories?|kcal)\\s+(?:in|for)\\b"), " ")
            .replace(Regex("\\b(?:boiled|grilled|fried|baked|steamed|roasted|raw)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calorieStatusReply(context: ChatContext): String {
        val target = context.targetCalories
        if (target == null) {
            return "Generate your calorie plan first, then I can compare your logged intake against your target."
        }

        val logged = context.mealTotals.calories
        val delta = target - logged
        return if (delta >= 0) {
            "You have logged $logged kcal and your target is $target kcal. You are $delta kcal under target today."
        } else {
            "You have logged $logged kcal and your target is $target kcal. You are ${-delta} kcal over target today."
        }
    }

    private fun macroReply(context: ChatContext): String {
        val goalLine = when (context.goal) {
            Goal.LoseFat -> "For fat loss, keep protein consistent and manage calories with mostly whole foods."
            Goal.Maintain -> "For maintenance, keep portions stable and monitor weekly weight trend."
            Goal.BuildMuscle -> "For muscle gain, keep protein high and place more carbs around training."
        }

        return "$goalLine A simple structure is 3 to 4 meals with protein in each meal and vegetables in at least 2 meals."
    }

    private fun mealIdeaReply(context: ChatContext): String {
        return when (context.goal) {
            Goal.LoseFat -> "Try a high-satiety plate: grilled chicken, large salad, olive oil, and fruit."
            Goal.Maintain -> "Try a balanced plate: rice, lean protein, vegetables, and yogurt."
            Goal.BuildMuscle -> "Try a higher-energy plate: rice or potatoes, salmon or beef, vegetables, and milk or yogurt."
        }
    }

    private fun generalReply(context: ChatContext): String {
        val targetLine = if (context.targetCalories == null) {
            "Generate a plan to get your target calories first."
        } else {
            "Your current target is ${context.targetCalories} kcal."
        }

        return "$targetLine Focus on consistency: log meals, compare against target, and adjust using weekly trends instead of single days."
    }

    private fun containsAny(text: String, vararg terms: String): Boolean = terms.any { term ->
        Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)
    }
}
