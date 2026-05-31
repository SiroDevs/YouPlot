import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/utils/formatters.dart';
import '../../../theme/app_colors.dart';

class SpeedSlider extends StatelessWidget {
  final double speed;
  final SportType sport;
  final DistanceUnit unit;
  final Brightness brightness;
  final ValueChanged<double> onChanged;

  const SpeedSlider({
    super.key,
    required this.speed,
    required this.sport,
    required this.unit,
    required this.brightness,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final isRunning = sport == SportType.running;
    final min = Fmt.minSpeed(sport);
    final max = Fmt.maxSpeed(sport);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              isRunning ? Fmt.pace(speed, unit) : Fmt.speed(speed, unit),
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary(b),
              ),
            ),
            if (isRunning)
              Text(
                Fmt.speed(speed, unit),
                style: TextStyle(
                  fontSize: 13,
                  color: AppColors.textSecondary(b),
                ),
              ),
          ],
        ),
        Slider(
          min: min,
          max: max,
          value: speed.clamp(min, max),
          divisions: ((max - min) * 2).round(),
          onChanged: onChanged,
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              Fmt.speedLabel(sport, 'slow'),
              style: TextStyle(fontSize: 10, color: AppColors.textMuted(b)),
            ),
            Text(
              Fmt.speedLabel(sport, 'fast'),
              style: TextStyle(fontSize: 10, color: AppColors.textMuted(b)),
            ),
          ],
        ),
      ],
    );
  }
}
