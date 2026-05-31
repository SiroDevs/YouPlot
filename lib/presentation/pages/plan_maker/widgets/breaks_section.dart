import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/break_selector.dart';

class BreaksSection extends StatelessWidget {
  final Set<BreakType> selected;
  final Brightness brightness;
  final ValueChanged<BreakType> onToggle;

  const BreaksSection({
    super.key,
    required this.selected,
    required this.brightness,
    required this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Tap to include in your daily schedule',
          style: TextStyle(
            fontSize: 11,
            color: AppColors.textSecondary(brightness),
          ),
        ),
        const Gap(12),
        BreakSelector(selected: selected, onToggle: onToggle),
      ],
    );
  }
}
