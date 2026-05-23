import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../theme/app_colors.dart';

class SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final Brightness brightness;

  const SectionHeader({
    super.key,
    required this.icon,
    required this.label,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 15, color: AppColors.primary),
        const Gap(7),
        Text(
          label,
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: AppColors.textPrimary(brightness),
          ),
        ),
      ],
    );
  }
}
