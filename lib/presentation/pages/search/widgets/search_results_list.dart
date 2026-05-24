import 'package:flutter/material.dart';
import 'package:gap/gap.dart';

import '../../../theme/app_colors.dart';

class SearchResultsList extends StatelessWidget {
  final List<dynamic> results;
  final String query;
  final Brightness brightness;
  final Function(dynamic) onSelect;

  const SearchResultsList({
    super.key,
    required this.results,
    required this.query,
    required this.brightness,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    if (results.isEmpty) {
      return _EmptyResultsView(query: query, brightness: brightness);
    }

    return ListView.separated(
      itemCount: results.length,
      separatorBuilder: (_, _) => Divider(height: 0, color: AppColors.border(brightness)),
      itemBuilder: (context, index) {
        final location = results[index];
        return _ResultTile(
          location: location,
          brightness: brightness,
          onTap: () => onSelect(location),
        );
      },
    );
  }
}

class _ResultTile extends StatelessWidget {
  final dynamic location;
  final Brightness brightness;
  final VoidCallback onTap;

  const _ResultTile({
    required this.location,
    required this.brightness,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(
        horizontal: 20,
        vertical: 8,
      ),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.surface(brightness),
          shape: BoxShape.circle,
        ),
        child: const Icon(
          Icons.place_rounded,
          size: 20,
          color: AppColors.primary,
        ),
      ),
      title: Text(
        location.name ?? '',
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w500,
          color: AppColors.textPrimary(brightness),
        ),
      ),
      subtitle: location.address != null
          ? Text(
              location.address!,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 12,
                color: AppColors.textSecondary(brightness),
              ),
            )
          : null,
      trailing: Icon(
        Icons.north_west_rounded,
        size: 16,
        color: AppColors.textMuted(brightness),
      ),
      onTap: onTap,
    );
  }
}

class _EmptyResultsView extends StatelessWidget {
  final String query;
  final Brightness brightness;

  const _EmptyResultsView({
    required this.query,
    required this.brightness,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.search_off_rounded,
            size: 48,
            color: AppColors.textMuted(brightness),
          ),
          const Gap(12),
          Text(
            'No results for "$query"',
            style: TextStyle(
              color: AppColors.textSecondary(brightness),
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }
}