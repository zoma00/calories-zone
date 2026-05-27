package com.calories.zone.domain

import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.Goal
import com.calories.zone.model.Sex
import com.calories.zone.model.UserProfileInput
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class CalorieCalculator {
    fun createPlan(input: UserProfileInput): CaloriePlan {
        val bmrValue = when (input.sex) {
            Sex.Male -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age + 5
            Sex.Female -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age - 161
        }

        val bmi = input.weightKg / (input.heightCm / 100.0).pow(2)
        val maintenanceCalories = (bmrValue * input.activityLevel.multiplier).roundToInt()
        val targetCalories = calculateTargetCalories(input.goal, maintenanceCalories, bmi)
        val proteinGrams = calculateProteinGrams(input, targetCalories)
        val fatGrams = calculateFatGrams(input, targetCalories)

        val remainingCalories = (targetCalories - proteinGrams * 4 - fatGrams * 9).coerceAtLeast(0)
        val carbsGrams = (remainingCalories / 4.0).roundToInt()

        return CaloriePlan(
            bmi = bmi,
            bmr = bmrValue.roundToInt(),
            maintenanceCalories = maintenanceCalories,
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams
        )
    }

    private fun calculateTargetCalories(goal: Goal, maintenanceCalories: Int, bmi: Double): Int {
        return if (goal == Goal.LoseFat && bmi >= OBESITY_BMI_THRESHOLD) {
            LOW_CALORIE_LOSE_FAT_TARGET
        } else {
            (maintenanceCalories * goal.calorieMultiplier).roundToInt()
        }
    }

    private fun calculateProteinGrams(input: UserProfileInput, targetCalories: Int): Int {
        val weightBasedProtein = input.weightKg * input.goal.proteinMultiplier
        val calorieCappedProtein = if (input.goal == Goal.LoseFat) {
            targetCalories * LOSE_FAT_MAX_PROTEIN_CALORIE_SHARE / 4.0
        } else {
            weightBasedProtein
        }

        return min(weightBasedProtein, calorieCappedProtein).roundToInt().coerceAtLeast(0)
    }

    private fun calculateFatGrams(input: UserProfileInput, targetCalories: Int): Int {
        val recommendedFatGrams = targetCalories * 0.25 / 9.0
        val minimumFatGrams = input.weightKg * 0.8
        val goalFatGrams = if (input.goal == Goal.LoseFat && targetCalories <= LOW_CALORIE_LOSE_FAT_TARGET) {
            recommendedFatGrams
        } else {
            max(minimumFatGrams, recommendedFatGrams)
        }

        val calorieCappedFat = if (input.goal == Goal.LoseFat) {
            targetCalories * LOSE_FAT_MAX_FAT_CALORIE_SHARE / 9.0
        } else {
            goalFatGrams
        }

        return min(goalFatGrams, calorieCappedFat).roundToInt().coerceAtLeast(0)
    }

    private companion object {
        const val OBESITY_BMI_THRESHOLD = 30.0
        const val LOW_CALORIE_LOSE_FAT_TARGET = 1200
        const val LOSE_FAT_MAX_PROTEIN_CALORIE_SHARE = 0.40
        const val LOSE_FAT_MAX_FAT_CALORIE_SHARE = 0.35
    }
}
