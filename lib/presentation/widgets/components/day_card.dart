import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../core/constants/app_constants.dart';
import '../../../../../core/utils/formatters.dart';
import '../../../domain/entities/daily_segment.dart';
import '../../theme/app_colors.dart';

class DayCard extends StatelessWidget {
  final DailySegment segment;
  final DistanceUnit unit;
  final SportType sport;
  final Brightness brightness;

  const DayCard({super.key, 
    required this.segment,
    required this.unit,
    required this.sport,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.card(b),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
            decoration: BoxDecoration(
              color: AppColors.surface(b),
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(11),
              ),
            ),
            child: Row(
              children: [
                Text(
                  'Day ${segment.day}',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary(b),
                  ),
                ),
                const Spacer(),
                Text(
                  Fmt.distance(segment.distanceKm, unit),
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    color: AppColors.primary,
                  ),
                ),
                const Gap(12),
                Text(
                  '${Fmt.hhmm(segment.departureTime)} → ${Fmt.hhmm(segment.estimatedArrival)}',
                  style: TextStyle(
                    fontSize: 11,
                    color: AppColors.textSecondary(b),
                  ),
                ),
              ],
            ),
          ),
          if (segment.breaks.isNotEmpty)
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                children: segment.breaks
                    .map(
                      (br) => Padding(
                        padding: const EdgeInsets.symmetric(vertical: 3),
                        child: Row(
                          children: [
                            Text(
                              br.type.emoji,
                              style: const TextStyle(fontSize: 14),
                            ),
                            const Gap(8),
                            Text(
                              br.type.label,
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.textPrimary(b),
                              ),
                            ),
                            const Spacer(),
                            Text(
                              Fmt.hhmm(br.scheduledAt),
                              style: TextStyle(
                                fontSize: 11,
                                color: AppColors.textSecondary(b),
                              ),
                            ),
                            const Gap(8),
                            Text(
                              Fmt.duration(br.duration),
                              style: TextStyle(
                                fontSize: 11,
                                color: AppColors.textMuted(b),
                              ),
                            ),
                          ],
                        ),
                      ),
                    )
                    .toList(),
              ),
            )
          else
            Padding(
              padding: const EdgeInsets.all(12),
              child: Text(
                'No breaks scheduled this day',
                style: TextStyle(fontSize: 11, color: AppColors.textMuted(b)),
              ),
            ),
        ],
      ),
    );
  }
}
