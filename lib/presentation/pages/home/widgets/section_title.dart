import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

class SectionTitle extends StatelessWidget {
  final IconData icon;
  final String label;
  final Brightness brightness;
  final Widget? trailing;

  const SectionTitle({super.key, 
    required this.icon,
    required this.label,
    required this.brightness,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 16, color: AppColors.primary),
        const Gap(7),
        Text(
          label,
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            color: AppColors.textPrimary(brightness),
          ),
        ),
        if (trailing != null) ...[
          const Spacer(),
          trailing!,
        ],
      ],
    );
  }
}
