import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:intl/intl.dart';

import '../../../theme/app_colors.dart';

class StartDateTimePicker extends StatelessWidget {
  final DateTime startTime;
  final Brightness brightness;
  final ValueChanged<DateTime> onChanged;

  const StartDateTimePicker({
    super.key,
    required this.startTime,
    required this.brightness,
    required this.onChanged,
  });

  String _fmtDate(DateTime dt) => DateFormat('EEE, MMM d').format(dt);
  String _fmtTime(DateTime dt) => DateFormat('h:mm a').format(dt);

  ThemeData _pickerTheme(BuildContext context) => Theme.of(context).copyWith(
    colorScheme: Theme.of(
      context,
    ).colorScheme.copyWith(primary: AppColors.primary),
  );

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Row(
      children: [
        Expanded(
          flex: 5,
          child: GestureDetector(
            onTap: () async {
              final picked = await showDatePicker(
                context: context,
                initialDate: startTime,
                firstDate: DateTime.now().subtract(const Duration(days: 1)),
                lastDate: DateTime.now().add(const Duration(days: 365 * 3)),
                builder: (ctx, child) =>
                    Theme(data: _pickerTheme(ctx), child: child!),
              );
              if (picked != null) {
                onChanged(
                  startTime.copyWith(
                    year: picked.year,
                    month: picked.month,
                    day: picked.day,
                  ),
                );
              }
            },
            child: _PickerCell(
              icon: Icons.calendar_today_rounded,
              label: _fmtDate(startTime),
              brightness: b,
              isLeft: true,
            ),
          ),
        ),
        const Gap(8),

        Expanded(
          flex: 4,
          child: GestureDetector(
            onTap: () async {
              final picked = await showTimePicker(
                context: context,
                initialTime: TimeOfDay.fromDateTime(startTime),
                builder: (ctx, child) =>
                    Theme(data: _pickerTheme(ctx), child: child!),
              );
              if (picked != null) {
                onChanged(
                  startTime.copyWith(hour: picked.hour, minute: picked.minute),
                );
              }
            },
            child: _PickerCell(
              icon: Icons.access_time_rounded,
              label: _fmtTime(startTime),
              brightness: b,
              isLeft: false,
            ),
          ),
        ),
      ],
    );
  }
}

class _PickerCell extends StatelessWidget {
  final IconData icon;
  final String label;
  final Brightness brightness;
  final bool isLeft;

  const _PickerCell({
    required this.icon,
    required this.label,
    required this.brightness,
    required this.isLeft,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 13),
      decoration: BoxDecoration(
        color: AppColors.surface(b),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: AppColors.primary),
          const Gap(7),
          Flexible(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary(b),
              ),
              overflow: TextOverflow.ellipsis,
              maxLines: 1,
            ),
          ),
          const Gap(4),
          Icon(
            Icons.unfold_more_rounded,
            size: 14,
            color: AppColors.textMuted(b),
          ),
        ],
      ),
    );
  }
}
