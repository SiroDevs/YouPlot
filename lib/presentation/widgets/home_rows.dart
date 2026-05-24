import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../domain/entities/location.dart';
import '../../domain/entities/waypoint.dart';
import '../theme/app_colors.dart';

class ViaRow extends StatelessWidget {
  final int index;
  final Location location;
  final VoidCallback onRemove;
  final Brightness brightness;

  const ViaRow({
    super.key,
    required this.index,
    required this.location,
    required this.onRemove,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.card(b),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.border(b), width: 0.5),
      ),
      child: Row(
        children: [
          Container(
            width: 22,
            height: 22,
            decoration: BoxDecoration(
              color: AppColors.primaryDim,
              borderRadius: BorderRadius.circular(5),
            ),
            child: Center(
              child: Text(
                '${index + 1}',
                style: const TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.w700,
                  color: AppColors.primary,
                ),
              ),
            ),
          ),
          const Gap(10),
          Text(
            location.name ?? location.toString(),
            style: TextStyle(fontSize: 13, color: AppColors.textPrimary(b)),
          ).expanded(),
          GestureDetector(
            onTap: onRemove,
            child: Icon(
              Icons.close_rounded,
              size: 16,
              color: AppColors.textMuted(b),
            ),
          ),
        ],
      ),
    );
  }
}

class WaypointRow extends StatelessWidget {
  final Waypoint waypoint;
  final Brightness brightness;
  const WaypointRow({
    super.key,
    required this.waypoint,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: AppColors.card(b),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.border(b), width: 0.5),
        ),
        child: Row(
          children: [
            const Icon(Icons.place_rounded, size: 15, color: AppColors.primary),
            const Gap(10),
            Text(
              waypoint.label,
              style: TextStyle(fontSize: 13, color: AppColors.textPrimary(b)),
            ).expanded(),
            if (waypoint.distanceFromStartKm != null)
              Text(
                '${waypoint.distanceFromStartKm!.toStringAsFixed(0)} km',
                style: TextStyle(
                  fontSize: 11,
                  color: AppColors.textSecondary(b),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
