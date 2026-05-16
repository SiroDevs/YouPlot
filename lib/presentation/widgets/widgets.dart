import 'dart:async';

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/entities/entities.dart';
import '../bloc/location_search/location_search_bloc.dart';
import '../theme/app_theme.dart';

class TFStatCard extends StatelessWidget {
  final String label;
  final String value;
  final String? sub;
  final IconData? icon;
  final Color? color;

  const TFStatCard({
    super.key,
    required this.label,
    required this.value,
    this.sub,
    this.icon,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final c = color ?? AppColors.primary;
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border, width: 0.5),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(children: [
            if (icon != null) ...[
              Icon(icon, size: 12, color: c),
              const SizedBox(width: 5),
            ],
            Flexible(
              child: Text(label,
                  style: const TextStyle(fontSize: 10, color: AppColors.textSecondary)),
            ),
          ]),
          const SizedBox(height: 5),
          Text(value,
              style: const TextStyle(
                  fontSize: 18, fontWeight: FontWeight.w700, color: AppColors.textPrimary)),
          if (sub != null)
            Text(sub!,
                style: const TextStyle(fontSize: 10, color: AppColors.textSecondary)),
        ],
      ),
    );
  }
}

// lib/presentation/widgets/tf_error_bar.dart

class TFErrorBar extends StatelessWidget {
  final String message;
  final VoidCallback? onDismiss;

  const TFErrorBar({super.key, required this.message, this.onDismiss});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.danger.withOpacity(0.12),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.danger.withOpacity(0.3)),
      ),
      child: Row(children: [
        const Icon(Icons.error_outline_rounded, color: AppColors.danger, size: 16),
        const SizedBox(width: 10),
        Expanded(
          child: Text(message,
              style: const TextStyle(color: AppColors.danger, fontSize: 13)),
        ),
        if (onDismiss != null)
          GestureDetector(
            onTap: onDismiss,
            child: const Icon(Icons.close_rounded,
                color: AppColors.danger, size: 16),
          ),
      ]),
    );
  }
}

// lib/presentation/widgets/tf_loading_overlay.dart

class TFLoadingOverlay extends StatelessWidget {
  final String message;
  const TFLoadingOverlay({super.key, this.message = 'Loading…'});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AppColors.bg.withOpacity(0.85),
      child: Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const CircularProgressIndicator(
            color: AppColors.primary,
            strokeWidth: 2,
          ),
          const SizedBox(height: 16),
          Text(message,
              style: const TextStyle(
                  color: AppColors.textSecondary, fontSize: 14)),
        ]),
      ),
    );
  }
}

class LocationField extends StatefulWidget {
  final String hint;
  final Location? value;
  final bool showGps;
  final ValueChanged<Location> onSelected;

  const LocationField({
    super.key,
    required this.hint,
    required this.onSelected,
    this.value,
    this.showGps = false,
  });

  @override
  State<LocationField> createState() => _LocationFieldState();
}

class _LocationFieldState extends State<LocationField> {
  final _ctrl = TextEditingController();
  final _focus = FocusNode();
  Timer? _debounce;
  bool _showDropdown = false;

  @override
  void initState() {
    super.initState();
    if (widget.value?.name != null) _ctrl.text = widget.value!.name!;
    _focus.addListener(() {
      if (!_focus.hasFocus) setState(() => _showDropdown = false);
    });
  }

