package com.calories.zone.domain

import com.calories.zone.model.CustomFoodProfile
import kotlin.math.roundToInt

data class MealNutritionEstimate(
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val matchedFoods: List<String>
)

interface MealNutritionEstimator {
    fun estimateFromText(mealText: String, customFoods: List<CustomFoodProfile> = emptyList()): MealNutritionEstimate?
}

class RuleBasedMealNutritionEstimator : MealNutritionEstimator {

    override fun estimateFromText(mealText: String, customFoods: List<CustomFoodProfile>): MealNutritionEstimate? {
        val normalizedText = mealText.trim().lowercase()
        if (normalizedText.isBlank()) {
            return null
        }

        val allProfiles = FOOD_PROFILES + customFoods.mapNotNull { it.toFoodProfileOrNull() }

        var calories = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0
        val matched = mutableListOf<String>()

        allProfiles.forEach { profile ->
            val servings = servingsFor(normalizedText, profile)
            if (servings <= 0.0) {
                return@forEach
            }

            calories += profile.caloriesPerServing * servings
            protein += profile.proteinPerServing * servings
            carbs += profile.carbsPerServing * servings
            fat += profile.fatPerServing * servings

            val servingLabel = if (servings == 1.0) {
                profile.label
            } else {
                "${formatServings(servings)} x ${profile.label}"
            }
            matched += servingLabel
        }

        if (matched.isEmpty()) {
            return null
        }

        return MealNutritionEstimate(
            calories = calories.roundToInt().coerceAtLeast(0),
            proteinGrams = protein.roundToInt().coerceAtLeast(0),
            carbsGrams = carbs.roundToInt().coerceAtLeast(0),
            fatGrams = fat.roundToInt().coerceAtLeast(0),
            matchedFoods = matched
        )
    }

    private fun servingsFor(mealText: String, profile: FoodProfile): Double {
        val aliasRegex = Regex("""\b(?:${profile.aliasPattern})\b""", RegexOption.IGNORE_CASE)
        if (!aliasRegex.containsMatchIn(mealText)) {
            return 0.0
        }

        if (profile.gramsPerServing != null) {
            val metricRegex = Regex(
                """(?i)(\d+(?:\.\d+)?)\s*(g|gram|grams|ml|milliliter|milliliters|lb|lbs|pound|pounds)\s*(?:of\s+)?(?:${profile.aliasPattern})\b|(?:${profile.aliasPattern})\b\s*(\d+(?:\.\d+)?)\s*(g|gram|grams|ml|milliliter|milliliters|lb|lbs|pound|pounds)"""
            )
            val metricMatch = metricRegex.find(mealText)
            if (metricMatch != null) {
                val rawValue = metricMatch.groupValues.getOrNull(1).orEmpty().ifBlank {
                    metricMatch.groupValues.getOrNull(3).orEmpty()
                }
                val rawUnit = metricMatch.groupValues.getOrNull(2).orEmpty().ifBlank {
                    metricMatch.groupValues.getOrNull(4).orEmpty()
                }

                val amountValue = rawValue.toDoubleOrNull()
                val gramsValue = if (amountValue != null) {
                    toGrams(amountValue, rawUnit)
                } else {
                    null
                }

                if (gramsValue != null && gramsValue > 0) {
                    return (gramsValue / profile.gramsPerServing).coerceIn(0.25, 20.0)
                }
            }
        }

        val prefixCountRegex = Regex(
            """(?i)(\d+(?:\.\d+)?)\s*(?:x\s*)?(?:cups?|cup|slices?|slice|tbsp|tablespoons?|tsp|teaspoons?|servings?|pieces?|piece|cans?|can|bowls?|bowl)?\s*(?:of\s+)?(?:${profile.aliasPattern})\b"""
        )
        val prefixCount = prefixCountRegex.find(mealText)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        if (prefixCount != null && prefixCount > 0) {
            return prefixCount.coerceIn(0.25, 20.0)
        }

        val postfixCountRegex = Regex(
            """(?i)(?:${profile.aliasPattern})\b\s*(\d+(?:\.\d+)?)\s*(?:cups?|cup|slices?|slice|tbsp|tablespoons?|tsp|teaspoons?|servings?|pieces?|piece|cans?|can|bowls?|bowl)?"""
        )
        val postfixCount = postfixCountRegex.find(mealText)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        if (postfixCount != null && postfixCount > 0) {
            return postfixCount.coerceIn(0.25, 20.0)
        }

        return profile.defaultServings
    }

