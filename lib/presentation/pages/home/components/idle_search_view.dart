import 'package:flutter/material.dart';

import '../../../theme/app_colors.dart';

class IdleSearchView extends StatelessWidget {
  final Brightness brightness;
  const IdleSearchView({super.key, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final tips = [
      ('London, UK', Icons.history_rounded),
      ('Paris, France', Icons.history_rounded),
      ('Current Location', Icons.my_location_rounded),
    ];

    return ListView(
      padding: const EdgeInsets.symmetric(vertical: 8),
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
          child: Text(
            'SUGGESTIONS',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w700,
              letterSpacing: 1.1,
              color: AppColors.textMuted(b),
            ),
          ),
        ),
        ...tips.map(
          (t) => ListTile(
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 20,
              vertical: 4,
            ),
            leading: Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: AppColors.surface(b),
                shape: BoxShape.circle,
              ),
              child: Icon(t.$2, size: 18, color: AppColors.textSecondary(b)),
            ),
            title: Text(
              t.$1,
              style: TextStyle(fontSize: 14, color: AppColors.textPrimary(b)),
            ),
            trailing: Icon(
              Icons.north_west_rounded,
              size: 16,
              color: AppColors.textMuted(b),
            ),
            onTap: () {
              // In real implementation, this would fill the field
            },
          ),
        ),
      ],
    );
  }
}
