import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../theme/app_colors.dart';
import 'gps_button.dart';

class SearchHeader extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode focusNode;
  final String hint;
  final Brightness brightness;
  final bool showGps;
  final VoidCallback onBack;
  final VoidCallback onClear;

  const SearchHeader({
    super.key,
    required this.controller,
    required this.focusNode,
    required this.hint,
    required this.brightness,
    required this.showGps,
    required this.onBack,
    required this.onClear,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = brightness == Brightness.dark;
    final bgColor = isDark ? const Color(0xFF111111) : Colors.white;

    return SafeArea(
      child: Container(
        color: bgColor,
        padding: const EdgeInsets.fromLTRB(12, 12, 12, 8),
        child: Row(
          children: [
            GestureDetector(
              onTap: onBack,
              child: Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: AppColors.surface(brightness),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  Icons.arrow_back_rounded,
                  size: 20,
                  color: AppColors.textPrimary(brightness),
                ),
              ),
            ),
            const Gap(10),
            _SearchTextField(
              controller: controller,
              focusNode: focusNode,
              hint: hint,
              brightness: brightness,
              onClear: onClear,
            ),
            if (showGps) ...[const Gap(10), const GpsButton()],
          ],
        ),
      ),
    );
  }
}

class _SearchTextField extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode focusNode;
  final String hint;
  final Brightness brightness;
  final VoidCallback onClear;

  const _SearchTextField({
    required this.controller,
    required this.focusNode,
    required this.hint,
    required this.brightness,
    required this.onClear,
  });

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      focusNode: focusNode,
      style: TextStyle(fontSize: 15, color: AppColors.textPrimary(brightness)),
      textInputAction: TextInputAction.search,
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: TextStyle(
          color: AppColors.textMuted(brightness),
          fontSize: 15,
        ),
        prefixIcon: Icon(
          Icons.search_rounded,
          size: 20,
          color: AppColors.textMuted(brightness),
        ),
        suffixIcon: controller.text.isNotEmpty
            ? IconButton(
                icon: Icon(
                  Icons.close_rounded,
                  size: 18,
                  color: AppColors.textMuted(brightness),
                ),
                onPressed: onClear,
              )
            : null,
        border: InputBorder.none,
        enabledBorder: InputBorder.none,
        focusedBorder: InputBorder.none,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 12,
        ),
      ),
    ).expanded();
  }
}
