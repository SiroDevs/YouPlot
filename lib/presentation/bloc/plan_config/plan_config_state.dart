part of 'plan_config_cubit.dart';

class PlanConfigState extends Equatable {
  final int days;
  final double speed;
  final DateTime startTime;
  final Set<BreakType> selectedBreaks;
  final bool loading;
  final String? error;

  const PlanConfigState({
    this.days = 1,
    this.speed = 4.0,
    required this.startTime,
    this.selectedBreaks = const {},
    this.loading = false,
    this.error,
  });

  double get displaySpeed => speed; // conversion handled in UI via session unit

  PlanConfigState copyWith({
    int? days,
    double? speed,
    DateTime? startTime,
    Set<BreakType>? selectedBreaks,
    bool? loading,
    String? error,
    bool clearError = false,
  }) {
    return PlanConfigState(
      days: days ?? this.days,
      speed: speed ?? this.speed,
      startTime: startTime ?? this.startTime,
      selectedBreaks: selectedBreaks ?? this.selectedBreaks,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
    );
  }

  @override
  List<Object?> get props =>
      [days, speed, startTime, selectedBreaks, loading, error];
}
