import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../../../../core/utils/formatters.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/break_selector.dart';
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

class StartTimePicker extends StatelessWidget {
  final DateTime startTime;
  final Brightness brightness;
  final ValueChanged<DateTime> onChanged;

  const StartTimePicker({
    super.key,
    required this.startTime,
    required this.brightness,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return GestureDetector(
      onTap: () async {
        final picked = await showTimePicker(
          context: context,
          initialTime: TimeOfDay.fromDateTime(startTime),
        );
        if (picked != null) {
          onChanged(
            startTime.copyWith(hour: picked.hour, minute: picked.minute),
          );
        }
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color: AppColors.surface(b),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.border(b), width: 0.5),
        ),
        child: Row(
          children: [
            Icon(
              Icons.access_time_rounded,
              size: 16,
              color: AppColors.textSecondary(b),
            ),
            const Gap(10),
            Text(
              Fmt.hhmm(startTime),
              style: TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary(b),
              ),
            ),
            const Spacer(),
            Icon(
              Icons.chevron_right_rounded,
              size: 18,
              color: AppColors.textMuted(b),
            ),
          ],
        ),
      ),
    );
  }
}

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
