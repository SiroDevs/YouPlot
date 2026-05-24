import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/route_map.dart';
import '../../../../domain/entities/route_plan.dart';
import '../../../bloc/home/home_bloc.dart';
import 'empty_plan_slot.dart';
import 'plan_card.dart';
import 'plan_filter_chips.dart';
import 'route_card.dart';
import 'section_title.dart';

class HomeContent extends StatelessWidget {
  final HomeState state;
  final Brightness brightness;
  final ValueChanged<RouteMap> onRouteSelected;
  final ValueChanged<RoutePlan> onPlanSelected;

  const HomeContent({
    super.key,
    required this.state,
    required this.brightness,
    required this.onRouteSelected,
    required this.onPlanSelected,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
      children: [
        if (state.routes.isNotEmpty) ...[
          SectionTitle(
            icon: Icons.route_rounded,
            label: 'Recent Routes',
            brightness: b,
          ),
          const Gap(12),
          SizedBox(
            height: 150,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: state.routes.length,
              separatorBuilder: (_, _) => const Gap(10),
              itemBuilder: (_, i) => GestureDetector(
                onTap: () => onRouteSelected(state.routes[i]),
                child: RouteCard(
                  route: state.routes[i],
                  brightness: b,
                  index: i,
                ),
              ),
            ),
          ),
          const Gap(28),
        ],

        SectionTitle(
          icon: Icons.calendar_today_rounded,
          label: 'Plans',
          brightness: b,
          trailing: PlanFilterChips(state: state),
        ),
        const Gap(12),
        if (state.activePlans.isEmpty)
          EmptyPlanSlot(
            filter: state.planFilter,
            brightness: b,
          )
        else
          ...state.activePlans.asMap().entries.map(
                (e) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: GestureDetector(
                    onTap: () => onPlanSelected(e.value),
                    child: PlanCard(
                      plan: e.value,
                      brightness: b,
                      index: e.key,
                    ),
                  ),
                ),
              ),
      ],
    );
  }
}
