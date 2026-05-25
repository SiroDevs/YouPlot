import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:share_plus/share_plus.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../bloc/review/review_cubit.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';

class ExportTab extends StatelessWidget {
  final ReviewState reviewState;
  final Brightness brightness;
  final ValueChanged<ExportFormat> onExport;

  const ExportTab({
    super.key,
    required this.reviewState,
    required this.brightness,
    required this.onExport,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Export route',
            style: Theme.of(context).textTheme.displayMedium,
          ),
          const Gap(4),
          Text(
            'Choose a format to share or use on your device.',
            style: TextStyle(color: AppColors.textSecondary(b), fontSize: 13),
          ),
          const Gap(20),

          _ExportGrid(
            loading: reviewState.loading,
            brightness: b,
            onExport: onExport,
          ),

          if (reviewState.loading) ...[
            const Gap(16),
            const Center(child: CircularProgressIndicator()),
          ],

          if (reviewState.exportedPath != null) ...[
            const Gap(20),
            _ExportSuccessBanner(
              path: reviewState.exportedPath!,
              brightness: b,
            ),
          ],

          if (reviewState.error != null) ...[
            const Gap(12),
            ErrorBar(message: reviewState.error!),
          ],
        ],
      ),
    );
  }
}

class _ExportGrid extends StatelessWidget {
  final bool loading;
  final Brightness brightness;
  final ValueChanged<ExportFormat> onExport;

  const _ExportGrid({
    required this.loading,
    required this.brightness,
    required this.onExport,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;

    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      childAspectRatio: 1.15,
      children: ExportFormat.values.map((fmt) {
        return GestureDetector(
          onTap: loading ? null : () => onExport(fmt),
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.card(b),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.border(b), width: 0.5),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(fmt.emoji, style: const TextStyle(fontSize: 30)),
                const Gap(8),
                Text(
                  fmt.label,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: AppColors.textPrimary(b),
                  ),
                ),
                const Gap(3),
                Text(
                  fmt.description,
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 10,
                    color: AppColors.textSecondary(b),
                  ),
                ),
              ],
            ),
          ),
        );
      }).toList(),
    );
  }
}

class _ExportSuccessBanner extends StatelessWidget {
  final String path;
  final Brightness brightness;

  const _ExportSuccessBanner({required this.path, required this.brightness});

  @override
  Widget build(BuildContext context) {
    final b = brightness;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.success.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.success.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          const Icon(
            Icons.check_circle_rounded,
            color: AppColors.success,
            size: 18,
          ),
          const Gap(10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Exported!',
                style: TextStyle(
                  color: AppColors.success,
                  fontWeight: FontWeight.w600,
                  fontSize: 13,
                ),
              ),
              const Gap(2),
              Text(
                path,
                style: TextStyle(
                  color: AppColors.textSecondary(b),
                  fontSize: 10,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ).expanded(),
          const Gap(8),
          IconButton(
            icon: const Icon(
              Icons.share_rounded,
              size: 18,
              color: AppColors.primary,
            ),
            onPressed: () => Share.shareXFiles([XFile(path)]),
          ),
        ],
      ),
    );
  }
}
