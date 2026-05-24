import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';
import '../../../widgets/maps/map_background.dart';

class PlanStep3 extends StatelessWidget {
  const PlanStep3({super.key});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Stack(
      children: [
        MapBackground(),
        Center(
          child: Container(
            padding: const EdgeInsets.all(28),
            margin: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: b == Brightness.dark
                  ? Colors.black.withValues(alpha: 0.85)
                  : Colors.white.withValues(alpha: 0.95),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                CircularProgressIndicator(
                  color: AppColors.primary,
                  strokeWidth: 2,
                ),
                const Gap(20),
                Text(
                  'Preparing your route …',
                  style: TextStyle(
                    color: Colors.black,
                    fontSize: 15,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
