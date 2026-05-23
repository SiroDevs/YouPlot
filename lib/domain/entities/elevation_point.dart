import 'package:equatable/equatable.dart';

class ElevationPoint extends Equatable {
  final double distanceKm;
  final double elevationM;

  const ElevationPoint({required this.distanceKm, required this.elevationM});

  @override
  List<Object?> get props => [distanceKm, elevationM];
}
