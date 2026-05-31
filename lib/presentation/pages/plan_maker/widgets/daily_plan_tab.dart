import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../domain/entities/route_plan.dart';
import '../../../widgets/steps/day_card.dart';

class DailyPlanTab extends StatelessWidget {
  final RoutePlan plan;
  final DistanceUnit unit;
  final SportType sport;
  final Brightness brightness;

  const DailyPlanTab({
    super.key,
    required this.plan,
    required this.unit,
    required this.sport,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: plan.segments
          .map(
            (seg) => Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: DayCard(
                segment: seg,
                unit: unit,
                sport: sport,
                brightness: brightness,
              ),
            ),
          )
          .toList(),
    );
  }
}
