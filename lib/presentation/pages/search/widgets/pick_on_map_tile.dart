import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../domain/entities/location.dart';
import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../theme/app_colors.dart';
import 'map_point_picker.dart';

class PickOnMapTile extends StatelessWidget {
  final Brightness brightness;
  final void Function(Location) onSelected;

  const PickOnMapTile({
    super.key,
    required this.brightness,
    required this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.primaryDim,
          shape: BoxShape.circle,
        ),
        child: const Icon(
          Icons.touch_app_rounded,
          size: 20,
          color: AppColors.primary,
        ),
      ),
      title: Text(
        'Pick a point on the map',
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w600,
          color: AppColors.textPrimary(b),
        ),
      ),
      subtitle: Text(
        'Tap anywhere to drop a pin',
        style: TextStyle(fontSize: 12, color: AppColors.textSecondary(b)),
      ),
      trailing: Icon(
        Icons.arrow_forward_ios_rounded,
        size: 14,
        color: AppColors.textMuted(b),
      ),
      onTap: () async {
        context.read<LocationSearchBloc>().add(ClearSearch());

        final loc = await Navigator.push<Location>(
          context,
          PageRouteBuilder(
            pageBuilder: (_, __, ___) => MapPointPicker(brightness: b),
            transitionsBuilder: (_, anim, __, child) =>
                FadeTransition(opacity: anim, child: child),
            transitionDuration: const Duration(milliseconds: 240),
          ),
        );

        if (loc != null) {
          onSelected(loc);
        }
      },
    );
  }
}
