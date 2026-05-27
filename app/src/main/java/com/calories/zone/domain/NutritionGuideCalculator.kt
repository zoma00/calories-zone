package com.calories.zone.domain

import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.MealLogEntry
import com.calories.zone.model.MealTotals
import com.calories.zone.model.NutritionGuideScore
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.UserProfileInput
import kotlin.math.abs
import kotlin.math.roundToInt

interface NutritionGuideCalculator {
    fun calculate(input: UserProfileInput?, plan: CaloriePlan?, meals: List<MealLogEntry>): NutritionGuideScore
}

class RuleBasedNutritionGuideCalculator : NutritionGuideCalculator {
    override fun calculate(input: UserProfileInput?, plan: CaloriePlan?, meals: List<MealLogEntry>): NutritionGuideScore {
        val ageActivityScore = calculateAgeActivityScore(input)
        if (meals.isEmpty()) {
            return NutritionGuideScore(
                ageActivityScore = ageActivityScore,
                notes = listOf(
                    "Log meals to calculate your beyond-calories guide score.",
                    ageActivityNote(input)
                )
            )
        }

        val totals = meals.toTotals()
        val signals = meals.flatMap { meal ->
            meal.matchedFoods.ifEmpty { listOf(meal.name) }
        }.map { it.lowercase() }

        val macroScore = calculateMacroScore(plan, totals)
        val satietyScore = calculateSatietyScore(totals, signals)
        val slowCarbScore = calculateSlowCarbScore(totals, signals)
        val nutrientDensityScore = calculateNutrientDensityScore(signals)
        val processingScore = calculateProcessingScore(signals)
        val overallScore = listOf(
            macroScore,
            satietyScore,
            slowCarbScore,
            nutrientDensityScore,
            processingScore,
            ageActivityScore
        ).average().roundToInt().coerceIn(0, 100)

        return NutritionGuideScore(
            overallScore = overallScore,
            macroScore = macroScore,
            satietyScore = satietyScore,
            slowCarbScore = slowCarbScore,
            nutrientDensityScore = nutrientDensityScore,
            processingScore = processingScore,
            ageActivityScore = ageActivityScore,
            notes = buildNotes(
                input = input,
                plan = plan,
                totals = totals,
                macroScore = macroScore,
                satietyScore = satietyScore,
                slowCarbScore = slowCarbScore,
                nutrientDensityScore = nutrientDensityScore,
                processingScore = processingScore
            )
        )
    }

    private fun List<MealLogEntry>.toTotals(): MealTotals = fold(MealTotals()) { totals, meal ->
        MealTotals(
            calories = totals.calories + meal.calories,
            proteinGrams = totals.proteinGrams + meal.proteinGrams,
            carbsGrams = totals.carbsGrams + meal.carbsGrams,
            fatGrams = totals.fatGrams + meal.fatGrams
        )
    }

    private fun calculateMacroScore(plan: CaloriePlan?, totals: MealTotals): Int {
        if (plan == null || plan.targetCalories <= 0 || totals.calories <= 0) {
            return 0
        }

        val calorieProgress = (totals.calories.toDouble() / plan.targetCalories).coerceIn(0.25, 1.0)
        val proteinScore = scoreAgainstTarget(totals.proteinGrams.toDouble(), plan.proteinGrams * calorieProgress)
        val carbsScore = scoreAgainstTarget(totals.carbsGrams.toDouble(), plan.carbsGrams * calorieProgress)
        val fatScore = scoreAgainstTarget(totals.fatGrams.toDouble(), plan.fatGrams * calorieProgress)

        return listOf(proteinScore, carbsScore, fatScore).average().roundToInt().coerceIn(0, 100)
    }

    private fun calculateSatietyScore(totals: MealTotals, signals: List<String>): Int {
        if (totals.calories <= 0) {
            return 0
        }

        val proteinDensity = totals.proteinGrams * 4.0 / totals.calories
        val proteinScore = ((proteinDensity / 0.24) * 100).roundToInt().coerceIn(0, 100)
        val satietyFoodScore = if (signals.anyMatches(SATIETY_FOOD_TERMS)) 90 else 45

        return listOf(proteinScore, satietyFoodScore).average().roundToInt().coerceIn(0, 100)
    }

    private fun calculateSlowCarbScore(totals: MealTotals, signals: List<String>): Int {
        if (totals.carbsGrams <= 10) {
            return 80
        }

        return when {
            signals.anyMatches(SLOW_CARB_TERMS) -> 92
            signals.anyMatches(FAST_CARB_TERMS) -> 45
            else -> 65
        }
    }

    private fun calculateNutrientDensityScore(signals: List<String>): Int {
        if (signals.isEmpty()) {
            return 0
        }

        val rawScore = signals.fold(0) { total, signal ->
            total + when {
                signal.matches(PROCESSED_FOOD_TERMS) -> 25
                signal.matches(WHOLE_FOOD_TERMS) -> 100
                else -> 65
            }
        }

        return (rawScore / signals.size).coerceIn(0, 100)
    }

    private fun calculateProcessingScore(signals: List<String>): Int {
        if (signals.isEmpty()) {
            return 0
        }

        val processedCount = signals.countMatches(PROCESSED_FOOD_TERMS)
        return (100 - processedCount * 25).coerceIn(0, 100)
    }

