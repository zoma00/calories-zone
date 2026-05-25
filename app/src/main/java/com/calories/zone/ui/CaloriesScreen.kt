package com.calories.zone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calories.zone.data.LocalAppStorage
import com.calories.zone.model.ActivityLevel
import com.calories.zone.model.CaloriePlan
import com.calories.zone.model.CustomFoodProfile
import com.calories.zone.model.Goal
import com.calories.zone.model.MealEntryUnit
import com.calories.zone.model.MealLogEntry
import com.calories.zone.model.MealTotals
import com.calories.zone.model.Sex

@Composable
fun CaloriesRoute(
    storage: LocalAppStorage,
    viewModel: CaloriesViewModel = viewModel(factory = CaloriesViewModel.factory(storage))
) {
    CaloriesScreen(
        state = viewModel.uiState,
        onProfileNameChanged = viewModel::updateProfileName,
        onAgeChanged = viewModel::updateAge,
        onHeightChanged = viewModel::updateHeight,
        onWeightChanged = viewModel::updateWeight,
        onSexChanged = viewModel::updateSex,
        onActivityChanged = viewModel::updateActivityLevel,
        onGoalChanged = viewModel::updateGoal,
        onGenerateClick = viewModel::generatePlan,
        onSaveProfileClick = viewModel::saveProfile,
        onMealNameChanged = viewModel::updateMealName,
        onMealEntryAmountChanged = viewModel::updateMealEntryAmount,
        onMealEntryUnitChanged = viewModel::updateMealEntryUnit,
        onDeleteMealClick = viewModel::deleteMeal,
        customFoods = viewModel.uiState.customFoods,
        customFoodName = viewModel.uiState.customFoodName,
        customFoodCalories = viewModel.uiState.customFoodCalories,
        customFoodProtein = viewModel.uiState.customFoodProtein,
        customFoodCarbs = viewModel.uiState.customFoodCarbs,
        customFoodFat = viewModel.uiState.customFoodFat,
        onCustomFoodNameChanged = viewModel::updateCustomFoodName,
        onCustomFoodCaloriesChanged = viewModel::updateCustomFoodCalories,
        onCustomFoodProteinChanged = viewModel::updateCustomFoodProtein,
        onCustomFoodCarbsChanged = viewModel::updateCustomFoodCarbs,
        onCustomFoodFatChanged = viewModel::updateCustomFoodFat,
        onAddCustomFoodClick = viewModel::addCustomFood,
        onDeleteCustomFoodClick = viewModel::deleteCustomFood,
        onAddMealClick = viewModel::addMeal
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaloriesScreen(
    state: CaloriesUiState,
    onProfileNameChanged: (String) -> Unit,
    onAgeChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onSexChanged: (Sex) -> Unit,
    onActivityChanged: (ActivityLevel) -> Unit,
    onGoalChanged: (Goal) -> Unit,
    onGenerateClick: () -> Unit,
    onSaveProfileClick: () -> Unit,
    onMealNameChanged: (String) -> Unit,
    onMealEntryAmountChanged: (String) -> Unit,
    onMealEntryUnitChanged: (MealEntryUnit) -> Unit,
    onDeleteMealClick: (String) -> Unit,
    customFoods: List<CustomFoodProfile>,
    customFoodName: String,
    customFoodCalories: String,
    customFoodProtein: String,
    customFoodCarbs: String,
    customFoodFat: String,
    onCustomFoodNameChanged: (String) -> Unit,
    onCustomFoodCaloriesChanged: (String) -> Unit,
    onCustomFoodProteinChanged: (String) -> Unit,
    onCustomFoodCarbsChanged: (String) -> Unit,
    onCustomFoodFatChanged: (String) -> Unit,
    onAddCustomFoodClick: () -> Unit,
    onDeleteCustomFoodClick: (String) -> Unit,
    onAddMealClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calories Zone") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Calories calculator with local logging",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep your profile and meals on-device, estimate calorie targets, and use local guidance without sending data off-device.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    MetricField(
                        label = "Profile name",
                        value = state.profileName,
                        onValueChanged = onProfileNameChanged,
                        keyboardType = KeyboardType.Text
                    )

                    MetricField(
                        label = "Age",
                        value = state.age,
                        onValueChanged = onAgeChanged,
                        suffix = "years",
                        keyboardType = KeyboardType.Number
                    )

                    MetricField(
                        label = "Height",
                        value = state.heightCm,
                        onValueChanged = onHeightChanged,
                        suffix = "cm"
                    )

                    MetricField(
                        label = "Weight",
                        value = state.weightKg,
                        onValueChanged = onWeightChanged,
                        suffix = "kg"
                    )

                    SelectionRow(
                        label = "Sex",
                        options = Sex.entries,
                        selected = state.sex,
                        optionLabel = { it.label },
                        onSelected = onSexChanged
                    )

                    SelectionRow(
                        label = "Activity level",
                        options = ActivityLevel.entries,
                        selected = state.activityLevel,
                        optionLabel = { it.label },
                        onSelected = onActivityChanged
                    )

                    SelectionRow(
                        label = "Goal",
                        options = Goal.entries,
                        selected = state.goal,
                        optionLabel = { it.label },
                        onSelected = onGoalChanged
                    )

                    Button(
                        onClick = onGenerateClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate plan")
                    }

                    OutlinedButton(
                        onClick = onSaveProfileClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save profile")
                    }

                    if (state.validationMessage != null) {
                        Text(
                            text = state.validationMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (state.statusMessage != null) {
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            state.result?.let { result ->
                ResultCard(result = result)

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Local guidance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "This app still works fully offline. If you later add a small on-device helper, keep it focused on meal parsing and chat instead of calorie math.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        state.guidanceNotes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            MealLogCard(
                mealTotals = state.mealTotals,
                targetPlan = state.result,
                mealName = state.mealName,
                mealEntryAmount = state.mealEntryAmount,
                mealEntryUnit = state.mealEntryUnit,
                mealCalories = state.mealCalories,
                mealProtein = state.mealProtein,
                mealCarbs = state.mealCarbs,
                mealFat = state.mealFat,
                meals = state.meals,
                onMealNameChanged = onMealNameChanged,
                onMealEntryAmountChanged = onMealEntryAmountChanged,
                onMealEntryUnitChanged = onMealEntryUnitChanged,
                onDeleteMealClick = onDeleteMealClick,
                onAddMealClick = onAddMealClick
            )

            CustomFoodsCard(
                customFoods = customFoods,
                customFoodName = customFoodName,
                customFoodCalories = customFoodCalories,
                customFoodProtein = customFoodProtein,
                customFoodCarbs = customFoodCarbs,
                customFoodFat = customFoodFat,
                onCustomFoodNameChanged = onCustomFoodNameChanged,
                onCustomFoodCaloriesChanged = onCustomFoodCaloriesChanged,
                onCustomFoodProteinChanged = onCustomFoodProteinChanged,
                onCustomFoodCarbsChanged = onCustomFoodCarbsChanged,
                onCustomFoodFatChanged = onCustomFoodFatChanged,
                onAddCustomFoodClick = onAddCustomFoodClick,
                onDeleteCustomFoodClick = onDeleteCustomFoodClick
            )
        }
    }
}

@Composable
private fun MetricField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = suffix?.let { { Text(it) } }
    )
}

@Composable
private fun <T> SelectionRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(optionLabel(option)) }
                )
            }
        }
    }
}

