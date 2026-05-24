import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:gap/gap.dart';

import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/route_map.dart';
import '../../../theme/app_colors.dart';

class RouteCard extends StatelessWidget {
  final RouteMap route;
  final Brightness brightness;
  final int index;

  const RouteCard({
    super.key,
    required this.route,
    required this.brightness,
    required this.index,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final distText = Fmt.distance(route.totalDistance, route.unit);

    return Material(
      color: AppColors.card(b),
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        child: Container(
          width: 200,
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.border(b), width: 0.5),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(route.sport.emoji,
                      style: const TextStyle(fontSize: 18)),
                  const Spacer(),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.primaryDim,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      distText,
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        color: AppColors.primary,
                      ),
                    ),
                  ),
                ],
              ),
              const Spacer(),
              Text(
                route.origin.name ?? route.origin.address ?? 'Origin',
                style: TextStyle(
                  fontSize: 12,
                  color: AppColors.textSecondary(b),
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              const Gap(2),
              Row(
                children: [
                  Icon(Icons.arrow_downward_rounded,
                      size: 12, color: AppColors.primary),
                  const Gap(4),
                  Expanded(
                    child: Text(
                      route.destination.name ??
                          route.destination.address ??
                          'Destination',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary(b),
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              const Gap(10),
              Row(
                children: [
                  Icon(Icons.trending_up_rounded,
                      size: 12, color: AppColors.textMuted(b)),
                  const Gap(3),
                  Text(
                    '+${route.totalAscent.round()}m',
                    style: TextStyle(
                      fontSize: 11,
                      color: AppColors.textMuted(b),
                    ),
                  ),
                  const Spacer(),
                  Icon(Icons.chevron_right_rounded,
                      size: 14, color: AppColors.primary),
                ],
              ),
            ],
          ),
        ),
      ),
    ).animate().fadeIn(delay: Duration(milliseconds: 60 * index));
  }
}