    private fun CustomFoodProfile.toFoodProfileOrNull(): FoodProfile? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || caloriesPer100g <= 0) {
            return null
        }

        val aliasPattern = customAliasPattern(trimmedName)
        if (aliasPattern.isBlank()) {
            return null
        }

        return FoodProfile(
            label = trimmedName,
            aliasPattern = aliasPattern,
            caloriesPerServing = caloriesPer100g.toDouble(),
            proteinPerServing = proteinPer100g.coerceAtLeast(0).toDouble(),
            carbsPerServing = carbsPer100g.coerceAtLeast(0).toDouble(),
            fatPerServing = fatPer100g.coerceAtLeast(0).toDouble(),
            defaultServings = 1.0,
            gramsPerServing = 100.0
        )
    }

    private fun customAliasPattern(foodName: String): String {
        return foodName
            .lowercase()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .joinToString("\\s+") { Regex.escape(it) }
    }

    private fun toGrams(value: Double, unit: String): Double? {
        return when (unit.lowercase()) {
            "g", "gram", "grams" -> value
            "ml", "milliliter", "milliliters" -> value
            "lb", "lbs", "pound", "pounds" -> value * GRAMS_PER_POUND
            else -> null
        }
    }

    private fun formatServings(servings: Double): String {
        val whole = servings.toInt().toDouble()
        return if (servings == whole) {
            servings.toInt().toString()
        } else {
            String.format("%.1f", servings)
        }
    }

    private data class FoodProfile(
        val label: String,
        val aliasPattern: String,
        val caloriesPerServing: Double,
        val proteinPerServing: Double,
        val carbsPerServing: Double,
        val fatPerServing: Double,
        val defaultServings: Double = 1.0,
        val gramsPerServing: Double? = null
    )

    private companion object {
        const val GRAMS_PER_POUND = 453.592

        val FOOD_PROFILES = listOf(
            FoodProfile("egg", "eggs?", 78.0, 6.0, 1.0, 5.0, gramsPerServing = 50.0),
            FoodProfile("chicken breast", "chicken(?:\\s+breast)?", 165.0, 31.0, 0.0, 4.0, gramsPerServing = 100.0),
            FoodProfile("rice", "(?:white\\s+|brown\\s+)?rice", 206.0, 4.0, 45.0, 0.0, gramsPerServing = 158.0),
            FoodProfile("bread", "bread|toast", 80.0, 3.0, 15.0, 1.0, gramsPerServing = 30.0),
            FoodProfile("oats", "oats?|oatmeal", 150.0, 5.0, 27.0, 3.0, gramsPerServing = 40.0),
            FoodProfile("banana", "bananas?", 105.0, 1.0, 27.0, 0.0, gramsPerServing = 118.0),
            FoodProfile("apple", "apples?", 95.0, 0.0, 25.0, 0.0, gramsPerServing = 182.0),
            FoodProfile("black beans", "black\\s+beans?", 227.0, 15.0, 41.0, 1.0, gramsPerServing = 172.0),
            FoodProfile("milk", "milk", 122.0, 8.0, 12.0, 5.0, gramsPerServing = 244.0),
            FoodProfile("yogurt", "yogurt|yoghurt", 150.0, 13.0, 17.0, 4.0, gramsPerServing = 170.0),
            FoodProfile("salmon", "salmon", 208.0, 22.0, 0.0, 13.0, gramsPerServing = 100.0),
            FoodProfile("beef", "beef|steak", 250.0, 26.0, 0.0, 15.0, gramsPerServing = 100.0),
            FoodProfile("potato", "potatoes?|potato", 161.0, 4.0, 37.0, 0.0, gramsPerServing = 173.0),
            FoodProfile("tuna", "tuna", 132.0, 29.0, 0.0, 1.0, gramsPerServing = 100.0),
            FoodProfile("peanut butter", "peanut\\s+butter", 94.0, 4.0, 3.0, 8.0, gramsPerServing = 16.0),
            FoodProfile("olive oil", "olive\\s+oil", 119.0, 0.0, 0.0, 14.0, gramsPerServing = 15.0),
            FoodProfile("cheese", "cheese", 113.0, 7.0, 1.0, 9.0, gramsPerServing = 28.0)
        )
    }
}