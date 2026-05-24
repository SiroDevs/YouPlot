import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../theme/app_colors.dart';

class GpsButton extends StatelessWidget {
  const GpsButton({super.key});

  @override
  Widget build(BuildContext context) {
    final brightness = Theme.of(context).brightness;
    
    return BlocBuilder<LocationSearchBloc, LocationSearchState>(
      buildWhen: (previous, current) => previous.locating != current.locating,
      builder: (context, state) {
        return GestureDetector(
          onTap: state.locating
              ? null
              : () => context.read<LocationSearchBloc>().add(LocateMe()),
          child: Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: state.locating
                  ? AppColors.primaryDim
                  : AppColors.surface(brightness),
              shape: BoxShape.circle,
              border: Border.all(
                color: state.locating
                    ? AppColors.primary
                    : AppColors.border(brightness),
                width: 0.5,
              ),
            ),
            child: state.locating
                ? const Padding(
                    padding: EdgeInsets.all(10),
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: AppColors.primary,
                    ),
                  )
                : const Icon(
                    Icons.my_location_rounded,
                    size: 20,
                    color: AppColors.primary,
                  ),
          ),
        );
      },
    );
  }
}