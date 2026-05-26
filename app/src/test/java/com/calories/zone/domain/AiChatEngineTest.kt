package com.calories.zone.domain

import com.calories.zone.model.Goal
import com.calories.zone.model.MealTotals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatEngineTest {

    private val engine = PolicyAwareAiChatEngine(completionAdapter = RuleBasedChatCompletionAdapter())

    @Test
    fun reply_blocksMedicalRequests() {
        val reply = engine.reply(
            userMessage = "Can you diagnose diabetes and tell me treatment?",
            context = sampleContext(),
            recentMessages = emptyList()
        )

        assertTrue(reply.blocked)
        assertTrue(reply.message.contains("not a medical device", ignoreCase = true))
    }

    @Test
    fun reply_reportsCalorieDeltaWhenAskedForTodayStatus() {
        val reply = engine.reply(
            userMessage = "How many calories do I have left today?",
            context = sampleContext(),
            recentMessages = emptyList()
        )

        assertFalse(reply.blocked)
        assertTrue(reply.message.contains("700 kcal", ignoreCase = true))
        assertTrue(reply.message.contains("under target", ignoreCase = true))
    }

    @Test
    fun reply_asksUserToGeneratePlanWhenTargetMissing() {
        val reply = engine.reply(
            userMessage = "How many calories are remaining?",
            context = sampleContext(targetCalories = null),
            recentMessages = emptyList()
        )

        assertFalse(reply.blocked)
        assertTrue(reply.message.contains("Generate your calorie plan first", ignoreCase = true))
    }

    @Test
    fun reply_estimatesFoodCaloriesForQuantityQuestion() {
        val reply = engine.reply(
            userMessage = "how much calories in 50 gram boiled egg",
            context = sampleContext(),
            recentMessages = emptyList()
        )

        assertFalse(reply.blocked)
        assertTrue(reply.message.contains("Estimated for", ignoreCase = true))
        assertTrue(reply.message.contains("78 kcal", ignoreCase = true))
    }

    private fun sampleContext(targetCalories: Int? = 2500): ChatContext {
        return ChatContext(
            profileName = "My profile",
            goal = Goal.Maintain,
            targetCalories = targetCalories,
            mealTotals = MealTotals(calories = 1800, proteinGrams = 120, carbsGrams = 180, fatGrams = 60)
        )
    }
}
