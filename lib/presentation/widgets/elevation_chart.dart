import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/entities/entities.dart';
import '../theme/app_colors.dart';

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
    final b = Theme.of(context).brightness;
    if (points.isEmpty) {
      return SizedBox(
        height: height,
        child: Center(
          child: Text('No elevation data',
              style: TextStyle(
                  color: AppColors.textMuted(b), fontSize: 12)),
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
              FlLine(color: AppColors.border(b), strokeWidth: 0.5),
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
                style: TextStyle(
                    fontSize: 9, color: AppColors.textMuted(b)),
              ),
            ),
          ),
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              interval: spots.last.x / 4,
              getTitlesWidget: (v, _) => Text(
                '${v.toStringAsFixed(0)}${unit.symbol}',
                style: TextStyle(
                    fontSize: 9, color: AppColors.textMuted(b)),
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
                  AppColors.primary.withValues(alpha: 0.28),
                  AppColors.primary.withValues(alpha: 0.0),
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
