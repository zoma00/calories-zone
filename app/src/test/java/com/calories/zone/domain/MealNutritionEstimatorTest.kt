package com.calories.zone.domain

import com.calories.zone.model.CustomFoodProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MealNutritionEstimatorTest {

    private val estimator = RuleBasedMealNutritionEstimator()

    @Test
    fun estimateFromText_calculatesKnownFoods() {
        val result = estimator.estimateFromText("2 eggs and toast")

        assertNotNull(result)
        assertEquals(236, result?.calories)
        assertEquals(15, result?.proteinGrams)
        assertEquals(17, result?.carbsGrams)
        assertEquals(11, result?.fatGrams)
    }

    @Test
    fun estimateFromText_supportsGramBasedProteinFood() {
        val result = estimator.estimateFromText("150g chicken breast with rice")

        assertNotNull(result)
        assertEquals(454, result?.calories)
        assertEquals(51, result?.proteinGrams)
        assertEquals(45, result?.carbsGrams)
        assertEquals(6, result?.fatGrams)
    }

    @Test
    fun estimateFromText_supportsMilliliters() {
        val result = estimator.estimateFromText("300 ml milk")

        assertNotNull(result)
        assertEquals(150, result?.calories)
        assertEquals(10, result?.proteinGrams)
        assertEquals(15, result?.carbsGrams)
        assertEquals(6, result?.fatGrams)
    }

    @Test
    fun estimateFromText_supportsPounds() {
        val result = estimator.estimateFromText("1 lb chicken breast")

        assertNotNull(result)
        assertEquals(748, result?.calories)
        assertEquals(141, result?.proteinGrams)
        assertEquals(0, result?.carbsGrams)
        assertEquals(18, result?.fatGrams)
    }

    @Test
    fun estimateFromText_supportsBlackBeans() {
        val result = estimator.estimateFromText("172 g black beans")

        assertNotNull(result)
        assertEquals(227, result?.calories)
        assertEquals(15, result?.proteinGrams)
        assertEquals(41, result?.carbsGrams)
        assertEquals(1, result?.fatGrams)
    }

    @Test
    fun estimateFromText_returnsNullForUnknownMeals() {
        val result = estimator.estimateFromText("festival platter deluxe")

        assertNull(result)
    }

    @Test
    fun estimateFromText_usesCustomFoods() {
        val customFoods = listOf(
            CustomFoodProfile(
                id = "1",
                name = "my shawarma",
                caloriesPer100g = 220,
                proteinPer100g = 14,
                carbsPer100g = 18,
                fatPer100g = 10
            )
        )

        val result = estimator.estimateFromText("200 g my shawarma", customFoods)

        assertNotNull(result)
        assertEquals(440, result?.calories)
        assertEquals(28, result?.proteinGrams)
        assertEquals(36, result?.carbsGrams)
        assertEquals(20, result?.fatGrams)
    }
}