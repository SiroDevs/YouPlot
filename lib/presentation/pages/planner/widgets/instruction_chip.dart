import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

class InstructionChip extends StatelessWidget {
  final bool isDark;
  final bool hasPinned;
  final bool resolving;

  const InstructionChip({super.key, 
    required this.isDark,
    required this.hasPinned,
    required this.resolving,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 250),
      child: Container(
        key: ValueKey(hasPinned),
        padding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: isDark
              ? Colors.black.withValues(alpha: 0.82)
              : Colors.white.withValues(alpha: 0.95),
          borderRadius: BorderRadius.circular(22),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.15),
              blurRadius: 10,
            ),
          ],
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (resolving)
              SizedBox(
                width: 12,
                height: 12,
                child: CircularProgressIndicator(
                    strokeWidth: 1.5, color: AppColors.primary),
              )
            else
              Icon(
                hasPinned
                    ? Icons.check_circle_outline_rounded
                    : Icons.touch_app_rounded,
                size: 14,
                color: hasPinned ? AppColors.primary : Colors.grey,
              ),
            const Gap(6),
            Text(
              resolving
                  ? 'Resolving address…'
                  : hasPinned
                      ? 'Confirm or tap again to move'
                      : 'Tap anywhere to drop a pin',
              style: TextStyle(
                fontSize: 13,
                color: isDark ? Colors.white70 : Colors.black54,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
