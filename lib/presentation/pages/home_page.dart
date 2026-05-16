import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:share_plus/share_plus.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../domain/entities/entities.dart';
import '../bloc/route_builder/route_builder_bloc.dart';
import '../theme/app_theme.dart';
import '../widgets/widgets.dart';

// ─── Root shell ───────────────────────────────────────────────────────────────

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        return Scaffold(
          backgroundColor: AppColors.bg,
          appBar: _buildAppBar(ctx, state),
          body: Stack(
            children: [
              AnimatedSwitcher(
                duration: const Duration(milliseconds: 280),
                transitionBuilder: (child, anim) => FadeTransition(
                  opacity: anim,
                  child: SlideTransition(
                    position:
                        Tween(begin: const Offset(0.04, 0), end: Offset.zero)
                            .animate(anim),
                    child: child,
                  ),
                ),
                child: KeyedSubtree(
                  key: ValueKey(state.step),
                  child: _pageForStep(state),
                ),
              ),
              if (state.loading)
                TFLoadingOverlay(message: _loadingMessage(state.step)),
            ],
          ),
        );
      },
    );
  }

  AppBar _buildAppBar(BuildContext ctx, RouteBuilderState state) {
    final bloc = ctx.read<RouteBuilderBloc>();
    return AppBar(
      leading: state.step != AppStep.setup
          ? IconButton(
              icon: const Icon(Icons.arrow_back_rounded),
              onPressed: () => bloc.add(GoToStep(_prevStep(state.step))),
            )
          : null,
      title: Row(children: [
        Container(
          width: 26,
          height: 26,
          decoration: BoxDecoration(
            color: AppColors.primary,
            borderRadius: BorderRadius.circular(6),
          ),
          child: const Icon(Icons.route_rounded, size: 15, color: Colors.black),
        ),
        const Gap(10),
        const Text(kAppName),
      ]),
      actions: [
        if (state.step != AppStep.setup && state.step != AppStep.generating)
          TextButton(
            onPressed: () => bloc.add(ResetAll()),
            child: const Text('New',
                style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
          ),
        const Gap(8),
      ],
      bottom: PreferredSize(
        preferredSize: const Size.fromHeight(36),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 0, 16, 10),
          child: StepBar(current: state.step.index.clamp(0, 4), total: 5),
        ),
      ),
    );
  }

  Widget _pageForStep(RouteBuilderState state) {
    switch (state.step) {
      case AppStep.setup:      return const _SetupPage();
      case AppStep.waypoints:  return const _WaypointsPage();
      case AppStep.generating: return const _GeneratingPage();
      case AppStep.map:        return const _MapPage();
      case AppStep.plan:       return const _PlanPage();
      case AppStep.review:     return const _ReviewPage();
      case AppStep.export:     return const _ExportPage();
    }
  }

  String _loadingMessage(AppStep step) {
    switch (step) {
      case AppStep.generating: return 'Building route…';
      case AppStep.waypoints:  return 'Finding waypoints…';
      case AppStep.plan:       return 'Scheduling plan…';
      case AppStep.export:     return 'Exporting file…';
      default:                 return 'Please wait…';
    }
  }

  AppStep _prevStep(AppStep step) {
    switch (step) {
      case AppStep.waypoints:  return AppStep.setup;
      case AppStep.map:        return AppStep.waypoints;
      case AppStep.plan:       return AppStep.map;
      case AppStep.review:     return AppStep.plan;
      case AppStep.export:     return AppStep.review;
      default:                 return AppStep.setup;
    }
  }
}

// ─── Step 1: Setup ────────────────────────────────────────────────────────────

