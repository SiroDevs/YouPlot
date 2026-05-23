import 'package:equatable/equatable.dart';

import 'location.dart';

class Waypoint extends Equatable {
  final String id;
  final Location location;
  final String label;
  final double? distanceFromStartKm;

  const Waypoint({
    required this.id,
    required this.location,
    required this.label,
    this.distanceFromStartKm,
  });

  @override
  List<Object?> get props => [id];
}
