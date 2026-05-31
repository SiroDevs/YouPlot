import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../main.dart';
import '../../../theme/app_colors.dart';
import 'general.dart';

class ThemeTile extends StatelessWidget {
  final Brightness brightness;
  const ThemeTile({super.key, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final appState = MainApp.of(context);
    final mode = appState?.themeMode ?? ThemeMode.dark;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      child: Row(
        children: [
          Icon(Icons.dark_mode_rounded, size: 20, color: AppColors.primary),
          const Gap(14),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Appearance',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: AppColors.textPrimary(brightness),
                ),
              ),
              Text(
                'Choose light or dark theme',
                style: TextStyle(
                  fontSize: 11,
                  color: AppColors.textSecondary(brightness),
                ),
              ),
            ],
          ).expanded(),
          const Gap(12),
          Container(
            height: 34,
            decoration: BoxDecoration(
              color: AppColors.surface(brightness),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: AppColors.border(brightness),
                width: 0.5,
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                ModeBtn(
                  icon: Icons.light_mode_rounded,
                  label: 'Light',
                  active: mode == ThemeMode.light,
                  brightness: brightness,
                  onTap: () => appState?.setThemeMode(ThemeMode.light),
                ),
                ModeBtn(
                  icon: Icons.dark_mode_rounded,
                  label: 'Dark',
                  active: mode == ThemeMode.dark,
                  brightness: brightness,
                  onTap: () => appState?.setThemeMode(ThemeMode.dark),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
