import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../core/constants/app_constants.dart';
import '../../../main.dart';
import '../../pages/settings/settings_screen.dart';
import '../../theme/app_colors.dart';
import 'buttons.dart';

class StepHeader extends StatelessWidget {
  final bool showBack;
  final bool showNew;
  final bool showThemeToggle;
  final VoidCallback? onBack;
  final VoidCallback? onNew;
  /// If non-null, shows a step number badge (e.g. "2 of 5") beside the back button.
  final int? stepNumber;
  final int? totalSteps;

  const StepHeader({
    super.key,
    this.showBack = false,
    this.showNew = false,
    this.showThemeToggle = false,
    this.onBack,
    this.onNew,
    this.stepNumber,
    this.totalSteps,
  });

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    final isDark = b == Brightness.dark;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(12, 8, 12, 0),
        child: Row(
          children: [
            if (showBack)
              GlassBtn(
                brightness: b,
                onTap: onBack ?? () {},
                child: Icon(
                  Icons.arrow_back_rounded,
                  size: 20,
                  color: AppColors.textPrimary(b),
                ),
              )
            else
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: isDark
                      ? Colors.black.withValues(alpha: 0.75)
                      : Colors.white.withValues(alpha: 0.92),
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.2),
                      blurRadius: 12,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 22,
                      height: 22,
                      decoration: BoxDecoration(
                        color: AppColors.primary,
                        borderRadius: BorderRadius.circular(5),
                      ),
                      child: const Icon(
                        Icons.route_rounded,
                        size: 13,
                        color: Colors.white,
                      ),
                    ),
                    const Gap(8),
                    Text(
                      kAppName,
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary(b),
                      ),
                    ),
                  ],
                ),
              ),

            // Step number badge
            if (stepNumber != null) ...[
              const Gap(10),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                decoration: BoxDecoration(
                  color: AppColors.primaryDim,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  totalSteps != null
                      ? 'Step $stepNumber of $totalSteps'
                      : 'Step $stepNumber',
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                    color: AppColors.primary,
                  ),
                ),
              ),
            ],

            const Spacer(),
            if (showNew)
              GlassBtn(
                brightness: b,
                onTap: onNew ?? () {},
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.add_rounded,
                      size: 16,
                      color: AppColors.textPrimary(b),
                    ),
                    const Gap(4),
                    Text(
                      'New',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary(b),
                      ),
                    ),
                  ],
                ),
              ),
            // ── Theme toggle (shown on planning screens) ─────────────────
            if (showThemeToggle) ...[
              const Gap(8),
              GlassBtn(
                brightness: b,
                onTap: () {
                  final appState = MainApp.of(context);
                  if (appState != null) {
                    appState.setThemeMode(
                      isDark ? ThemeMode.light : ThemeMode.dark,
                    );
                  }
                },
                child: Icon(
                  isDark ? Icons.light_mode_rounded : Icons.dark_mode_rounded,
                  size: 20,
                  color: AppColors.textPrimary(b),
                ),
              ),
            ],
            const Gap(8),
            GlassBtn(
              brightness: b,
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const SettingsScreen()),
              ),
              child: Icon(
                Icons.settings_rounded,
                size: 20,
                color: AppColors.textPrimary(b),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
