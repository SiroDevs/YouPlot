part of 'location_search_bloc.dart';

sealed class LocationSearchEvent {}

class QueryChanged extends LocationSearchEvent {
  final String query;
  QueryChanged(this.query);
}

/// Fired when user taps the GPS button — resolves immediately.
class LocateMe extends LocationSearchEvent {}

class ClearSearch extends LocationSearchEvent {}

/// Saves a location to local history after user selects it.
class SaveToHistory extends LocationSearchEvent {
  final Location location;
  SaveToHistory(this.location);
}

/// Loads saved history on search screen open.
class LoadHistory extends LocationSearchEvent {}

/// Reverse-geocodes a lat/lng to a named Location (used by map point picker).
class ReverseGeocode extends LocationSearchEvent {
  final double lat;
  final double lng;
  ReverseGeocode(this.lat, this.lng);
}
