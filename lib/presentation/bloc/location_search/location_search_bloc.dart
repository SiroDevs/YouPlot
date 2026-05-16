import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../../../../domain/entities/entities.dart';
import '../../../../../../domain/usecases/usecases.dart';

part 'location_search_event.dart';
part 'location_search_state.dart';

class LocationSearchBloc extends Bloc<LocationSearchEvent, LocationSearchState> {
  final SearchPlaces _searchPlaces;
  final GetCurrentLocation _getCurrentLocation;

  LocationSearchBloc({
    required SearchPlaces searchPlaces,
    required GetCurrentLocation getCurrentLocation,
  })  : _searchPlaces = searchPlaces,
        _getCurrentLocation = getCurrentLocation,
        super(const LocationSearchState()) {
    on<QueryChanged>(_onQueryChanged);
    on<LocateMe>(_onLocateMe);
    on<ClearSearch>(_onClear);
  }

  Future<void> _onQueryChanged(QueryChanged e, Emitter<LocationSearchState> emit) async {
    if (e.query.trim().length < 2) {
      emit(state.copyWith(results: [], loading: false, query: e.query));
      return;
    }
    emit(state.copyWith(loading: true, query: e.query));
    final result = await _searchPlaces(SearchPlacesParams(e.query));
    result.fold(
      (f) => emit(state.copyWith(loading: false, error: f.message)),
      (locs) => emit(state.copyWith(loading: false, results: locs, error: null)),
    );
  }

  Future<void> _onLocateMe(LocateMe e, Emitter<LocationSearchState> emit) async {
    emit(state.copyWith(locating: true));
    final result = await _getCurrentLocation();
    result.fold(
      (f) => emit(state.copyWith(locating: false, error: f.message)),
      (loc) => emit(state.copyWith(locating: false, currentLocation: loc, error: null)),
    );
  }

  void _onClear(ClearSearch e, Emitter<LocationSearchState> emit) =>
      emit(const LocationSearchState());
}
