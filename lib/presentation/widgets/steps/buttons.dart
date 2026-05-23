import 'package:flutter/material.dart';

import '../../theme/app_colors.dart';

class GlassBtn extends StatelessWidget {
  final Brightness brightness;
  final VoidCallback onTap;
  final Widget child;

  const GlassBtn({super.key, 
    required this.brightness,
    required this.onTap,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = brightness == Brightness.dark;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isDark
              ? Colors.black.withValues(alpha: 0.75)
              : Colors.white.withValues(alpha: 0.92),
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.2),
              blurRadius: 12,
              offset: const Offset(0, 2),
            )
          ],
        ),
        child: child,
      ),
    );
  }
}

class CounterBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback? onTap;
  final Brightness brightness;

  const CounterBtn({super.key, required this.icon, required this.brightness, this.onTap});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.surface(b),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: onTap != null ? AppColors.border(b) : AppColors.textMuted(b),
            width: 0.5,
          ),
        ),
        child: Icon(
          icon,
          size: 20,
          color: onTap != null
              ? AppColors.textPrimary(b)
              : AppColors.textMuted(b),
        ),
      ),
    );
  }
}