class _SetupPage extends StatelessWidget {
  const _SetupPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        return SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Gap(4),
              Text('Plan your route', style: Theme.of(ctx).textTheme.displayMedium)
                  .animate().fadeIn(delay: 50.ms).slideY(begin: 0.08),
              const Gap(4),
              const Text('Choose your sport, set distance units, then pick your start and end.',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13))
                  .animate().fadeIn(delay: 100.ms),
              const Gap(24),

              // Units row
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Units',
                      style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: AppColors.textPrimary,
                          fontSize: 14)),
                  UnitToggle(
                    value: state.unit,
                    onChanged: (u) => bloc.add(SetUnit(u)),
                  ),
                ],
              ).animate().fadeIn(delay: 120.ms),
              const Gap(20),

              // Sport grid
              const Text('Sport',
                  style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                      fontSize: 14))
                  .animate().fadeIn(delay: 140.ms),
              const Gap(10),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: SportType.values
                    .map((s) => SportChip(
                          sport: s,
                          selected: state.sport == s,
                          onTap: () => bloc.add(SetSport(s)),
                        ))
                    .toList(),
              ).animate().fadeIn(delay: 160.ms),
              const Gap(24),

              // Origin
              const _SectionHeader(icon: Icons.trip_origin_rounded, label: 'Starting point'),
              const Gap(10),
              LocationField(
                hint: 'Search city or address…',
                value: state.origin,
                showGps: true,
                onSelected: (loc) => bloc.add(SetOrigin(loc)),
              ).animate().fadeIn(delay: 200.ms),
              if (state.origin != null) ...[
                const Gap(6),
                _SelectedBadge(
                    label: state.origin!.address ?? state.origin!.name ?? ''),
              ],
              const Gap(20),

              // Destination
              const _SectionHeader(icon: Icons.flag_rounded, label: 'Destination'),
              const Gap(10),
              LocationField(
                hint: 'Search destination…',
                value: state.destination,
                onSelected: (loc) => bloc.add(SetDestination(loc)),
              ).animate().fadeIn(delay: 240.ms),
              if (state.destination != null) ...[
                const Gap(6),
                _SelectedBadge(
                    label: state.destination!.address ??
                        state.destination!.name ??
                        ''),
              ],
              const Gap(32),

              if (state.error != null)
                TFErrorBar(
                    message: state.error!,
                    onDismiss: () => bloc.add(ResetAll())),
              const Gap(8),

              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: state.canProceed
                      ? () => bloc.add(GoToStep(AppStep.waypoints))
                      : null,
                  icon: const Icon(Icons.arrow_forward_rounded, size: 17),
                  label: const Text('Continue'),
                  style: ElevatedButton.styleFrom(padding: const EdgeInsets.all(15)),
                ),
              ).animate().fadeIn(delay: 280.ms),
              const Gap(24),
            ],
          ),
        );
      },
    );
  }
}

// ─── Step 2: Waypoints ────────────────────────────────────────────────────────

class _WaypointsPage extends StatelessWidget {
  const _WaypointsPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        return SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Route waypoints', style: Theme.of(ctx).textTheme.displayMedium),
              const Gap(4),
              Text(
                '${state.origin?.name ?? "Start"} → ${state.destination?.name ?? "End"}',
                style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
              ),
              const Gap(28),

              // Option 1: App suggestions
              _OptionCard(
                emoji: '🤖',
                title: 'App suggestions',
                subtitle: 'Auto-pick major towns along the way',
                onTap: () => bloc.add(RequestSuggestions()),
              ),
              const Gap(12),

              // Option 2: Custom via-points
              _OptionCard(
                emoji: '✏️',
                title: 'My own stops',
                subtitle: 'Add specific places you want to pass through',
                onTap: () => _showCustomSheet(ctx, bloc),
              ),
              const Gap(12),

              // Option 3: Direct
              _OptionCard(
                emoji: '⚡',
                title: 'Direct route',
                subtitle: 'No intermediate stops — straight from A to B',
                onTap: () => bloc.add(GenerateRoute()),
              ),

