import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../theme/app_colors.dart';

class IconTextButton extends StatelessWidget {
  final String label;
  final IconData? icon;
  final VoidCallback? onPressed;
  final bool loading;
  final Brightness brightness;
  final Color? color;

  const IconTextButton({
    super.key,
    required this.label,
    required this.brightness,
    this.icon,
    this.onPressed,
    this.loading = false,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
      decoration: BoxDecoration(
        color: AppColors.surface(b),
        border: Border(top: BorderSide(color: AppColors.border(b), width: 0.5)),
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: loading ? null : onPressed,
            style: ElevatedButton.styleFrom(
              backgroundColor: color ?? AppColors.primary,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(vertical: 15),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              elevation: 0,
            ),
            child: loading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (icon != null) ...[
                        Icon(icon, size: 18),
                        const Gap(8),
                      ],
                      Text(
                        label,
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
          ),
        ),
      ),
    );
  }
}
