part of 'location_search_bloc.dart';

sealed class LocationSearchEvent {}

class QueryChanged extends LocationSearchEvent {
  final String query;
  QueryChanged(this.query);
}

class LocateMe extends LocationSearchEvent {}

class ClearSearch extends LocationSearchEvent {}

class SaveToHistory extends LocationSearchEvent {
  final Location location;
  SaveToHistory(this.location);
}

class LoadHistory extends LocationSearchEvent {}

class ReverseGeocode extends LocationSearchEvent {
  final double lat;
  final double lng;
  ReverseGeocode(this.lat, this.lng);
}
