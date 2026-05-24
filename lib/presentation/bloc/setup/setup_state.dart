part of 'setup_cubit.dart';

class SetupState extends Equatable {
  final SportType sport;
  final DistanceUnit unit;
  final Location? origin;
  final Location? destination;
  final String? error;

  const SetupState({
    this.sport = SportType.cycling,
    this.unit = DistanceUnit.kilometers,
    this.origin,
    this.destination,
    this.error,
  });

  bool get canProceed => origin != null && destination != null;

  SetupState copyWith({
    SportType? sport,
    DistanceUnit? unit,
    Location? origin,
    Location? destination,
    String? error,
    bool clearError = false,
  }) {
    return SetupState(
      sport: sport ?? this.sport,
      unit: unit ?? this.unit,
      origin: origin ?? this.origin,
      destination: destination ?? this.destination,
      error: clearError ? null : (error ?? this.error),
    );
  }

  @override
  List<Object?> get props => [sport, unit, origin, destination, error];
}