  @override
  void didUpdateWidget(LocationField old) {
    super.didUpdateWidget(old);
    if (widget.value?.name != null && widget.value != old.value) {
      _ctrl.text = widget.value!.name!;
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    _focus.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onChanged(String q) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 350), () {
      context.read<LocationSearchBloc>().add(QueryChanged(q));
      setState(() => _showDropdown = q.length >= 2);
    });
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<LocationSearchBloc, LocationSearchState>(
      listener: (ctx, s) {
        if (s.currentLocation != null && widget.showGps) {
          widget.onSelected(s.currentLocation!);
          _ctrl.text = s.currentLocation!.name ?? 'Current location';
          setState(() => _showDropdown = false);
          ctx.read<LocationSearchBloc>().add(ClearSearch());
        }
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: _ctrl,
            focusNode: _focus,
            onChanged: _onChanged,
            style: const TextStyle(color: AppColors.textPrimary, fontSize: 14),
            decoration: InputDecoration(
              hintText: widget.hint,
              prefixIcon: const Icon(Icons.search_rounded, size: 17),
              suffixIcon: widget.showGps
                  ? BlocBuilder<LocationSearchBloc, LocationSearchState>(
                      builder: (_, s) => s.locating
                          ? const Padding(
                              padding: EdgeInsets.all(12),
                              child: SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(
                                    strokeWidth: 2, color: AppColors.primary),
                              ),
                            )
                          : IconButton(
                              icon: const Icon(Icons.my_location_rounded, size: 17),
                              onPressed: () =>
                                  context.read<LocationSearchBloc>().add(LocateMe()),
                            ),
                    )
                  : null,
            ),
          ),
          if (_showDropdown)
            BlocBuilder<LocationSearchBloc, LocationSearchState>(
              builder: (_, s) {
                if (s.loading) {
                  return const Padding(
                    padding: EdgeInsets.all(12),
                    child: Center(
                        child: CircularProgressIndicator(
                            strokeWidth: 2, color: AppColors.primary)),
                  );
                }
                if (s.results.isEmpty) return const SizedBox.shrink();
                return Container(
                  margin: const EdgeInsets.only(top: 4),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: AppColors.border, width: 0.5),
                    boxShadow: [
                      BoxShadow(
                          color: Colors.black.withOpacity(0.4),
                          blurRadius: 12,
                          offset: const Offset(0, 4))
                    ],
                  ),
                  child: ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: s.results.length.clamp(0, 6),
                    separatorBuilder: (_, __) =>
                        const Divider(height: 0, color: AppColors.border),
                    itemBuilder: (_, i) {
                      final loc = s.results[i];
                      return ListTile(
                        dense: true,
                        contentPadding:
                            const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
                        leading: const Icon(Icons.place_outlined,
                            size: 16, color: AppColors.textSecondary),
                        title: Text(loc.name ?? '',
                            style: const TextStyle(
                                fontSize: 13, color: AppColors.textPrimary)),
                        subtitle: loc.address != null
                            ? Text(loc.address!,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                    fontSize: 11, color: AppColors.textSecondary))
                            : null,
                        onTap: () {
                          widget.onSelected(loc);
                          _ctrl.text = loc.name ?? loc.address ?? '';
                          _focus.unfocus();
                          setState(() => _showDropdown = false);
                          context.read<LocationSearchBloc>().add(ClearSearch());
                        },
                      );
                    },
                  ),
                );
              },
            ),
        ],
      ),
    );
  }
}

class ElevationChart extends StatelessWidget {
  final List<ElevationPoint> points;
  final DistanceUnit unit;
  final double height;

  const ElevationChart({
    super.key,
    required this.points,
    required this.unit,
    this.height = 130,
  });

  @override
  Widget build(BuildContext context) {
    if (points.isEmpty) {
      return SizedBox(
        height: height,
        child: const Center(
          child: Text('No elevation data',
              style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
        ),
      );
    }

    final spots = points.map((p) {
      final dist =
          unit == DistanceUnit.miles ? p.distanceKm * 0.621371 : p.distanceKm;
      final elev =
          unit == DistanceUnit.miles ? p.elevationM * 3.28084 : p.elevationM;
      return FlSpot(dist, elev);
    }).toList();

    final minY = spots.map((s) => s.y).reduce((a, b) => a < b ? a : b);
    final maxY = spots.map((s) => s.y).reduce((a, b) => a > b ? a : b);
    final pad = ((maxY - minY) * 0.2).clamp(10.0, double.infinity);

    return SizedBox(
      height: height,
      child: LineChart(LineChartData(
        gridData: FlGridData(
          show: true,
          drawVerticalLine: false,
          getDrawingHorizontalLine: (_) =>
              const FlLine(color: AppColors.border, strokeWidth: 0.5),
        ),
        borderData: FlBorderData(show: false),
        minY: minY - pad,
        maxY: maxY + pad,
        titlesData: FlTitlesData(
          leftTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 42,
              getTitlesWidget: (v, _) => Text(
                '${v.toInt()}${unit == DistanceUnit.miles ? 'ft' : 'm'}',
                style: const TextStyle(fontSize: 9, color: AppColors.textMuted),
              ),
            ),
          ),
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              interval: spots.last.x / 4,
              getTitlesWidget: (v, _) => Text(
                '${v.toStringAsFixed(0)}${unit.symbol}',
                style: const TextStyle(fontSize: 9, color: AppColors.textMuted),
              ),
            ),
          ),
          topTitles:
              const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          rightTitles:
              const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        ),
        lineBarsData: [
          LineChartBarData(
            spots: spots,
            isCurved: true,
            curveSmoothness: 0.3,
            color: AppColors.primary,
            barWidth: 2,
            dotData: const FlDotData(show: false),
            belowBarData: BarAreaData(
              show: true,
              gradient: LinearGradient(
                colors: [
                  AppColors.primary.withOpacity(0.28),
                  AppColors.primary.withOpacity(0.0),
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),
        ],
      )),
    );
  }
}

