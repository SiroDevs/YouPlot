part of 'review_cubit.dart';

class ReviewState extends Equatable {
  final bool loading;
  final String? error;
  final String? exportedPath;

  const ReviewState({
    this.loading = false,
    this.error,
    this.exportedPath,
  });

  ReviewState copyWith({
    bool? loading,
    String? error,
    String? exportedPath,
    bool clearError = false,
    bool clearExport = false,
  }) {
    return ReviewState(
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
      exportedPath: clearExport ? null : (exportedPath ?? this.exportedPath),
    );
  }

  @override
  List<Object?> get props => [loading, error, exportedPath];
}
