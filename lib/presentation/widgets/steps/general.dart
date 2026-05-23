import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../bloc/route_builder/route_builder_bloc.dart';
import '../../theme/app_colors.dart';
import '../maps/map_background.dart';

class FieldLabel extends StatelessWidget {
  final String label;
  final IconData? icon;
  final Brightness brightness;

  const FieldLabel({
    super.key,
    required this.label,
    required this.brightness,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return Row(
      children: [
        if (icon != null) ...[
          Icon(icon, size: 14, color: AppColors.primary),
          const Gap(6),
        ],
        Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w600,
            color: AppColors.textPrimary(b),
          ),
        ),
      ],
    );
  }
}

class RoutePainter extends CustomPainter {
  final bool isDark;
  const RoutePainter({required this.isDark});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = AppColors.primary.withValues(alpha: 0.7)
      ..strokeWidth = 4
      ..strokeCap = StrokeCap.round
      ..style = PaintingStyle.stroke;

    final path = Path();
    path.moveTo(size.width * 0.15, size.height * 0.65);
    path.cubicTo(
      size.width * 0.3, size.height * 0.3,
      size.width * 0.65, size.height * 0.45,
      size.width * 0.85, size.height * 0.35,
    );
    canvas.drawPath(path, paint);

    canvas.drawCircle(
      Offset(size.width * 0.15, size.height * 0.65),
      8,
      Paint()..color = AppColors.success,
    );
    canvas.drawCircle(
      Offset(size.width * 0.85, size.height * 0.35),
      8,
      Paint()..color = AppColors.primary,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class AddViaSheet extends StatelessWidget {
  const AddViaSheet({super.key});

  @override
  Widget build(BuildContext context) {
    final bloc = context.read<RouteBuilderBloc>();
    final b = Theme.of(context).brightness;
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        Container(
          width: 36, height: 4,
          decoration: BoxDecoration(
              color: AppColors.border(b),
              borderRadius: BorderRadius.circular(2)),
        ),
        const Gap(16),
        Text('Add a stop',
            style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary(b))),
        const Gap(16),
        MapSearchField(
          hint: 'Search town or landmark…',
          brightness: b,
          onSelected: (loc) {
            bloc.add(AddViaPoint(loc));
            Navigator.pop(context);
          },
        ),
      ]),
    );
  }
}