class SportChip extends StatelessWidget {
  final SportType sport;
  final bool selected;
  final VoidCallback onTap;

  const SportChip({
    super.key,
    required this.sport,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final c = AppColors.sport(sport);
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: selected ? c.withOpacity(0.14) : AppColors.surface,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: selected ? c : AppColors.border,
            width: selected ? 1.2 : 0.5,
          ),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Text(sport.emoji, style: const TextStyle(fontSize: 18)),
          const SizedBox(width: 8),
          Text(
            sport.label,
            style: TextStyle(
              fontSize: 13,
              fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
              color: selected ? c : AppColors.textSecondary,
            ),
          ),
        ]),
      ),
    );
  }
}

class UnitToggle extends StatelessWidget {
  final DistanceUnit value;
  final ValueChanged<DistanceUnit> onChanged;

  const UnitToggle({super.key, required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 34,
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppColors.border, width: 0.5),
      ),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        for (final u in DistanceUnit.values)
          GestureDetector(
            onTap: () => onChanged(u),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 150),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
              decoration: BoxDecoration(
                color: u == value ? AppColors.primary : Colors.transparent,
                borderRadius: BorderRadius.circular(7),
              ),
              child: Text(
                u.symbol.toUpperCase(),
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                  color: u == value ? Colors.black : AppColors.textSecondary,
                ),
              ),
            ),
          ),
      ]),
    );
  }
}

class StepBar extends StatelessWidget {
  final int current; // 0-based
  final int total;

  const StepBar({super.key, required this.current, required this.total});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: List.generate(total * 2 - 1, (i) {
        if (i.isOdd) {
          final idx = i ~/ 2;
          return Expanded(
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 300),
              height: 1,
              color: idx < current ? AppColors.primary : AppColors.border,
            ),
          );
        }
        final idx = i ~/ 2;
        final done = idx < current;
        final active = idx == current;
        return AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          width: 24,
          height: 24,
          decoration: BoxDecoration(
            color: done
                ? AppColors.primary
                : active
                    ? AppColors.primaryDim
                    : AppColors.surface,
            border: Border.all(
              color: (done || active) ? AppColors.primary : AppColors.border,
              width: active ? 1.5 : 0.5,
            ),
            shape: BoxShape.circle,
          ),
          child: Center(
            child: done
                ? const Icon(Icons.check_rounded,
                    size: 12, color: Colors.black)
                : Text(
                    '${idx + 1}',
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w600,
                      color: active ? AppColors.primary : AppColors.textMuted,
                    ),
                  ),
          ),
        );
      }),
    );
  }
}

class BreakSelector extends StatelessWidget {
  final Set<BreakType> selected;
  final ValueChanged<BreakType> onToggle;

  const BreakSelector(
      {super.key, required this.selected, required this.onToggle});

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: BreakType.values.map((t) {
        final on = selected.contains(t);
        return GestureDetector(
          onTap: () => onToggle(t),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            padding:
                const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: on ? AppColors.primaryDim : AppColors.surface,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: on ? AppColors.primary : AppColors.border,
                width: on ? 1 : 0.5,
              ),
            ),
            child: Row(mainAxisSize: MainAxisSize.min, children: [
              Text(t.emoji, style: const TextStyle(fontSize: 15)),
              const SizedBox(width: 6),
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(t.label,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight:
                          on ? FontWeight.w600 : FontWeight.w400,
                      color: on
                          ? AppColors.primary
                          : AppColors.textSecondary,
                    )),
                Text('${t.defaultMinutes} min',
                    style: const TextStyle(
                        fontSize: 10, color: AppColors.textMuted)),
              ]),
            ]),
          ),
        );
      }).toList(),
    );
  }
}
