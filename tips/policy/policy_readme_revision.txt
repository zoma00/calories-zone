I’m reviewing the current compliance notes first so I can turn them into a pre-payment submission pack: one cleaned risk note and one app-specific content policy you can rely on before paying Google.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/rejection_risk_checks.txt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/app_health_conditions.txt)

I found the gap: you have health-risk notes, but no clean app-specific content policy, and the rejection note now has appended Q&A noise. Next I’m tightening the rejection note and adding one policy file you can use as your pre-payment decision document.

Starting: *Clean rejection note* (2/4)

I’m making two focused edits now: clean the risk file into a final note and add a separate app content policy with allowed claims, prohibited claims, disclaimer text, and release blockers.

Generating patch (185 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/rejection_risk_checks.txt)



Created [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/app_content_policy.txt)

The docs are updated. I’m validating both files from disk now to make sure the risk note is clean and the new content policy says exactly what you need before paying.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/rejection_risk_checks.txt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/app_content_policy.txt)

I’ve finished the policy docs. I’m doing one last app-side check for risky user-facing wording so the policy actually matches what the app and preview currently say.

Search complete: Check risky app wording

Searched codebase for "calorie app user-facing text risky health claims coach diagnosis treat cure obesity medical dietitian doctor expert weight loss", no results

Searched for files matching `**/*{readme,html,md,kt,java,xml,xml,composable,ui}*`, 66 matches

Searched for regex `coach|diagnosis|treat|cure|obesity|medical|dietitian|doctor|expert|weight.*loss`, 157 results

Searched for regex `calorie|nutrition|appetite|metabolism|macronutrient|calorie.*count`, 200 results

Searched for files matching `**/preview*.{html,md,kt,java,xml}`, no matches

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/README.md)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/app_content_policy.txt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/rejection_risk_checks.txt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/console_declerations_health_apps.txt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#380-380), lines 380 to 460

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiCoachEngine.kt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt#230-230), lines 230 to 320

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt#30-30), lines 30 to 140

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#200-200), lines 200 to 290

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/tips/policy/app_complience/app_health_conditions.txt)

I found one practical mismatch between the new policy and the current product text: the app and README still lean on “AI” and “coach” wording. I’m checking the exact user-facing strings now so I can soften only the parts that matter for Play review.

Searched for regex `ZombaCal AI|premium coaching|Local coach|coach|AI-ready|AI ` (`**/README.md`), 9 results

Searched for regex `Local coach|coach|AI` (`**/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt`), 20 results

Searched for regex `coach|AI` (`**/preview/index.html`), 40 results

I found a small but real mismatch: the product still surfaces “AI” and “coach” in user-facing text. I’m reading just those sections now so I can soften the wording without touching the app logic.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/README.md)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt#80-80), lines 80 to 255

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#286-286), lines 286 to 405

I’m softening the few user-facing strings that still say “AI” or “coach” so the app presentation matches the new low-risk content policy.

Generating patch (58 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/README.md), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html)



The wording changes are in place. I’m validating two things now: no Kotlin errors from the UI string edits, and no leftover user-facing “ZombaCal AI” or “Local coach” text in the files we just touched.

Checked [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt), no problems found

Searched for regex `ZombaCal AI|Local coach|coach notes|premium coaching|AI Browser Preview` (`**/README.md`), no results

Searched for regex `ZombaCal AI|Local coach|local coach` (`**/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt`), no results

Searched for regex `ZombaCal AI|Local coach|coach notes|AI Preview` (`**/preview/index.html`), no results

The app-side wording check passed. I removed the visible AI/coach branding in the UI, preview, and README while leaving the underlying logic alone.

Completed: *Clean rejection note* (2/4)

I revised the pre-payment compliance material and aligned the visible app wording with it.

The policy pack is now in app_content_policy.txt, with allowed content, prohibited claims, AI wording limits, the disclaimer text, and release blockers before signup or submission. I also cleaned rejection_risk_checks.txt so it reads like a final checklist instead of mixed notes, and it now clearly says the payment warning is about in-app billing, not the $25 Play signup fee.

