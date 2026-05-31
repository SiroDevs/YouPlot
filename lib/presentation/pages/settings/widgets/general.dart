import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../theme/app_colors.dart';

class SectionHeader extends StatelessWidget {
  final String label;
  final Brightness brightness;
  const SectionHeader({
    super.key,
    required this.label,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Text(
      label.toUpperCase(),
      style: TextStyle(
        fontSize: 11,
        fontWeight: FontWeight.w700,
        letterSpacing: 1.2,
        color: AppColors.primary,
      ),
    );
  }
}

class SettingsCard extends StatelessWidget {
  final List<Widget> children;
  final Brightness brightness;
  const SettingsCard({
    super.key,
    required this.children,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.card(brightness),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border(brightness), width: 0.5),
      ),
      child: Column(
        children: List.generate(children.length * 2 - 1, (i) {
          if (i.isOdd) {
            return Divider(
              height: 0,
              thickness: 0.5,
              color: AppColors.border(brightness),
            );
          }
          return children[i ~/ 2];
        }),
      ),
    );
  }
}

class ModeBtn extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool active;
  final Brightness brightness;
  final VoidCallback onTap;

  const ModeBtn({
    super.key,
    required this.icon,
    required this.label,
    required this.active,
    required this.brightness,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: active ? AppColors.primary : Colors.transparent,
          borderRadius: BorderRadius.circular(7),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 14,
              color: active
                  ? Colors.white
                  : AppColors.textSecondary(brightness),
            ),
            const Gap(4),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: active
                    ? Colors.white
                    : AppColors.textSecondary(brightness),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class DropdownTile<T> extends StatelessWidget {
  final Brightness brightness;
  final IconData icon;
  final String label;
  final T value;
  final List<T> items;
  final String Function(T) itemLabel;
  final ValueChanged<T?> onChanged;

  const DropdownTile({
    super.key,
    required this.brightness,
    required this.icon,
    required this.label,
    required this.value,
    required this.items,
    required this.itemLabel,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          Icon(icon, size: 20, color: AppColors.primary),
          const Gap(14),
          Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: AppColors.textPrimary(brightness),
            ),
          ).expanded(),
          const Gap(8),
          DropdownButton<T>(
            value: value,
            underline: const SizedBox.shrink(),
            dropdownColor: AppColors.card(brightness),
            style: TextStyle(
              fontSize: 13,
              color: AppColors.textPrimary(brightness),
              fontWeight: FontWeight.w500,
            ),
            icon: Icon(
              Icons.expand_more_rounded,
              size: 18,
              color: AppColors.textSecondary(brightness),
            ),
            items: items
                .map(
                  (t) => DropdownMenuItem<T>(
                    value: t,
                    child: Text(
                      itemLabel(t),
                      style: TextStyle(
                        fontSize: 13,
                        color: AppColors.textPrimary(brightness),
                      ),
                    ),
                  ),
                )
                .toList(),
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}