              if (state.viaPoints.isNotEmpty) ...[
                const Gap(20),
                const _SectionHeader(icon: Icons.place_rounded, label: 'Your stops'),
                const Gap(8),
                ...state.viaPoints.asMap().entries.map((e) => Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: _ViaRow(
                        index: e.key,
                        location: e.value,
                        onRemove: () => bloc.add(RemoveViaPoint(e.key)),
                      ),
                    )),
                const Gap(16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () => bloc.add(GenerateRoute()),
                    icon: const Icon(Icons.map_rounded, size: 16),
                    label: const Text('Generate Route'),
                  ),
                ),
              ],

              if (state.suggestions.isNotEmpty) ...[
                const Gap(20),
                const _SectionHeader(icon: Icons.auto_awesome_rounded, label: 'Suggested stops'),
                const Gap(8),
                ...state.suggestions.map((w) => _WaypointRow(waypoint: w)),
                const Gap(16),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () => bloc.add(GenerateRoute()),
                    icon: const Icon(Icons.map_rounded, size: 16),
                    label: const Text('Generate Route with These Stops'),
                  ),
                ),
              ],

              if (state.error != null) ...[
                const Gap(12),
                TFErrorBar(message: state.error!),
              ],
            ],
          ),
        );
      },
    );
  }

  void _showCustomSheet(BuildContext ctx, RouteBuilderBloc bloc) {
    showModalBottomSheet(
      context: ctx,
      isScrollControlled: true,
      builder: (_) => BlocProvider.value(
        value: bloc,
        child: const _AddViaSheet(),
      ),
    );
  }
}

class _AddViaSheet extends StatelessWidget {
  const _AddViaSheet();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        Container(
          width: 36, height: 4,
          decoration: BoxDecoration(
            color: AppColors.border, borderRadius: BorderRadius.circular(2)),
        ),
        const Gap(16),
        const Text('Add a stop',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        const Gap(16),
        LocationField(
          hint: 'Search town or landmark…',
          onSelected: (loc) {
            bloc.add(AddViaPoint(loc));
            Navigator.pop(context);
          },
        ),
      ]),
    );
  }
}

// ─── Step 3: Generating ───────────────────────────────────────────────────────

class _GeneratingPage extends StatelessWidget {
  const _GeneratingPage();

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        CircularProgressIndicator(color: AppColors.primary, strokeWidth: 2),
        Gap(20),
        Text('Calculating your route…',
            style: TextStyle(color: AppColors.textSecondary, fontSize: 15)),
        Gap(6),
        Text('Fetching elevation data',
            style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
      ]),
    );
  }
}

// ─── Step 4: Map + elevation ──────────────────────────────────────────────────

class _MapPage extends StatelessWidget {
  const _MapPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final route = state.route!;
        final unit = state.unit;
        final sport = state.sport;

        return Column(children: [
          // ── Map area ──────────────────────────────────────────────────────
          Expanded(
            flex: 5,
            child: Container(
              color: const Color(0xFF0D1B2A),
              child: Stack(children: [
                // MapboxMap widget is wired here.
                // Replace this placeholder with:
                //   MapboxMap(
                //     mapboxOptions: MapboxOptions(accessToken: kMapboxToken),
                //     styleUri: kMapboxStyleDark,
                //     onMapCreated: (c) => _drawRoute(c, route),
                //     cameraOptions: CameraOptions(
                //       center: Point(coordinates:
                //         Position(route.origin.lng, route.origin.lat)),
                //       zoom: 9,
                //     ),
                //   )
                Center(
                  child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
                    Icon(Icons.map_rounded,
                        size: 52,
                        color: AppColors.primary.withOpacity(0.35)),
                    const Gap(12),
                    Text(
                      '${route.origin.name ?? "Start"} → ${route.destination.name ?? "End"}',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                          color: AppColors.textSecondary, fontSize: 13),
                    ),
                    const Gap(6),
                    Text(
                      'Wire MapboxMap widget here\n(see SETUP.md)',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                          color: AppColors.textMuted, fontSize: 11),
                    ),
                  ]),
                ),

                // Sport badge
                Positioned(
                  top: 12,
                  left: 12,
                  child: _SportBadge(sport: sport),
                ),
              ]),
            ),
          ),

          // ── Info panel ────────────────────────────────────────────────────
          Expanded(
            flex: 4,
            child: Container(
              color: AppColors.surface,
              child: Column(children: [
                // Stats row
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 14, 16, 0),
                  child: Row(children: [
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
                        value: '+${Fmt.elevation(route.totalAscentM, unit)}',
                        icon: Icons.trending_up_rounded,
                        color: AppColors.warning,
                      ),
                    ),
                    const Gap(8),
                    Expanded(
                      child: TFStatCard(
                        label: 'Descent',
                        value: '-${Fmt.elevation(route.totalDescentM, unit)}',
                        icon: Icons.trending_down_rounded,
                        color: AppColors.accent,
                      ),
                    ),
                  ]),
                ),

                // Elevation graph
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 14, 16, 0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Elevation profile',
                          style: TextStyle(
                              fontSize: 11,
                              color: AppColors.textSecondary,
                              fontWeight: FontWeight.w500)),
                      const Gap(8),
                      ElevationChart(
                          points: route.elevation, unit: unit, height: 110),
                    ],
                  ),
                ),

                // Waypoints (major towns)
                if (route.waypoints.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Route passes through',
                            style: TextStyle(
                                fontSize: 11,
                                color: AppColors.textSecondary,
                                fontWeight: FontWeight.w500)),
                        const Gap(6),
                        SingleChildScrollView(
                          scrollDirection: Axis.horizontal,
                          child: Row(
                            children: route.waypoints
                                .map((w) => Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Container(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 10, vertical: 4),
                                        decoration: BoxDecoration(
                                          color: AppColors.card,
                                          borderRadius: BorderRadius.circular(6),
                                          border: Border.all(
                                              color: AppColors.border,
                                              width: 0.5),
                                        ),
                                        child: Text(w.label,
                                            style: const TextStyle(
                                                fontSize: 11,
                                                color: AppColors.textPrimary)),
                                      ),
                                    ))
                                .toList(),
                          ),
                        ),
                      ],
                    ),
                  ),

                const Spacer(),
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                  child: SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: () => bloc.add(GoToStep(AppStep.plan)),
                      icon: const Icon(Icons.tune_rounded, size: 16),
                      label: const Text('Plan this route'),
                      style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.all(14)),
                    ),
                  ),
                ),
              ]),
            ),
          ),
        ]);
      },
    );
  }
}

