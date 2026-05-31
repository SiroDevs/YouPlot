import 'package:equatable/equatable.dart';

import '../../core/constants/app_constants.dart';

class RouteBreak extends Equatable {
  final String id;
  final BreakType type;
  final DateTime scheduledAt;
  final Duration duration;
  final double? distanceFromStartKm;

  const RouteBreak({
    required this.id,
    required this.type,
    required this.scheduledAt,
    required this.duration,
    this.distanceFromStartKm,
  });

  @override
  List<Object?> get props => [id];
}