@Composable
private fun ResultCard(result: CaloriePlan) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Your numbers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            ResultRow(label = "BMR", value = "${result.bmr} kcal")
            ResultRow(label = "Maintenance", value = "${result.maintenanceCalories} kcal")
            ResultRow(label = "Target", value = "${result.targetCalories} kcal")
            ResultRow(label = "BMI", value = String.format("%.1f", result.bmi))

            HorizontalDivider()

            Text(
                text = "Macro split",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroBadge(label = "Protein", value = "${result.proteinGrams}g")
                MacroBadge(label = "Carbs", value = "${result.carbsGrams}g")
                MacroBadge(label = "Fat", value = "${result.fatGrams}g")
            }
        }
    }
}

@Composable
private fun MealLogCard(
    mealTotals: MealTotals,
    targetPlan: CaloriePlan?,
    mealName: String,
    mealEntryAmount: String,
    mealEntryUnit: MealEntryUnit,
    mealCalories: String,
    mealProtein: String,
    mealCarbs: String,
    mealFat: String,
    meals: List<MealLogEntry>,
    onMealNameChanged: (String) -> Unit,
    onMealEntryAmountChanged: (String) -> Unit,
    onMealEntryUnitChanged: (MealEntryUnit) -> Unit,
    onDeleteMealClick: (String) -> Unit,
    onAddMealClick: () -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Meal log",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Enter meal name and amount in grams, ml, or pounds. Nutrition is auto-calculated as you type.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MetricField(
                label = "Meal entry name",
                value = mealName,
                onValueChanged = onMealNameChanged,
                keyboardType = KeyboardType.Text
            )

            MetricField(
                label = "Meal entry amount",
                value = mealEntryAmount,
                onValueChanged = onMealEntryAmountChanged,
                keyboardType = KeyboardType.Decimal
            )

            SelectionRow(
                label = "Entry unit",
                options = MealEntryUnit.entries,
                selected = mealEntryUnit,
                optionLabel = { it.label },
                onSelected = onMealEntryUnitChanged
            )

            Text(
                text = "Auto calculated",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            ResultRow(label = "Calories", value = if (mealCalories.isBlank()) "-" else "$mealCalories kcal")
            ResultRow(label = "Protein", value = if (mealProtein.isBlank()) "-" else "${mealProtein}g")
            ResultRow(label = "Carbs", value = if (mealCarbs.isBlank()) "-" else "${mealCarbs}g")
            ResultRow(label = "Fat", value = if (mealFat.isBlank()) "-" else "${mealFat}g")

            Button(
                onClick = onAddMealClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log meal")
            }

            HorizontalDivider()

            Text(
                text = "Today so far",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            ResultRow(label = "Logged calories", value = "${mealTotals.calories} kcal")
            ResultRow(label = "Protein", value = "${mealTotals.proteinGrams}g")
            ResultRow(label = "Carbs", value = "${mealTotals.carbsGrams}g")
            ResultRow(label = "Fat", value = "${mealTotals.fatGrams}g")

            if (targetPlan != null) {
                val calorieDelta = targetPlan.targetCalories - mealTotals.calories
                ResultRow(
                    label = "Target status",
                    value = if (calorieDelta >= 0) {
                        "$calorieDelta kcal remaining"
                    } else {
                        "${-calorieDelta} kcal over"
                    }
                )
            }

            HorizontalDivider()

            Text(
                text = "Recent meals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (meals.isEmpty()) {
                Text(
                    text = "No meals logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                meals.take(8).forEach { meal ->
                    MealLogRow(
                        meal = meal,
                        onDeleteMealClick = { onDeleteMealClick(meal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFoodsCard(
    customFoods: List<CustomFoodProfile>,
    customFoodName: String,
    customFoodCalories: String,
    customFoodProtein: String,
    customFoodCarbs: String,
    customFoodFat: String,
    onCustomFoodNameChanged: (String) -> Unit,
    onCustomFoodCaloriesChanged: (String) -> Unit,
    onCustomFoodProteinChanged: (String) -> Unit,
    onCustomFoodCarbsChanged: (String) -> Unit,
    onCustomFoodFatChanged: (String) -> Unit,
    onAddCustomFoodClick: () -> Unit,
    onDeleteCustomFoodClick: (String) -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Custom foods",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Add custom foods with calories and macros per 100g so meal estimation can use your own nutrition data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MetricField(
                label = "Custom food name",
                value = customFoodName,
                onValueChanged = onCustomFoodNameChanged,
                keyboardType = KeyboardType.Text
            )

            MetricField(
                label = "Calories per 100g",
                value = customFoodCalories,
                onValueChanged = onCustomFoodCaloriesChanged,
                suffix = "kcal",
                keyboardType = KeyboardType.Number
            )

            MetricField(
                label = "Protein per 100g",
                value = customFoodProtein,
                onValueChanged = onCustomFoodProteinChanged,
                suffix = "g",
                keyboardType = KeyboardType.Number
            )

            MetricField(
                label = "Carbs per 100g",
                value = customFoodCarbs,
                onValueChanged = onCustomFoodCarbsChanged,
                suffix = "g",
                keyboardType = KeyboardType.Number
            )

            MetricField(
                label = "Fat per 100g",
                value = customFoodFat,
                onValueChanged = onCustomFoodFatChanged,
                suffix = "g",
                keyboardType = KeyboardType.Number
            )

            Button(
                onClick = onAddCustomFoodClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save custom food")
            }

            HorizontalDivider()

            Text(
                text = "Saved custom foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (customFoods.isEmpty()) {
                Text(
                    text = "No custom foods saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                customFoods.take(8).forEach { customFood ->
                    CustomFoodRow(
                        customFood = customFood,
                        onDeleteCustomFoodClick = { onDeleteCustomFoodClick(customFood.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFoodRow(
    customFood: CustomFoodProfile,
    onDeleteCustomFoodClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = customFood.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${customFood.caloriesPer100g} kcal • P ${customFood.proteinPer100g}g • C ${customFood.carbsPer100g}g • F ${customFood.fatPer100g}g (per 100g)",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = onDeleteCustomFoodClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete custom food")
            }
        }
    }
}

@Composable
private fun MealLogRow(
    meal: MealLogEntry,
    onDeleteMealClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = meal.loggedAtLabel, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${meal.calories} kcal • P ${meal.proteinGrams}g • C ${meal.carbsGrams}g • F ${meal.fatGrams}g",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(
                onClick = onDeleteMealClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete meal")
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MacroBadge(label: String, value: String) {
    ElevatedCard(
        modifier = Modifier.widthIn(min = 96.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