// ─── Step 5: Plan (days, speed, breaks) ──────────────────────────────────────

class _PlanPage extends StatelessWidget {
  const _PlanPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final isRunning = state.sport == SportType.running;
        return SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Plan your journey', style: Theme.of(ctx).textTheme.displayMedium),
              const Gap(4),
              const Text('Days, speed, start time and breaks.',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
              const Gap(28),

              // ── Days ─────────────────────────────────────────────────────
              const _SectionHeader(icon: Icons.calendar_today_rounded, label: 'Total days'),
              const Gap(12),
              Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                _CounterBtn(
                  icon: Icons.remove_rounded,
                  onTap: state.days > 1 ? () => bloc.add(SetDays(state.days - 1)) : null,
                ),
                const Gap(24),
                Column(children: [
                  Text('${state.days}',
                      style: Theme.of(ctx).textTheme.displayLarge),
                  Text(state.days == 1 ? 'day' : 'days',
                      style: const TextStyle(
                          fontSize: 12, color: AppColors.textSecondary)),
                ]),
                const Gap(24),
                _CounterBtn(
                  icon: Icons.add_rounded,
                  onTap: () => bloc.add(SetDays(state.days + 1)),
                ),
              ]),
              const Gap(28),

              // ── Speed ─────────────────────────────────────────────────────
              _SectionHeader(
                icon: Icons.speed_rounded,
                label: isRunning ? 'Pace' : 'Speed',
              ),
              const Gap(12),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    isRunning
                        ? Fmt.pace(state.speedKmh, state.unit)
                        : Fmt.speed(state.speedKmh, state.unit),
                    style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary),
                  ),
                  if (isRunning)
                    Text(
                      Fmt.speed(state.speedKmh, state.unit),
                      style: const TextStyle(
                          fontSize: 13, color: AppColors.textSecondary),
                    ),
                ],
              ),
              Slider(
                min: _minSpeed(state.sport),
                max: _maxSpeed(state.sport),
                value: state.speedKmh.clamp(
                    _minSpeed(state.sport), _maxSpeed(state.sport)),
                divisions: ((_maxSpeed(state.sport) - _minSpeed(state.sport)) * 2).round(),
                onChanged: (v) => bloc.add(SetSpeed(v)),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(_speedLabel(state.sport, 'slow'),
                      style: const TextStyle(
                          fontSize: 10, color: AppColors.textMuted)),
                  Text(_speedLabel(state.sport, 'fast'),
                      style: const TextStyle(
                          fontSize: 10, color: AppColors.textMuted)),
                ],
              ),
              const Gap(28),

              // ── Start time ────────────────────────────────────────────────
              const _SectionHeader(
                  icon: Icons.access_time_rounded, label: 'Daily start time'),
              const Gap(12),
              GestureDetector(
                onTap: () async {
                  final t = await showTimePicker(
                    context: ctx,
                    initialTime: TimeOfDay.fromDateTime(state.startTime),
                    builder: (c, child) => Theme(
                      data: ThemeData.dark(useMaterial3: true).copyWith(
                        colorScheme:
                            const ColorScheme.dark(primary: AppColors.primary),
                      ),
                      child: child!,
                    ),
                  );
                  if (t != null && ctx.mounted) {
                    final st = state.startTime;
                    bloc.add(SetStartTime(DateTime(
                        st.year, st.month, st.day, t.hour, t.minute)));
                  }
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 16, vertical: 14),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(10),
                    border:
                        Border.all(color: AppColors.border, width: 0.5),
                  ),
                  child: Row(children: [
                    const Icon(Icons.access_time_rounded,
                        size: 17, color: AppColors.primary),
                    const Gap(12),
                    Text(
                      Fmt.hhmm(state.startTime),
                      style: const TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.w600,
                          color: AppColors.textPrimary),
                    ),
                    const Spacer(),
                    const Icon(Icons.chevron_right_rounded,
                        size: 17, color: AppColors.textMuted),
                  ]),
                ),
              ),
              const Gap(28),

              // ── Breaks ────────────────────────────────────────────────────
              const _SectionHeader(
                  icon: Icons.coffee_rounded, label: 'Activity breaks'),
              const Gap(4),
              const Text('Tap to include in your daily schedule',
                  style: TextStyle(
                      fontSize: 11, color: AppColors.textSecondary)),
              const Gap(12),
              BreakSelector(
                selected: state.selectedBreaks,
                onToggle: (t) => bloc.add(ToggleBreak(t)),
              ),
              const Gap(32),

              if (state.error != null) ...[
                TFErrorBar(message: state.error!),
                const Gap(12),
              ],

              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: state.loading
                      ? null
                      : () => bloc.add(BuildPlanEvent()),
                  icon: const Icon(Icons.check_rounded, size: 16),
                  label: const Text('Create Plan'),
                  style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.all(15)),
                ),
              ),
              const Gap(24),
            ],
          ),
        );
      },
    );
  }

  double _minSpeed(SportType s) {
    switch (s) {
      case SportType.running:  return 4.0;
      case SportType.cycling:
      case SportType.skating:  return 8.0;
      default:                 return 2.0;
    }
  }

  double _maxSpeed(SportType s) {
    switch (s) {
      case SportType.running:  return 22.0;
      case SportType.cycling:  return 45.0;
      case SportType.skating:  return 30.0;
      default:                 return 8.0;
    }
  }

  String _speedLabel(SportType s, String end) {
    if (end == 'slow') {
      return '${_minSpeed(s).toStringAsFixed(0)} km/h';
    }
    return '${_maxSpeed(s).toStringAsFixed(0)} km/h';
  }
}

