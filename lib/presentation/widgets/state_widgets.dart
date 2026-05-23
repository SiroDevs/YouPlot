import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class StatCard extends StatelessWidget {
  final String label;
  final String value;
  final String? sub;
  final IconData? icon;
  final Color? color;

  const StatCard({
    super.key,
    required this.label,
    required this.value,
    this.sub,
    this.icon,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    final c = color ?? AppColors.primary;
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.card(b),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.border(b), width: 0.5),
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
                  style: TextStyle(
                      fontSize: 10,
                      color: AppColors.textSecondary(b))),
            ),
          ]),
          const SizedBox(height: 5),
          Text(value,
              style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary(b))),
          if (sub != null)
            Text(sub!,
                style: TextStyle(
                    fontSize: 10, color: AppColors.textSecondary(b))),
        ],
      ),
    );
  }
}

class ErrorBar extends StatelessWidget {
  final String message;
  final VoidCallback? onDismiss;

  const ErrorBar({super.key, required this.message, this.onDismiss});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 0, vertical: 4),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.danger.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.danger.withValues(alpha: 0.3)),
      ),
      child: Row(children: [
        const Icon(Icons.error_outline_rounded,
            color: AppColors.danger, size: 16),
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

class LoadingOverlay extends StatelessWidget {
  final String message;
  const LoadingOverlay({super.key, this.message = 'Loading…'});

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Container(
      color: AppColors.bg(b).withValues(alpha: 0.85),
      child: Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const CircularProgressIndicator(
              color: AppColors.primary, strokeWidth: 2),
          const SizedBox(height: 16),
          Text(message,
              style: TextStyle(
                  color: AppColors.textSecondary(b), fontSize: 14)),
        ]),
      ),
    );
  }
}
