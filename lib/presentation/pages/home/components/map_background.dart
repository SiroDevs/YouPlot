import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../../../../domain/entities/entities.dart';
import '../../../theme/app_colors.dart';
import 'full_page_search.dart';

class MapBackground extends StatelessWidget {
  final Brightness brightness;
  const MapBackground({super.key, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final isDark = brightness == Brightness.dark;
    return Positioned.fill(
      child: Container(
        color: isDark ? const Color(0xFF0D1B2A) : const Color(0xFFE8E2D8),
        child: Stack(
          children: [
            CustomPaint(
              painter: MapGridPainter(isDark: isDark),
              size: Size.infinite,
            ),
          ],
        ),
      ),
    );
  }
}

class MapGridPainter extends CustomPainter {
  final bool isDark;
  const MapGridPainter({required this.isDark});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = (isDark ? Colors.white : Colors.black).withValues(alpha: 0.04)
      ..strokeWidth = 1;

    const step = 60.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }

    // Subtle road-like lines
    final roadPaint = Paint()
      ..color = (isDark ? Colors.white : Colors.brown).withValues(alpha: 0.07)
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;

    canvas.drawLine(
      Offset(0, size.height * 0.4),
      Offset(size.width, size.height * 0.55),
      roadPaint,
    );
    canvas.drawLine(
      Offset(size.width * 0.3, 0),
      Offset(size.width * 0.45, size.height),
      roadPaint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter old) => false;
}

class MapSearchField extends StatelessWidget {
  final String hint;
  final Location? value;
  final bool showGps;
  final ValueChanged<Location> onSelected;
  final Brightness brightness;

  const MapSearchField({
    super.key,
    required this.hint,
    required this.onSelected,
    required this.brightness,
    this.value,
    this.showGps = false,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final isDark = b == Brightness.dark;

    return GestureDetector(
      onTap: () async {
        final loc = await Navigator.push<Location>(
          context,
          PageRouteBuilder(
            pageBuilder: (_, __, ___) =>
                FullPageSearch(hint: hint, showGps: showGps, brightness: b),
            transitionsBuilder: (_, anim, __, child) => FadeTransition(
              opacity: anim,
              child: SlideTransition(
                position: Tween(
                  begin: const Offset(0, 0.05),
                  end: Offset.zero,
                ).animate(CurvedAnimation(parent: anim, curve: Curves.easeOut)),
                child: child,
              ),
            ),
            transitionDuration: const Duration(milliseconds: 220),
          ),
        );
        if (loc != null) onSelected(loc);
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 13),
        decoration: BoxDecoration(
          color: isDark ? AppColors.surfaceDark : Colors.white,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.border(b), width: 0.5),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.08),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Row(
          children: [
            Icon(Icons.search_rounded, size: 17, color: AppColors.textMuted(b)),
            const Gap(10),
            Expanded(
              child: Text(
                value?.name ?? hint,
                style: TextStyle(
                  fontSize: 14,
                  color: value != null
                      ? AppColors.textPrimary(b)
                      : AppColors.textMuted(b),
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            if (value != null)
              Icon(
                Icons.check_circle_rounded,
                size: 16,
                color: AppColors.primary,
              ),
          ],
        ),
      ),
    );
  }
}
