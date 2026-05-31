import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

/// Empty state shown when user has no routes or plans yet.
/// No CTA — the FAB on the home screen handles creation.
class EmptyState extends StatelessWidget {
  final Brightness brightness;

  const EmptyState({super.key, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 88,
              height: 88,
              decoration: BoxDecoration(
                color: AppColors.primaryDim,
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Icon(Icons.map_outlined,
                  size: 42, color: AppColors.primary),
            ).animate().scale(duration: 400.ms, curve: Curves.elasticOut),
            const Gap(28),
            Text(
              'No routes yet',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
            ).animate().fadeIn(delay: 100.ms),
            const Gap(10),
            Text(
              'Tap + New Plan to start planning\nyour first adventure.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                height: 1.6,
                color: AppColors.textSecondary(b),
              ),
            ).animate().fadeIn(delay: 160.ms),
          ],
        ),
      ),
    );
  }
}
