import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../theme/app_colors.dart';

class SelectedBadge extends StatelessWidget {
  final String label;
  final Brightness brightness;
  const SelectedBadge({
    super.key,
    required this.label,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 6,
          height: 6,
          decoration: const BoxDecoration(
            color: AppColors.primary,
            shape: BoxShape.circle,
          ),
        ),
        const Gap(6),
        Flexible(
          child: Text(
            label,
            style: TextStyle(
              fontSize: 11,
              color: AppColors.textSecondary(brightness),
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}

class SportBadge extends StatelessWidget {
  final SportType sport;
  final Brightness brightness;
  const SportBadge({super.key, required this.sport, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final isDark = b == Brightness.dark;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: isDark
            ? Colors.black.withValues(alpha: 0.75)
            : Colors.white.withValues(alpha: 0.92),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border(b)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.15),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(sport.emoji, style: const TextStyle(fontSize: 13)),
          const Gap(5),
          Text(
            sport.label,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary(b),
            ),
          ),
        ],
      ),
    );
  }
}
