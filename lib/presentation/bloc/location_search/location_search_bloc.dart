import 'dart:async';
import 'dart:convert';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../domain/entities/location.dart';
import '../../../domain/repos/location_repo.dart';
import '../../../domain/usecases/get_current_location.dart';
import '../../../domain/usecases/search_places.dart';

part 'location_search_event.dart';
part 'location_search_state.dart';

const _kHistoryKey = 'location_history_v1';
const _kMaxHistory = 8;

class LocationSearchBloc extends Bloc<LocationSearchEvent, LocationSearchState> {
  final SearchPlaces _searchPlaces;
  final GetCurrentLocation _getCurrentLocation;
  final LocationRepo _locationRepo;
  final SharedPreferences _prefs;

  Timer? _debounce;

  LocationSearchBloc({
    required SearchPlaces searchPlaces,
    required GetCurrentLocation getCurrentLocation,
    required LocationRepo locationRepo,
    required SharedPreferences prefs,
  })  : _searchPlaces = searchPlaces,
        _getCurrentLocation = getCurrentLocation,
        _locationRepo = locationRepo,
        _prefs = prefs,
        super(const LocationSearchState()) {
    on<QueryChanged>(_onQueryChanged);
    on<LocateMe>(_onLocateMe);
    on<ClearSearch>(_onClear);
    on<SaveToHistory>(_onSaveHistory);
    on<LoadHistory>(_onLoadHistory);
    on<ReverseGeocode>(_onReverseGeocode);
  }

  @override
  Future<void> close() {
    _debounce?.cancel();
    return super.close();
  }

  Future<void> _onQueryChanged(
    QueryChanged e,
    Emitter<LocationSearchState> emit,
  ) async {
    _debounce?.cancel();

    if (e.query.trim().length < 2) {
      emit(state.copyWith(results: [], loading: false, query: e.query));
      return;
    }

    emit(state.copyWith(loading: true, query: e.query));

    final completer = Completer<void>();
    _debounce = Timer(const Duration(milliseconds: 300), () async {
      if (isClosed) return;
      final result = await _searchPlaces(SearchPlacesParams(e.query));
      result.fold(
        (f) => emit(state.copyWith(loading: false, error: f.message)),
        (locs) => emit(state.copyWith(loading: false, results: locs, error: null)),
      );
      completer.complete();
    });

    await completer.future;
  }

  Future<void> _onLocateMe(
    LocateMe e,
    Emitter<LocationSearchState> emit,
  ) async {
    emit(state.copyWith(locating: true, error: null));

    final result = await _getCurrentLocation();
    result.fold(
      (f) => emit(state.copyWith(locating: false, error: f.message)),
      (loc) => emit(state.copyWith(
        locating: false,
        currentLocation: loc,
        error: null,
      )),
    );
  }

  Future<void> _onReverseGeocode(
    ReverseGeocode e,
    Emitter<LocationSearchState> emit,
  ) async {
    emit(state.copyWith(clearReversed: true));
    final result = await _locationRepo.reverseGeocode(e.lat, e.lng);
    result.fold(
      (_) {
        emit(state.copyWith(
          reversedLocation: Location(
            lat: e.lat,
            lng: e.lng,
            name: '${e.lat.toStringAsFixed(5)}, ${e.lng.toStringAsFixed(5)}',
          ),
        ));
      },
      (loc) => emit(state.copyWith(reversedLocation: loc)),
    );
  }

  void _onClear(ClearSearch e, Emitter<LocationSearchState> emit) =>
      emit(LocationSearchState(history: state.history));

  Future<void> _onLoadHistory(
    LoadHistory e,
    Emitter<LocationSearchState> emit,
  ) async {
    final raw = _prefs.getStringList(_kHistoryKey) ?? [];
    final history = raw
        .map((s) {
          try {
            final m = jsonDecode(s) as Map<String, dynamic>;
            return Location(
              lat: (m['lat'] as num).toDouble(),
              lng: (m['lng'] as num).toDouble(),
              name: m['name'] as String?,
              address: m['address'] as String?,
            );
          } catch (_) {
            return null;
          }
        })
        .whereType<Location>()
        .toList();
    emit(state.copyWith(history: history));
  }

  Future<void> _onSaveHistory(
    SaveToHistory e,
    Emitter<LocationSearchState> emit,
  ) async {
    final loc = e.location;
    final updated = [
      loc,
      ...state.history.where((h) => h.lat != loc.lat || h.lng != loc.lng),
    ].take(_kMaxHistory).toList();

    emit(state.copyWith(history: updated));

    final raw = updated
        .map((l) => jsonEncode({
              'lat': l.lat,
              'lng': l.lng,
              'name': l.name,
              'address': l.address,
            }))
        .toList();
    await _prefs.setStringList(_kHistoryKey, raw);
  }
}
