package com.calories.zone.domain

import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.Goal
import com.calories.zone.model.Sex
import com.calories.zone.model.UserProfileInput
import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalculatorTest {
    private val calculator = CalorieCalculator()

    @Test
    fun createPlan_returnsExpectedTargetsForModerateMaintenanceProfile() {
        val plan = calculator.createPlan(
            UserProfileInput(
                age = 28,
                heightCm = 175.0,
                weightKg = 78.0,
                sex = Sex.Male,
                activityLevel = ActivityLevel.Moderate,
                goal = Goal.Maintain
            )
        )

        assertEquals(1739, plan.bmr)
        assertEquals(2695, plan.maintenanceCalories)
        assertEquals(2695, plan.targetCalories)
        assertEquals(140, plan.proteinGrams)
        assertEquals(365, plan.carbsGrams)
        assertEquals(75, plan.fatGrams)
    }

    @Test
    fun createPlan_usesLowCalorieTargetForObeseFatLossProfile() {
        val plan = calculator.createPlan(
            UserProfileInput(
                age = 28,
                heightCm = 173.0,
                weightKg = 97.0,
                sex = Sex.Male,
                activityLevel = ActivityLevel.Light,
                goal = Goal.LoseFat
            )
        )

        assertEquals(1916, plan.bmr)
        assertEquals(2635, plan.maintenanceCalories)
        assertEquals(1200, plan.targetCalories)
        assertEquals(120, plan.proteinGrams)
        assertEquals(106, plan.carbsGrams)
        assertEquals(33, plan.fatGrams)
    }
}
