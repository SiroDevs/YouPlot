import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../widgets/maps/map_convas.dart';
import '../components/route_panel.dart';

class MapStep extends StatelessWidget {
  const MapStep({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final route = state.route!;
        final b = Theme.of(ctx).brightness;
        final isDark = b == Brightness.dark;
        return Scaffold(
          backgroundColor:
              isDark ? const Color(0xFF0D1B2A) : const Color(0xFFE8E0D8),
          body: Stack(
            children: [
              Positioned.fill(
                child: MapCanvas(
                  route: route,
                  brightness: b,
                  isDark: isDark,
                  onBack: () => bloc.add(GoToStep(AppStep.waypoints)),
                  onReset: () => bloc.add(ResetAll()),
                ),
              ),

              Positioned(
                left: 0,
                right: 0,
                bottom: 0,
                child: RoutePanel(
                  route: route,
                  brightness: b,
                  isDark: isDark,
                  onPlan: () => bloc.add(GoToStep(AppStep.plan)),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
