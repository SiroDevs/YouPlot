import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

class EmptyState extends StatelessWidget {
  final VoidCallback onCreateNew;
  final Brightness brightness;

  const EmptyState({super.key, required this.onCreateNew, required this.brightness});

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
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: AppColors.primaryDim,
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Icon(Icons.map_outlined,
                  size: 38, color: AppColors.primary),
            ).animate().scale(duration: 400.ms, curve: Curves.elasticOut),
            const Gap(24),
            Text(
              'No routes yet',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
            ).animate().fadeIn(delay: 100.ms),
            const Gap(8),
            Text(
              'Plan your first route and it will\nappear here for easy access.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                height: 1.5,
                color: AppColors.textSecondary(b),
              ),
            ).animate().fadeIn(delay: 160.ms),
            const Gap(32),
            ElevatedButton.icon(
              onPressed: onCreateNew,
              icon: const Icon(Icons.add_rounded, size: 18),
              label: const Text('Create your first plan'),
              style: ElevatedButton.styleFrom(
                padding:
                    const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
              ),
            ).animate().fadeIn(delay: 220.ms),
          ],
        ),
      ),
    );
  }
}
