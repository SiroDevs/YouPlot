import 'package:flutter/material.dart';

import '../../core/constants/app_constants.dart';
import '../theme/app_colors.dart';

class BreakSelector extends StatelessWidget {
  final Set<BreakType> selected;
  final ValueChanged<BreakType> onToggle;

  const BreakSelector(
      {super.key, required this.selected, required this.onToggle});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: BreakType.values.map((t) {
        final on = selected.contains(t);
        return GestureDetector(
          onTap: () => onToggle(t),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: on ? AppColors.primaryDim : AppColors.surface(b),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: on ? AppColors.primary : AppColors.border(b),
                width: on ? 1 : 0.5,
              ),
            ),
            child: Row(mainAxisSize: MainAxisSize.min, children: [
              Text(t.emoji, style: const TextStyle(fontSize: 15)),
              const SizedBox(width: 6),
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(t.label,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight:
                          on ? FontWeight.w600 : FontWeight.w400,
                      color: on
                          ? AppColors.primary
                          : AppColors.textSecondary(b),
                    )),
                Text('${t.defaultMinutes} min',
                    style: TextStyle(
                        fontSize: 10, color: AppColors.textMuted(b))),
              ]),
            ]),
          ),
        );
      }).toList(),
    );
  }
}
