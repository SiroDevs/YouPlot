import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:share_plus/share_plus.dart';

import '../../../../../../core/constants/app_constants.dart';
import '../../../bloc/route_builder/route_builder_bloc.dart';
import '../../../theme/app_colors.dart';
import '../../../widgets/state_widgets.dart';
import '../../../widgets/steps/headers.dart';

class ExportStep extends StatelessWidget {
  const ExportStep({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    return BlocBuilder<RouteBuilderBloc, RouteBuilderState>(
      builder: (ctx, state) {
        final b = Theme.of(ctx).brightness;
        return Scaffold(
          backgroundColor: AppColors.bg(b),
          body: Column(children: [
            AppHeader(
              showBack: true,
              onBack: () => bloc.add(GoToStep(AppStep.review)),
            ),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Export route',
                        style: Theme.of(ctx).textTheme.displayMedium),
                    const Gap(4),
                    Text('Choose a format to share or use on your device.',
                        style: TextStyle(
                            color: AppColors.textSecondary(b), fontSize: 13)),
                    const Gap(24),

                    GridView.count(
                      crossAxisCount: 2,
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      crossAxisSpacing: 12,
                      mainAxisSpacing: 12,
                      childAspectRatio: 1.15,
                      children: ExportFormat.values.map((fmt) {
                        return GestureDetector(
                          onTap: state.loading
                              ? null
                              : () => bloc.add(ExportEvent(fmt)),
                          child: Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: AppColors.card(b),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                  color: AppColors.border(b), width: 0.5),
                            ),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(fmt.emoji,
                                    style: const TextStyle(fontSize: 30)),
                                const Gap(8),
                                Text(fmt.label,
                                    style: TextStyle(
                                        fontSize: 14,
                                        fontWeight: FontWeight.w600,
                                        color: AppColors.textPrimary(b))),
                                const Gap(3),
                                Text(fmt.description,
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        fontSize: 10,
                                        color: AppColors.textSecondary(b))),
                              ],
                            ),
                          ),
                        );
                      }).toList(),
                    ),

                    if (state.exportedPath != null) ...[
                      const Gap(20),
                      Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: AppColors.success.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                              color: AppColors.success.withValues(alpha: 0.3)),
                        ),
                        child: Row(children: [
                          const Icon(Icons.check_circle_rounded,
                              color: AppColors.success, size: 18),
                          const Gap(10),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text('Exported!',
                                    style: TextStyle(
                                        color: AppColors.success,
                                        fontWeight: FontWeight.w600,
                                        fontSize: 13)),
                                const Gap(2),
                                Text(state.exportedPath!,
                                    style: TextStyle(
                                        color: AppColors.textSecondary(b),
                                        fontSize: 10),
                                    maxLines: 2,
                                    overflow: TextOverflow.ellipsis),
                              ],
                            ),
                          ),
                          const Gap(8),
                          IconButton(
                            icon: const Icon(Icons.share_rounded,
                                size: 18, color: AppColors.primary),
                            onPressed: () => Share.shareXFiles(
                                [XFile(state.exportedPath!)]),
                          ),
                        ]),
                      ),
                    ],

                    if (state.error != null) ...[
                      const Gap(12),
                      ErrorBar(message: state.error!),
                    ],
                  ],
                ),
              ),
            ),
          ]),
        );
      },
    );
  }
}