// ─── Step 6: Review ───────────────────────────────────────────────────────────

class _ReviewPage extends StatelessWidget {
  const _ReviewPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final plan = state.plan!;
        final route = plan.route;
        final unit = state.unit;

        return Column(children: [
          // Compact elevation + stats header
          Container(
            color: AppColors.surface,
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 14),
            child: Column(children: [
              // Summary stats
              Row(children: [
                Expanded(
                  child: TFStatCard(
                    label: 'Total distance',
                    value: Fmt.distance(route.totalDistanceKm, unit),
                  ),
                ),
                const Gap(8),
                Expanded(
                  child: TFStatCard(
                    label: 'Total time',
                    value: Fmt.duration(plan.estimatedTotal),
                  ),
                ),
                const Gap(8),
                Expanded(
                  child: TFStatCard(
                    label: 'Avg/day',
                    value: Fmt.distance(
                        route.totalDistanceKm / plan.totalDays, unit),
                  ),
                ),
              ]),
              const Gap(14),
              ElevationChart(
                  points: route.elevation, unit: unit, height: 90),
            ]),
          ),

          // Daily segments list
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                ...plan.segments.map((seg) => _DayCard(
                    segment: seg,
                    unit: unit,
                    sport: state.sport)),
              ],
            ),
          ),

          // Footer CTA
          Container(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
            color: AppColors.surface,
            child: SafeArea(
              top: false,
              child: SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: () => bloc.add(GoToStep(AppStep.export)),
                  icon: const Icon(Icons.download_rounded, size: 16),
                  label: const Text('Export Route'),
                  style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.all(14)),
                ),
              ),
            ),
          ),
        ]);
      },
    );
  }
}

