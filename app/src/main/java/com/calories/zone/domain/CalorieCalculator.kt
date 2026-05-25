package com.calories.zone.domain

import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.Goal
import com.calories.zone.model.Sex
import com.calories.zone.model.UserProfileInput
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

class CalorieCalculator {
    fun createPlan(input: UserProfileInput): CaloriePlan {
        val bmrValue = when (input.sex) {
            Sex.Male -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age + 5
            Sex.Female -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age - 161
        }

        val maintenanceCalories = (bmrValue * input.activityLevel.multiplier).roundToInt()
        val targetCalories = (maintenanceCalories * input.goal.calorieMultiplier).roundToInt()
        val proteinGrams = (input.weightKg * input.goal.proteinMultiplier).roundToInt()

        val minimumFatGrams = input.weightKg * 0.8
        val recommendedFatGrams = targetCalories * 0.25 / 9.0
        val fatGrams = max(minimumFatGrams, recommendedFatGrams).roundToInt()

        val remainingCalories = (targetCalories - proteinGrams * 4 - fatGrams * 9).coerceAtLeast(0)
        val carbsGrams = (remainingCalories / 4.0).roundToInt()

        return CaloriePlan(
            bmi = input.weightKg / (input.heightCm / 100.0).pow(2),
            bmr = bmrValue.roundToInt(),
            maintenanceCalories = maintenanceCalories,
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams
        )
    }
}
