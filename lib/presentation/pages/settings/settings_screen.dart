import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../../core/constants/app_constants.dart';
import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import 'components/general.dart';
import 'components/theme_tile.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final b = theme.brightness;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: AppBar(
        backgroundColor: AppColors.bg(b),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: AppColors.textPrimary(b)),
          onPressed: () => Navigator.pop(context),
        ),
        title: Row(children: [
          Container(
            width: 26, height: 26,
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(6),
            ),
            child: const Icon(Icons.settings_rounded, size: 15, color: Colors.white),
          ),
          const Gap(10),
          Text('Settings', style: theme.textTheme.titleLarge),
        ]),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          SectionHeader(label: 'Appearance', brightness: b),
          const Gap(12),
          SettingsCard(
            brightness: b,
            children: [
              ThemeTile(brightness: b),
            ],
          ),

          const Gap(24),

          SectionHeader(label: 'Units', brightness: b),
          const Gap(12),
          BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
            builder: (ctx, state) {
              return SettingsCard(
                brightness: b,
                children: [
                  DropdownTile<DistanceUnit>(
                    brightness: b,
                    icon: Icons.straighten_rounded,
                    label: 'Distance unit',
                    value: state.unit,
                    items: DistanceUnit.values,
                    itemLabel: (u) => u == DistanceUnit.kilometers
                        ? 'Kilometers (km)'
                        : 'Miles (mi)',
                    onChanged: (u) {
                      if (u != null) ctx.read<RouteBuilderBloc>().add(SetUnit(u));
                    },
                  ),
                ],
              );
            },
          ),

          const Gap(24),

          SectionHeader(label: 'Activity', brightness: b),
          const Gap(12),
          BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
            builder: (ctx, state) {
              return SettingsCard(
                brightness: b,
                children: [
                  DropdownTile<SportType>(
                    brightness: b,
                    icon: Icons.directions_run_rounded,
                    label: 'Default sport',
                    value: state.sport,
                    items: SportType.values,
                    itemLabel: (s) => '${s.emoji}  ${s.label}',
                    onChanged: (s) {
                      if (s != null) ctx.read<RouteBuilderBloc>().add(SetSport(s));
                    },
                  ),
                ],
              );
            },
          ),

          const Gap(40),

          Center(
            child: Text(
              'YouPlot v1.0.0',
              style: TextStyle(fontSize: 11, color: AppColors.textMuted(b)),
            ),
          ),
          const Gap(20),
        ],
      ),
    );
  }
}
