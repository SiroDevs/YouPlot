import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../theme/app_colors.dart';
import '../../settings/settings_page.dart';
import 'buttons.dart';

class AppHeader extends StatelessWidget {
  final bool showBack;
  final bool showNew;
  final VoidCallback? onBack;
  final VoidCallback? onNew;

  const AppHeader({
    super.key,
    this.showBack = false,
    this.showNew = false,
    this.onBack,
    this.onNew,
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
            const Gap(8),
            GlassBtn(
              brightness: b,
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const SettingsPage()),
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

class SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final Brightness brightness;

  const SectionHeader({
    super.key,
    required this.icon,
    required this.label,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 15, color: AppColors.primary),
        const Gap(7),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: AppColors.textPrimary(brightness),
          ),
        ),
      ],
    );
  }
}
