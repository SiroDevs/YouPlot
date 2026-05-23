import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../theme/app_colors.dart';

/// Shown when the search field has fewer than 2 characters.
/// Displays the user's real search history (from SharedPreferences).
/// On first use, shows nothing — no fake hardcoded suggestions.
class IdleSearchView extends StatefulWidget {
  final Brightness brightness;
  final ValueChanged<String> onQueryFill; // fills the text field

  const IdleSearchView({
    super.key,
    required this.brightness,
    required this.onQueryFill,
  });

  @override
  State<IdleSearchView> createState() => _IdleSearchViewState();
}

class _IdleSearchViewState extends State<IdleSearchView> {
  @override
  void initState() {
    super.initState();
    // Load history the moment the idle view appears
    context.read<LocationSearchBloc>().add(LoadHistory());
  }

  @override
  Widget build(BuildContext context) {
    final b = widget.brightness;

    return BlocBuilder<LocationSearchBloc, LocationSearchState>(
      buildWhen: (prev, curr) => prev.history != curr.history,
      builder: (ctx, state) {
        if (state.history.isEmpty) {
          // First time — nothing to show
          return Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.search_rounded, size: 48,
                    color: AppColors.textMuted(b)),
                const Gap(12),
                Text(
                  'Start typing to search',
                  style: TextStyle(
                      color: AppColors.textSecondary(b), fontSize: 14),
                ),
                const Gap(4),
                Text(
                  'Recent places will appear here',
                  style: TextStyle(
                      color: AppColors.textMuted(b), fontSize: 12),
                ),
              ],
            ),
          );
        }

        return ListView(
          padding: const EdgeInsets.symmetric(vertical: 8),
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
              child: Text(
                'RECENT',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 1.1,
                  color: AppColors.textMuted(b),
                ),
              ),
            ),
            ...state.history.map((loc) {
              final label = loc.name ?? loc.address ?? '${loc.lat}, ${loc.lng}';
              final sub = loc.address;
              return ListTile(
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                leading: Container(
                  width: 36, height: 36,
                  decoration: BoxDecoration(
                    color: AppColors.surface(b),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(Icons.history_rounded,
                      size: 18, color: AppColors.textSecondary(b)),
                ),
                title: Text(label,
                    style: TextStyle(
                        fontSize: 14, color: AppColors.textPrimary(b))),
                subtitle: sub != null && sub != label
                    ? Text(sub,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                            fontSize: 11, color: AppColors.textSecondary(b)))
                    : null,
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Fill search field with this query for refinement
                    GestureDetector(
                      onTap: () => widget.onQueryFill(label),
                      child: Icon(Icons.north_west_rounded,
                          size: 16, color: AppColors.textMuted(b)),
                    ),
                  ],
                ),
                onTap: () {
                  // Pop immediately with the history location
                  Navigator.pop(context, loc);
                },
              );
            }),
          ],
        );
      },
    );
  }
}
