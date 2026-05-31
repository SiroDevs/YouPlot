import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../theme/app_colors.dart';

class NewPlanDialog extends StatelessWidget {
  final VoidCallback onScratch;
  final VoidCallback onImport;

  const NewPlanDialog({
    super.key,
    required this.onScratch,
    required this.onImport,
  });

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Dialog(
      backgroundColor: AppColors.surface(b),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'New Plan',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
            ),
            const Gap(6),
            Text(
              'How would you like to start?',
              style: TextStyle(fontSize: 13, color: AppColors.textSecondary(b)),
            ),
            const Gap(20),
            DialogOption(
              icon: Icons.edit_road_rounded,
              color: AppColors.primary,
              title: 'Plan from scratch',
              subtitle: 'Search locations and build your route',
              onTap: onScratch,
              brightness: b,
            ),
            const Gap(12),
            DialogOption(
              icon: Icons.upload_file_rounded,
              color: const Color(0xFF6366F1),
              title: 'Import a file',
              subtitle: 'GPX, KML, TCX or FIT format',
              onTap: onImport,
              brightness: b,
            ),
            const Gap(8),
          ],
        ),
      ),
    );
  }
}

class DialogOption extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final Brightness brightness;

  const DialogOption({
    super.key,
    required this.icon,
    required this.color,
    required this.title,
    required this.subtitle,
    required this.onTap,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return InkWell(
      borderRadius: BorderRadius.circular(14),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: AppColors.card(b),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: AppColors.border(b), width: 0.5),
        ),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, size: 22, color: color),
            ),
            const Gap(14),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary(b),
                  ),
                ),
                const Gap(2),
                Text(
                  subtitle,
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary(b),
                  ),
                ),
              ],
            ).expanded(),
            Icon(
              Icons.chevron_right_rounded,
              size: 18,
              color: AppColors.textMuted(b),
            ),
          ],
        ),
      ),
    );
  }
}