// ─── Step 7: Export ───────────────────────────────────────────────────────────

class _ExportPage extends StatelessWidget {
  const _ExportPage();

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        return SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Export route', style: Theme.of(ctx).textTheme.displayMedium),
              const Gap(4),
              const Text('Choose a format to share or use on your device.',
                  style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
              const Gap(24),

              GridView.count(
                crossAxisCount: 2,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisSpacing: 12,
                mainAxisSpacing: 12,
                childAspectRatio: 1.15,
                children: ExportFormat.values.map((fmt) {
                  return GestureDetector(
                    onTap: state.loading
                        ? null
                        : () => bloc.add(ExportEvent(fmt)),
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: AppColors.card,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                            color: AppColors.border, width: 0.5),
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(fmt.emoji,
                              style: const TextStyle(fontSize: 30)),
                          const Gap(8),
                          Text(fmt.label,
                              style: const TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.textPrimary)),
                          const Gap(3),
                          Text(fmt.description,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                  fontSize: 10,
                                  color: AppColors.textSecondary)),
                        ],
                      ),
                    ),
                  );
                }).toList(),
              ),

              if (state.exportedPath != null) ...[
                const Gap(20),
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.success.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(
                        color: AppColors.success.withOpacity(0.3)),
                  ),
                  child: Row(children: [
                    const Icon(Icons.check_circle_rounded,
                        color: AppColors.success, size: 18),
                    const Gap(10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Exported!',
                              style: TextStyle(
                                  color: AppColors.success,
                                  fontWeight: FontWeight.w600,
                                  fontSize: 13)),
                          const Gap(2),
                          Text(state.exportedPath!,
                              style: const TextStyle(
                                  color: AppColors.textSecondary,
                                  fontSize: 10),
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis),
                        ],
                      ),
                    ),
                    const Gap(8),
                    IconButton(
                      icon: const Icon(Icons.share_rounded,
                          size: 18, color: AppColors.primary),
                      onPressed: () => Share.shareXFiles(
                          [XFile(state.exportedPath!)]),
                    ),
                  ]),
                ),
              ],

              if (state.error != null) ...[
                const Gap(12),
                TFErrorBar(message: state.error!),
              ],
            ],
          ),
        );
      },
    );
  }
}

// ─── Shared small widgets ─────────────────────────────────────────────────────

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  const _SectionHeader({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Icon(icon, size: 15, color: AppColors.primary),
      const Gap(7),
      Text(label,
          style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary)),
    ]);
  }
}

class _SelectedBadge extends StatelessWidget {
  final String label;
  const _SelectedBadge({required this.label});

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Container(
        width: 6, height: 6,
        decoration: const BoxDecoration(
            color: AppColors.primary, shape: BoxShape.circle),
      ),
      const Gap(6),
      Flexible(
        child: Text(label,
            style: const TextStyle(
                fontSize: 11, color: AppColors.textSecondary),
            maxLines: 1,
            overflow: TextOverflow.ellipsis),
      ),
    ]);
  }
}

class _OptionCard extends StatelessWidget {
  final String emoji;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _OptionCard({
    required this.emoji,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.border, width: 0.5),
        ),
        child: Row(children: [
          Text(emoji, style: const TextStyle(fontSize: 26)),
          const Gap(14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title,
                    style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary)),
                const Gap(2),
                Text(subtitle,
                    style: const TextStyle(
                        fontSize: 12, color: AppColors.textSecondary)),
              ],
            ),
          ),
          const Icon(Icons.chevron_right_rounded,
              size: 18, color: AppColors.textMuted),
        ]),
      ),
    );
  }
}

class _ViaRow extends StatelessWidget {
  final int index;
  final Location location;
  final VoidCallback onRemove;

