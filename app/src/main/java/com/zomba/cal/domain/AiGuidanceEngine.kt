package com.zomba.cal.domain

import com.zomba.cal.model.ActivityLevel
import com.zomba.cal.model.CaloriePlan
import com.zomba.cal.model.Goal
import com.zomba.cal.model.UserProfileInput

interface AiGuidanceEngine {
    fun buildInsights(input: UserProfileInput, plan: CaloriePlan): List<String>
}

class RuleBasedAiGuidanceEngine : AiGuidanceEngine {
    override fun buildInsights(input: UserProfileInput, plan: CaloriePlan): List<String> = buildList {
        add(
            when (input.goal) {
                Goal.LoseFat -> "Start with this 15% deficit for two weeks and adjust only if your weekly average weight is flat."
                Goal.Maintain -> "Use this intake as a baseline and keep your bodyweight within a narrow weekly range."
                Goal.BuildMuscle -> "This small surplus is enough for muscle gain without turning the bulk into unnecessary fat gain."
            }
        )

        add(
            when {
                plan.bmi < 18.5 -> "Your BMI is low, so avoid aggressive dieting and bias toward strength training plus a mild calorie surplus."
                plan.bmi < 25 -> "Your BMI is in a healthy range, so make small calorie changes and judge progress by weekly trends."
                plan.bmi < 30 -> "A moderate deficit and consistent protein intake should work better than an aggressive cut."
                else -> "Keep the calorie deficit moderate, prioritize steps and sleep, and avoid trying to lose weight too quickly."
            }
        )

        add(
            when (input.activityLevel) {
                ActivityLevel.Sedentary -> "Increase daily movement first; adding 7k to 8k steps can improve results before cutting calories harder."
                ActivityLevel.Light -> "A short walk after meals will improve appetite control and makes this target easier to follow."
                ActivityLevel.Moderate -> "This activity level pairs well with three to five resistance sessions per week."
                ActivityLevel.VeryActive -> "Keep most of your carbs around training so performance does not fall while you follow the plan."
                ActivityLevel.Athlete -> "For heavy training blocks, split calories across four or five meals and anchor recovery around protein timing."
            }
        )

        add(
            "Macro anchor: about ${plan.proteinGrams}g protein, ${plan.carbsGrams}g carbs, and ${plan.fatGrams}g fat per day."
        )
    }
}