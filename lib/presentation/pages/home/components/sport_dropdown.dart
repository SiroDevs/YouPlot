import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../theme/app_colors.dart';

class SportDropdown extends StatelessWidget {
  final Brightness brightness;
  final SportType value;
  final ValueChanged<SportType?> onChanged;

  const SportDropdown({
    super.key,
    required this.brightness,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.surface(b),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: DropdownButton<SportType>(
        value: value,
        isExpanded: true,
        underline: const SizedBox.shrink(),
        dropdownColor: AppColors.card(b),
        icon: Icon(
          Icons.expand_more_rounded,
          color: AppColors.textSecondary(b),
        ),
        style: TextStyle(
          fontSize: 14,
          color: AppColors.textPrimary(b),
          fontWeight: FontWeight.w500,
        ),
        selectedItemBuilder: (_) => SportType.values
            .map(
              (s) => Row(
                children: [
                  Text(s.emoji, style: const TextStyle(fontSize: 18)),
                  const Gap(10),
                  Text(
                    s.label,
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textPrimary(b),
                    ),
                  ),
                ],
              ),
            )
            .toList(),
        items: SportType.values
            .map(
              (s) => DropdownMenuItem<SportType>(
                value: s,
                child: Row(
                  children: [
                    Text(s.emoji, style: const TextStyle(fontSize: 20)),
                    const Gap(12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          s.label,
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: AppColors.textPrimary(b),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            )
            .toList(),
        onChanged: onChanged,
      ),
    );
  }
}