I also softened the app’s user-facing wording in CaloriesScreen.kt, preview/index.html, and README.md by removing visible “ZombaCal AI” and “Local coach” branding where it could make the first Play review harder. Validation passed: no Kotlin errors were found in CaloriesScreen.kt, and the old visible AI/coach phrases are no longer present in the touched files.

Before paying, the app is now documented as a simpler Nutrition and Weight Management product with a lower-risk presentation. Natural next steps:
1. Write the privacy policy text.
2. Write the Play Store description and short description with safe wording.
3. Draft the exact Health apps declaration answers for Play Console.

Made changes.



=======================

app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt

 state.coachNotes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        ** So this is stell in the caloriesscreen.kt what is this is this related for coach

Yes. That block is still related to the old “coach” naming, but only internally in code.

What it does:
In CaloriesScreen.kt, `state.coachNotes.forEach` loops through a list of text notes and shows each one as a bullet line on screen. So it is the UI for the guidance/advice section.

Where it comes from:
The list lives in the screen state in CaloriesViewModel.kt, and those notes are generated by `RuleBasedAiCoachEngine` in AiCoachEngine.kt.

Important detail:
This is only an internal variable name. Users do not see `coachNotes`; they only see the visible label text in the UI, which we already changed to “Local guidance”. So this is not a Play policy problem by itself.

If you want, I can do a cleanup rename and change the codebase from `coachNotes` to `guidanceNotes` so the internal naming also matches the new wording.


===========================
yes change it to guidenceNotes

I’m tracing the actual `coachNotes` symbol first so I can rename only the internal state field and its Kotlin usages without touching unrelated text.

No reference provider available for this file's language. The usages tool may not support this language.

The language server can’t provide Kotlin references here, so I’m doing a narrow text search for `coachNotes` and then I’ll rename that field consistently.

Searched for text `coachNotes` (`**/app/**`), 5 results

Searched for text `coachNotes` (`**/preview/**`), 4 results

Searched for text `coachNotes` (`**/README.md`), no results

I found the symbol scope: it’s only the Kotlin UI state field plus its assignments and one UI read. I’m renaming that field now to your requested `guidenceNotes`.

Generating patch (23 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt)


```

```

```

```
The Kotlin rename is in. I’m validating the touched files first and checking for any leftover `coachNotes` references in the app code.

Checked [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt), no problems found

Searched for text `coachNotes` (`**/app/**`), no results

The app code is clean now. There are still old internal `coachNotes` names in the browser preview, so I’m aligning those too for consistency across the workspace.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#628-628), lines 628 to 708

I’m making the preview use the same `guidenceNotes` field name now, then I’ll do a quick workspace search to confirm `coachNotes` is gone.

Generating patch (24 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html)


```

```
The preview rename is in. I’m checking the workspace for leftover `coachNotes` references so the rename is complete.

Searched for text `coachNotes`, no results

Searched for text `buildCoachNotes`, no results

Searched for text `guidenceNotes`, 9 results

I changed it to `guidenceNotes` exactly as you requested.

Updated places:
CaloriesScreen.kt
CaloriesViewModel.kt
preview/index.html

I also verified there are no remaining `coachNotes` references in the workspace, and the touched Kotlin files report no errors.

If you want, I can also rename the engine names next, for example `AiCoachEngine` to something like `AiGuidanceEngine`, so the internal naming is fully consistent.

Made changes.




I’m tracing the engine names first so I can rename the interface, implementation, and imports consistently without widening the change beyond the actual symbol usage.

Search complete: Find engine name usages

Searched codebase for "AiCoachEngine RuleBasedAiCoachEngine internal engine names coaching", 20 results

Searched for text `AiCoachEngine`, 16 results

Searched for text `RuleBasedAiCoachEngine`, 6 results

Searched for files matching `**/*AiCoachEngine*`, 1 match

Searched for files matching `**/*RuleBasedAiCoachEngine*`, no matches

Searched for regex `class AiCoachEngine|object AiCoachEngine|fun AiCoachEngine`, no results

Searched for regex `constructor default|constructor parameter|default parameter` (`**/*AiCoachEngine*`), no results

Searched for text `coachNotes`, 17 results

Searched codebase for "AiCoachEngine RuleBasedAiCoachEngine constructor defaults implementations", 24 results