    private fun calculateAgeActivityScore(input: UserProfileInput?): Int {
        if (input == null) {
            return 0
        }

        val ageScore = when (input.age) {
            in 15..17 -> 80
            in 18..39 -> 90
            in 40..59 -> 86
            else -> 82
        }
        val activityScore = when (input.activityLevel) {
            ActivityLevel.Sedentary -> 55
            ActivityLevel.Light -> 70
            ActivityLevel.Moderate -> 88
            ActivityLevel.VeryActive -> 92
            ActivityLevel.Athlete -> 90
        }

        return listOf(ageScore, activityScore).average().roundToInt().coerceIn(0, 100)
    }

    private fun scoreAgainstTarget(actual: Double, target: Double): Int {
        if (target <= 0.0) {
            return 0
        }

        val differenceRatio = abs(actual - target) / target
        return (100 - differenceRatio * 100).roundToInt().coerceIn(0, 100)
    }

    private fun buildNotes(
        input: UserProfileInput?,
        plan: CaloriePlan?,
        totals: MealTotals,
        macroScore: Int,
        satietyScore: Int,
        slowCarbScore: Int,
        nutrientDensityScore: Int,
        processingScore: Int
    ): List<String> = buildList {
        add(portionNote(plan, totals.calories))
        add(ageActivityNote(input))

        if (macroScore < LOW_SCORE_THRESHOLD) {
            add("Macro guide: make the next meal balance the weakest macro instead of only chasing calories.")
        }

        if (satietyScore < LOW_SCORE_THRESHOLD) {
            add("Satiety guide: add a clear protein source and a high-volume food such as fruit, beans, potato, oats, or yogurt.")
        }

        if (slowCarbScore < LOW_SCORE_THRESHOLD) {
            add("Glycemic-load guide: swap some fast carbs for oats, beans, potato, fruit, or another slower-digesting carb.")
        }

        if (nutrientDensityScore < LOW_SCORE_THRESHOLD) {
            add("Nutrient-density guide: bias remaining meals toward simple whole foods instead of low-nutrient calories.")
        }

        if (processingScore < LOW_SCORE_THRESHOLD) {
            add("Processing guide: keep heavily processed foods smaller and anchor the day with more minimally processed foods.")
        }

        if (size == 1) {
            add("Guide calculation: your logged meals currently match the beyond-calories rules well.")
        }
    }

    private fun ageActivityNote(input: UserProfileInput?): String {
        if (input == null) {
            return "Age/activity guide: update your profile age and activity level so the guide can fit your routine."
        }

        val ageLine = when (input.age) {
            in 15..17 -> "keep nutrition changes conservative while your body is still developing"
            in 18..39 -> "use your activity level to decide where carbs and meal timing matter most"
            in 40..59 -> "prioritize protein distribution, strength-friendly meals, and steady daily movement"
            else -> "prioritize protein, hydration, strength-friendly meals, and consistent movement"
        }

        val activityLine = when (input.activityLevel) {
            ActivityLevel.Sedentary -> "start with short daily walks before cutting calories harder"
            ActivityLevel.Light -> "use simple meals and regular walking to improve consistency"
            ActivityLevel.Moderate -> "place more carbs around training and keep protein steady"
            ActivityLevel.VeryActive -> "support training with enough carbs, fluids, and recovery meals"
            ActivityLevel.Athlete -> "spread calories across multiple meals to protect performance and recovery"
        }

        return "Age/activity guide: $ageLine; $activityLine."
    }

    private fun portionNote(plan: CaloriePlan?, loggedCalories: Int): String {
        if (plan == null) {
            return "Portion guide: generate a calorie plan so portions can be judged against your target."
        }

        val delta = plan.targetCalories - loggedCalories
        return when {
            delta < 0 -> "Portion guide: you are ${-delta} kcal over target, so keep later portions lighter and protein-focused."
            delta < plan.targetCalories * 0.15 -> "Portion guide: you are close to target, so keep remaining portions small and simple."
            else -> "Portion guide: use remaining calories for a palm of protein, a fist of plants, and measured carbs or fats."
        }
    }

    private fun List<String>.anyMatches(terms: Set<String>): Boolean = any { value ->
        terms.any { term -> value.contains(term) }
    }

    private fun List<String>.countMatches(terms: Set<String>): Int = count { value ->
        value.matches(terms)
    }

    private fun String.matches(terms: Set<String>): Boolean = terms.any { term -> contains(term) }

    private companion object {
        const val LOW_SCORE_THRESHOLD = 70

        val SATIETY_FOOD_TERMS = setOf(
            "egg",
            "chicken",
            "salmon",
            "beef",
            "steak",
            "tuna",
            "beans",
            "oats",
            "oatmeal",
            "potato",
            "apple",
            "banana",
            "yogurt",
            "yoghurt",
            "milk"
        )

        val SLOW_CARB_TERMS = setOf(
            "oats",
            "oatmeal",
            "beans",
            "potato",
            "apple",
            "banana",
            "brown rice"
        )

        val FAST_CARB_TERMS = setOf(
            "bread",
            "toast",
            "white rice",
            "soda",
            "juice",
            "candy",
            "cake",
            "cookie",
            "donut"
        )

        val WHOLE_FOOD_TERMS = setOf(
            "egg",
            "chicken",
            "rice",
            "oats",
            "oatmeal",
            "banana",
            "apple",
            "beans",
            "milk",
            "yogurt",
            "yoghurt",
            "salmon",
            "beef",
            "steak",
            "potato",
            "tuna",
            "olive oil"
        )

        val PROCESSED_FOOD_TERMS = setOf(
            "soda",
            "chips",
            "candy",
            "cookie",
            "cake",
            "donut",
            "ice cream",
            "pizza",
            "burger",
            "fries",
            "fast food",
            "packaged",
            "snack",
            "sausage",
            "hot dog",
            "fried"
        )
    }
}
