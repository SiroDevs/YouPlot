import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../domain/entities/location.dart';
import '../../theme/app_colors.dart';
import '../../pages/home/components/full_page_search.dart';

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
            pageBuilder: (_, _, _) =>
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
