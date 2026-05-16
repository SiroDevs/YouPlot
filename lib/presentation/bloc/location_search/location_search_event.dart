part of 'location_search_bloc.dart';

sealed class LocationSearchEvent {}

class QueryChanged extends LocationSearchEvent {
  final String query;
  QueryChanged(this.query);
}

class LocateMe extends LocationSearchEvent {}

class ClearSearch extends LocationSearchEvent {}
