package com.calories.zone.domain

import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.Goal
import com.calories.zone.model.MealLogEntry
import com.calories.zone.model.Sex
import com.calories.zone.model.UserProfileInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionGuideCalculatorTest {
    private val calculator = RuleBasedNutritionGuideCalculator()

    @Test
    fun calculate_scoresWholeFoodMealsHighly() {
        val score = calculator.calculate(
            input = sampleInput(),
            plan = samplePlan(),
            meals = listOf(
                meal(
                    name = "Chicken oats beans apple",
                    calories = 800,
                    proteinGrams = 70,
                    carbsGrams = 100,
                    fatGrams = 12,
                    matchedFoods = listOf("chicken breast", "oats", "black beans", "apple")
                )
            )
        )

        assertTrue(score.overallScore >= 75)
        assertEquals(92, score.slowCarbScore)
        assertEquals(100, score.processingScore)
        assertTrue(score.ageActivityScore > 80)
    }

    @Test
    fun calculate_penalizesProcessedFastCarbMeals() {
        val score = calculator.calculate(
            input = sampleInput(),
            plan = samplePlan(),
            meals = listOf(
                meal(
                    name = "Pizza soda candy",
                    calories = 1200,
                    proteinGrams = 25,
                    carbsGrams = 160,
                    fatGrams = 45,
                    matchedFoods = listOf("pizza", "soda", "candy")
                )
            )
        )

        assertTrue(score.overallScore < 70)
        assertEquals(45, score.slowCarbScore)
        assertEquals(25, score.processingScore)
        assertTrue(score.notes.any { it.contains("Processing guide") })
    }

    @Test
    fun calculate_usesSedentaryActivityInGuideScoreAndNotes() {
        val score = calculator.calculate(
            input = sampleInput(age = 62, activityLevel = ActivityLevel.Sedentary),
            plan = samplePlan(),
            meals = listOf(
                meal(
                    name = "Chicken and potato",
                    calories = 500,
                    proteinGrams = 45,
                    carbsGrams = 40,
                    fatGrams = 10,
                    matchedFoods = listOf("chicken breast", "potato")
                )
            )
        )

        assertTrue(score.ageActivityScore < 80)
        assertTrue(score.notes.any { it.contains("short daily walks") })
    }

    private fun samplePlan(): CaloriePlan = CaloriePlan(
        bmi = 24.0,
        bmr = 1700,
        maintenanceCalories = 2400,
        targetCalories = 2000,
        proteinGrams = 150,
        carbsGrams = 250,
        fatGrams = 65
    )

    private fun sampleInput(
        age: Int = 28,
        activityLevel: ActivityLevel = ActivityLevel.Moderate
    ): UserProfileInput = UserProfileInput(
        age = age,
        heightCm = 175.0,
        weightKg = 78.0,
        sex = Sex.Male,
        activityLevel = activityLevel,
        goal = Goal.Maintain
    )

    private fun meal(
        name: String,
        calories: Int,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        matchedFoods: List<String>
    ): MealLogEntry = MealLogEntry(
        id = name,
        name = name,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        loggedAtLabel = "Today",
        matchedFoods = matchedFoods
    )
}
