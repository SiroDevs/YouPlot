import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../../../core/utils/formatters.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/elevation_chart.dart';
import '../../../widgets/tf_widgets.dart';
import '../components/badges.dart';
import '../components/general.dart';
import '../components/headers.dart';

class MapStep extends StatelessWidget {
  const MapStep({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final route = state.route!;
        final unit = state.unit;
        final sport = state.sport;
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;

        return Stack(
          children: [
            Positioned.fill(
              child: Container(
                color: isDark
                    ? const Color(0xFF0D1B2A)
                    : const Color(0xFFE8E0D8),
                child: Stack(
                  children: [
                    Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.map_rounded,
                            size: 64,
                            color: AppColors.primary.withValues(alpha: 0.3),
                          ),
                          const Gap(12),
                          Text(
                            '${route.origin.name ?? "Start"} → ${route.destination.name ?? "End"}',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              color: AppColors.textSecondary(b),
                              fontSize: 13,
                            ),
                          ),
                          const Gap(6),
                          Text(
                            'MapboxMap widget goes here',
                            style: TextStyle(
                              color: AppColors.textMuted(b),
                              fontSize: 11,
                            ),
                          ),
                        ],
                      ),
                    ),

                    Positioned.fill(
                      child: CustomPaint(painter: RoutePainter(isDark: isDark)),
                    ),
                  ],
                ),
              ),
            ),

            AppHeader(
              showBack: true,
              onBack: () => bloc.add(GoToStep(AppStep.waypoints)),
              showNew: true,
              onNew: () => bloc.add(ResetAll()),
            ),

            Positioned(
              top: 110,
              left: 12,
              child: SportBadge(sport: sport, brightness: b),
            ),

            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: Container(
                decoration: BoxDecoration(
                  color: isDark
                      ? Colors.black.withValues(alpha: 0.9)
                      : Colors.white.withValues(alpha: 0.97),
                  borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(20),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.3),
                      blurRadius: 16,
                      offset: const Offset(0, -4),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Handle
                    Container(
                      width: 36,
                      height: 4,
                      margin: const EdgeInsets.only(top: 10),
                      decoration: BoxDecoration(
                        color: AppColors.border(b),
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                    const Gap(14),

                    // Stats row
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Row(
                        children: [
                          Expanded(
                            child: TFStatCard(
                              label: 'Distance',
                              value: Fmt.distance(route.totalDistanceKm, unit),
                              icon: Icons.straighten_rounded,
                              color: AppColors.sport(sport),
                            ),
                          ),
                          const Gap(8),
                          Expanded(
                            child: TFStatCard(
                              label: 'Ascent',
                              value:
                                  '+${Fmt.elevation(route.totalAscentM, unit)}',
                              icon: Icons.trending_up_rounded,
                              color: AppColors.warning,
                            ),
                          ),
                          const Gap(8),
                          Expanded(
                            child: TFStatCard(
                              label: 'Descent',
                              value:
                                  '-${Fmt.elevation(route.totalDescentM, unit)}',
                              icon: Icons.trending_down_rounded,
                              color: AppColors.accent,
                            ),
                          ),
                        ],
                      ),
                    ),

                    Padding(
                      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Elevation profile',
                            style: TextStyle(
                              fontSize: 11,
                              color: AppColors.textSecondary(b),
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const Gap(6),
                          ElevationChart(
                            points: route.elevation,
                            unit: unit,
                            height: 100,
                          ),
                        ],
                      ),
                    ),

                    if (route.waypoints.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
                        child: SingleChildScrollView(
                          scrollDirection: Axis.horizontal,
                          child: Row(
                            children: route.waypoints
                                .map(
                                  (w) => Container(
                                    margin: const EdgeInsets.only(right: 8),
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 4,
                                    ),
                                    decoration: BoxDecoration(
                                      color: AppColors.card(b),
                                      borderRadius: BorderRadius.circular(6),
                                      border: Border.all(
                                        color: AppColors.border(b),
                                        width: 0.5,
                                      ),
                                    ),
                                    child: Text(
                                      w.label,
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: AppColors.textPrimary(b),
                                      ),
                                    ),
                                  ),
                                )
                                .toList(),
                          ),
                        ),
                      ),

                    const Gap(16),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 0),
                      child: SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: () => bloc.add(GoToStep(AppStep.plan)),
                          icon: const Icon(Icons.tune_rounded, size: 16),
                          label: const Text('Plan this route'),
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.all(14),
                          ),
                        ),
                      ),
                    ),
                    const SafeArea(top: false, child: Gap(12)),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}
