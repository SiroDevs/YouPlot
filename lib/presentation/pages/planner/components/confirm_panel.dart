import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/location.dart';
import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../theme/app_colors.dart';

class ConfirmPanel extends StatelessWidget {
  final Location location;
  final Brightness brightness;
  final bool resolving;
  final VoidCallback onConfirm;
  final VoidCallback onCancel;

  const ConfirmPanel({super.key, 
    required this.location,
    required this.brightness,
    required this.resolving,
    required this.onConfirm,
    required this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    final isDark = b == Brightness.dark;

    return BlocListener<LocationSearchBloc, LocationSearchState>(
      listenWhen: (p, c) => p.reversedLocation != c.reversedLocation,
      listener: (ctx, s) {
        // Panel is rebuilt automatically when state changes
      },
      child: BlocBuilder<LocationSearchBloc, LocationSearchState>(
        buildWhen: (p, c) => p.reversedLocation != c.reversedLocation,
        builder: (ctx, s) {
          final display = s.reversedLocation ?? location;
          return Container(
            margin: const EdgeInsets.all(12),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: isDark
                  ? Colors.black.withValues(alpha: 0.92)
                  : Colors.white.withValues(alpha: 0.97),
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.25),
                  blurRadius: 20,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 32,
                      height: 32,
                      decoration: BoxDecoration(
                        color: AppColors.primaryDim,
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.place_rounded,
                          size: 18, color: AppColors.primary),
                    ),
                    const Gap(12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            display.name ?? 'Selected point',
                            style: TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                              color: AppColors.textPrimary(b),
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          Text(
                            display.address ??
                                '${display.lat.toStringAsFixed(5)}, '
                                    '${display.lng.toStringAsFixed(5)}',
                            style: TextStyle(
                              fontSize: 12,
                              color: AppColors.textSecondary(b),
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                    if (resolving && s.reversedLocation == null)
                      SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: AppColors.primary,
                        ),
                      ),
                  ],
                ),
                const Gap(14),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: onCancel,
                        child: const Text('Cancel'),
                      ),
                    ),
                    const Gap(10),
                    Expanded(
                      flex: 2,
                      child: ElevatedButton.icon(
                        onPressed: onConfirm,
                        icon: const Icon(Icons.check_rounded, size: 16),
                        label: const Text('Use this point'),
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                      ),
                    ),
                  ],
                ),
                Gap(MediaQuery.of(context).padding.bottom),
              ],
            ),
          );
        },
      ),
    );
  }
}