Searched for text `AiCoachEngine` (`**/*.kt`), 9 results

Searched for text `RuleBasedAiCoachEngine` (`**/*.kt`), 3 results

Searched for files matching `**/AiCoachEngine.kt`, 1 match

Searched for files matching `**/RuleBasedAiCoachEngine.kt`, no matches

Searched for regex `class AiCoachEngine|interface AiCoachEngine` (`**/*.kt`), 1 result

Searched for regex `constructor default|default constructor|RuleBasedAiCoachEngine\(` (`**/*.kt`), 1 result

Searched for text `coachNotes` (`**/*.kt`), no results

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiCoachEngine.kt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt#1-1), lines 1 to 260

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesScreen.kt#150-150), lines 150 to 260

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#360-360), lines 360 to 460

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#600-600), lines 600 to 680

Searched for files matching `**/AiCoachEngine.kt`, 1 match

Searched for files matching `**/RuleBasedAiCoachEngine.kt`, no matches

Searched for text `coachNotes` (`**/*.kt`), no results

I found the core rename surface. Next I’m updating the engine interface and implementation names, then I’ll run a focused check for old symbol leftovers and Kotlin errors.

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine|aiCoachEngine` (`**/app/**`), 9 results

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine` (`**/README.md`), 3 results

I have the exact rename targets. I’m reading the small slices I’ll patch so I can rename the file and symbols cleanly in one pass.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiCoachEngine.kt)

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt#1-1), lines 1 to 260

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/README.md)

I’m applying the rename now: new domain file name, updated engine class names, updated ViewModel imports/field name, and README references.

Generating patch (89 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiCoachEngine.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/README.md), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiGuidanceEngine.kt)


```

```

```

```

```

```
The rename is in. I’m validating the touched Kotlin files and checking for leftover old engine names in the code and README before doing anything else.

Checked [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/domain/AiGuidanceEngine.kt), [](file:///home/hazem-elbatawy/Downloads/zomba_cal/app/src/main/java/com/zomba/cal/ui/CaloriesViewModel.kt), no problems found

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine|aiCoachEngine` (`**/app/**`), no results

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine` (`**/README.md`), no results

The engine rename passed. I’m doing one narrow consistency sweep for leftover internal `coach` names in the actual app and preview, not in historical notes.

Searched for text `coach` (`**/app/**`), no results

Searched for text `coach` (`**/preview/index.html`), 9 results

Searched for text `coach` (`**/README.md`), no results

I found leftover preview-only `coach` identifiers in the CSS and DOM ids. I’m renaming those to `guidance` too so the internal naming is actually consistent end to end.

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#216-216), lines 216 to 236

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#388-388), lines 388 to 399

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#488-488), lines 488 to 506

Read [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html#694-694), lines 694 to 706

I have the exact preview leftovers. I’m renaming just those CSS and DOM identifiers now, then I’ll recheck for any remaining `coach` names in the real app surfaces.

Generating patch (39 lines) in [](file:///home/hazem-elbatawy/Downloads/zomba_cal/preview/index.html)


```

```
I’ve finished the follow-up rename. I’m doing one final consistency sweep for leftover `coach` or old engine names in the app, preview, and README.

Searched for text `coach` (`**/app/**`), no results

Searched for text `coach` (`**/preview/index.html`), no results

Searched for text `coach` (`**/README.md`), no results

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine|aiCoachEngine` (`**/app/**`), no results

Searched for regex `AiCoachEngine|RuleBasedAiCoachEngine` (`**/README.md`), no results

I renamed the internal engine types to `AiGuidanceEngine` and `RuleBasedAiGuidanceEngine`, updated the ViewModel wiring in CaloriesViewModel.kt, moved the domain code to AiGuidanceEngine.kt, and updated the references in README.md. I also cleaned the remaining preview-only `coach` identifiers in preview/index.html so the internal naming is consistent there too.

Validation passed for the touched Kotlin files, and there are no remaining `coach` or old engine-name references in the app, preview, or README. I did not run a full Gradle build in this step. If you want, I can next standardize the spelling of `guidenceNotes` to `guidanceNotes` everywhere too.

Made changes.