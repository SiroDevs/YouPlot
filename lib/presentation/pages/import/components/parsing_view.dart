import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

class ParsingView extends StatelessWidget {
  final String fileName;
  final double progress;
  final Brightness brightness;

  const ParsingView({super.key, 
    required this.fileName,
    required this.progress,
    required this.brightness,
  });

  String get _label {
    if (progress < 0.3) return 'Reading file…';
    if (progress < 0.5) return 'Parsing waypoints…';
    if (progress < 0.7) return 'Fetching elevation data…';
    if (progress < 0.9) return 'Analysing route…';
    return 'Almost done!';
  }

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                color: AppColors.primaryDim,
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Center(
                child: Icon(Icons.route_rounded, size: 34, color: AppColors.primary),
              ),
            ),
            const Gap(24),
            Text(
              fileName,
              style: TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
              textAlign: TextAlign.center,
            ),
            const Gap(6),
            Text(
              _label,
              style: TextStyle(fontSize: 13, color: AppColors.textSecondary(b)),
            ),
            const Gap(24),
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: progress,
                minHeight: 8,
                backgroundColor: AppColors.border(b),
                valueColor: const AlwaysStoppedAnimation(AppColors.primary),
              ),
            ),
            const Gap(8),
            Align(
              alignment: Alignment.centerRight,
              child: Text(
                '${(progress * 100).round()}%',
                style: TextStyle(
                  fontSize: 11,
                  color: AppColors.textMuted(b),
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
