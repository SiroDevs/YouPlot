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

  String _fmtDate(DateTime dt) => DateFormat('EEE, MMM d, yyyy').format(dt);
  String _fmtTime(DateTime dt) => DateFormat('h:mm a').format(dt);

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Column(
      children: [
        // Date row
        GestureDetector(
          onTap: () async {
            final picked = await showDatePicker(
              context: context,
              initialDate: startTime,
              firstDate: DateTime.now().subtract(const Duration(days: 1)),
              lastDate: DateTime.now().add(const Duration(days: 365 * 3)),
              builder: (ctx, child) => Theme(
                data: Theme.of(ctx).copyWith(
                  colorScheme: Theme.of(ctx).colorScheme.copyWith(
                        primary: AppColors.primary,
                      ),
                ),
                child: child!,
              ),
            );
            if (picked != null) {
              onChanged(startTime.copyWith(
                year: picked.year,
                month: picked.month,
                day: picked.day,
              ));
            }
          },
          child: _PickerRow(
            icon: Icons.calendar_today_rounded,
            label: _fmtDate(startTime),
            brightness: b,
          ),
        ),
        const Gap(10),

        GestureDetector(
          onTap: () async {
            final picked = await showTimePicker(
              context: context,
              initialTime: TimeOfDay.fromDateTime(startTime),
              builder: (ctx, child) => Theme(
                data: Theme.of(ctx).copyWith(
                  colorScheme: Theme.of(ctx).colorScheme.copyWith(
                        primary: AppColors.primary,
                      ),
                ),
                child: child!,
              ),
            );
            if (picked != null) {
              onChanged(
                startTime.copyWith(hour: picked.hour, minute: picked.minute),
              );
            }
          },
          child: _PickerRow(
            icon: Icons.access_time_rounded,
            label: _fmtTime(startTime),
            brightness: b,
          ),
        ),
      ],
    );
  }
}

class _PickerRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final Brightness brightness;

  const _PickerRow({
    required this.icon,
    required this.label,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppColors.surface(b),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Row(
        children: [
          Icon(icon, size: 16, color: AppColors.textSecondary(b)),
          const Gap(10),
          Text(
            label,
            style: TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary(b),
            ),
          ),
          const Spacer(),
          Icon(Icons.chevron_right_rounded,
              size: 18, color: AppColors.textMuted(b)),
        ],
      ),
    );
  }
}