  const _ViaRow(
      {required this.index,
      required this.location,
      required this.onRemove});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.border, width: 0.5),
      ),
      child: Row(children: [
        Container(
          width: 22,
          height: 22,
          decoration: BoxDecoration(
            color: AppColors.primaryDim,
            borderRadius: BorderRadius.circular(5),
          ),
          child: Center(
            child: Text('${index + 1}',
                style: const TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                    color: AppColors.primary)),
          ),
        ),
        const Gap(10),
        Expanded(
          child: Text(location.name ?? location.toString(),
              style: const TextStyle(
                  fontSize: 13, color: AppColors.textPrimary)),
        ),
        GestureDetector(
          onTap: onRemove,
          child: const Icon(Icons.close_rounded,
              size: 16, color: AppColors.textMuted),
        ),
      ]),
    );
  }
}

class _WaypointRow extends StatelessWidget {
  final Waypoint waypoint;
  const _WaypointRow({required this.waypoint});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.border, width: 0.5),
        ),
        child: Row(children: [
          const Icon(Icons.place_rounded,
              size: 15, color: AppColors.primary),
          const Gap(10),
          Expanded(
            child: Text(waypoint.label,
                style: const TextStyle(
                    fontSize: 13, color: AppColors.textPrimary)),
          ),
          if (waypoint.distanceFromStartKm != null)
            Text(
              '${waypoint.distanceFromStartKm!.toStringAsFixed(0)} km',
              style: const TextStyle(
                  fontSize: 11, color: AppColors.textSecondary),
            ),
        ]),
      ),
    );
  }
}

class _SportBadge extends StatelessWidget {
  final SportType sport;
  const _SportBadge({required this.sport});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: AppColors.bg.withOpacity(0.9),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Text(sport.emoji, style: const TextStyle(fontSize: 13)),
        const Gap(5),
        Text(sport.label,
            style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary)),
      ]),
    );
  }
}

class _DayCard extends StatelessWidget {
  final DailySegment segment;
  final DistanceUnit unit;
  final SportType sport;

  const _DayCard(
      {required this.segment, required this.unit, required this.sport});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border, width: 0.5),
      ),
      child: Column(children: [
        // Header
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius:
                const BorderRadius.vertical(top: Radius.circular(11)),
          ),
          child: Row(children: [
            Text('Day ${segment.day}',
                style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary)),
            const Spacer(),
            Text(Fmt.distance(segment.distanceKm, unit),
                style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    color: AppColors.primary)),
            const Gap(12),
            Text(
              '${Fmt.hhmm(segment.departureTime)} → ${Fmt.hhmm(segment.estimatedArrival)}',
              style: const TextStyle(
                  fontSize: 11, color: AppColors.textSecondary),
            ),
          ]),
        ),

        if (segment.breaks.isNotEmpty)
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              children: segment.breaks.map((b) => Padding(
                    padding: const EdgeInsets.symmetric(vertical: 3),
                    child: Row(children: [
                      Text(b.type.emoji,
                          style: const TextStyle(fontSize: 14)),
                      const Gap(8),
                      Text(b.type.label,
                          style: const TextStyle(
                              fontSize: 12, color: AppColors.textPrimary)),
                      const Spacer(),
                      Text(Fmt.hhmm(b.scheduledAt),
                          style: const TextStyle(
                              fontSize: 11, color: AppColors.textSecondary)),
                      const Gap(8),
                      Text(Fmt.duration(b.duration),
                          style: const TextStyle(
                              fontSize: 11, color: AppColors.textMuted)),
                    ]),
                  )).toList(),
            ),
          )
        else
          const Padding(
            padding: EdgeInsets.all(12),
            child: Text('No breaks scheduled this day',
                style: TextStyle(
                    fontSize: 11, color: AppColors.textMuted)),
          ),
      ]),
    );
  }
}

class _CounterBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback? onTap;

  const _CounterBtn({required this.icon, this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: onTap != null ? AppColors.border : AppColors.textMuted,
            width: 0.5,
          ),
        ),
        child: Icon(icon,
            size: 20,
            color: onTap != null
                ? AppColors.textPrimary
                : AppColors.textMuted),
      ),
    );
  }
}
