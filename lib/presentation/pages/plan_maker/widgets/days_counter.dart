import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';
import '../../../widgets/steps/buttons.dart';

class DaysCounter extends StatelessWidget {
  final int days;
  final Brightness brightness;
  final ValueChanged<int> onChanged;

  const DaysCounter({
    super.key,
    required this.days,
    required this.brightness,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        CounterBtn(
          icon: Icons.remove_rounded,
          brightness: b,
          onTap: days > 1 ? () => onChanged(days - 1) : null,
        ),
        const Gap(24),
        Column(
          children: [
            Text('$days', style: Theme.of(context).textTheme.displayLarge),
            Text(
              days == 1 ? 'day' : 'days',
              style: TextStyle(fontSize: 12, color: AppColors.textSecondary(b)),
            ),
          ],
        ),
        const Gap(24),
        CounterBtn(
          icon: Icons.add_rounded,
          brightness: b,
          onTap: () => onChanged(days + 1),
        ),
      ],
    );
  }
}
