import 'package:flutter/material.dart';
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';

import '../../../theme/app_colors.dart';

class GlassCircle extends StatelessWidget {
  final Widget child;
  const GlassCircle({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      decoration: BoxDecoration(
        color: isDark
            ? Colors.black.withValues(alpha: 0.75)
            : Colors.white.withValues(alpha: 0.92),
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.15), blurRadius: 8),
        ],
      ),
      child: child,
    );
  }
}

class Crosshair extends StatelessWidget {
  const Crosshair({super.key});

  @override
  Widget build(BuildContext context) {
    return IgnorePointer(
      child: SizedBox(
        width: 32,
        height: 32,
        child: CustomPaint(painter: _CrosshairPainter()),
      ),
    );
  }
}

class _CrosshairPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = AppColors.primary
      ..strokeWidth = 2
      ..style = PaintingStyle.stroke;
    final cx = size.width / 2;
    final cy = size.height / 2;
    canvas.drawCircle(Offset(cx, cy), 6, paint);
    canvas.drawLine(Offset(cx, 0), Offset(cx, cy - 9), paint);
    canvas.drawLine(Offset(cx, cy + 9), Offset(cx, size.height), paint);
    canvas.drawLine(Offset(0, cy), Offset(cx - 9, cy), paint);
    canvas.drawLine(Offset(cx + 9, cy), Offset(size.width, cy), paint);
  }

  @override
  bool shouldRepaint(_) => false;
}
