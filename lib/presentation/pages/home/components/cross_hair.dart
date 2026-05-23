import 'package:flutter/material.dart';

import '../../../theme/app_colors.dart';

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
    final p = Paint()
      ..color = AppColors.primary
      ..strokeWidth = 2
      ..style = PaintingStyle.stroke;
    final cx = size.width / 2;
    final cy = size.height / 2;
    canvas.drawCircle(Offset(cx, cy), 6, p);
    canvas.drawLine(Offset(cx, 0), Offset(cx, cy - 9), p);
    canvas.drawLine(Offset(cx, cy + 9), Offset(cx, size.height), p);
    canvas.drawLine(Offset(0, cy), Offset(cx - 9, cy), p);
    canvas.drawLine(Offset(cx + 9, cy), Offset(size.width, cy), p);
  }
 
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
