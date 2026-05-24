part of 'route_builder_bloc.dart';

sealed class RouteBuilderEvent {}

class SetOrigin extends RouteBuilderEvent {
  final Location location;
  SetOrigin(this.location);
}

class SetDestination extends RouteBuilderEvent {
  final Location location;
  SetDestination(this.location);
}

class SetSport extends RouteBuilderEvent {
  final SportType sport;
  SetSport(this.sport);
}

class SetUnit extends RouteBuilderEvent {
  final DistanceUnit unit;
  SetUnit(this.unit);
}

class AddViaPoint extends RouteBuilderEvent {
  final Location location;
  AddViaPoint(this.location);
}

class RemoveViaPoint extends RouteBuilderEvent {
  final int index;
  RemoveViaPoint(this.index);
}

class RequestSuggestions extends RouteBuilderEvent {}

class AcceptSuggestions extends RouteBuilderEvent {}

class GenerateRoute extends RouteBuilderEvent {}

class SetDays extends RouteBuilderEvent {
  final int days;
  SetDays(this.days);
}

class SetSpeed extends RouteBuilderEvent {
  final double kmh;
  SetSpeed(this.kmh);
}

class SetStartTime extends RouteBuilderEvent {
  final DateTime time;
  SetStartTime(this.time);
}

class ToggleBreak extends RouteBuilderEvent {
  final BreakType type;
  ToggleBreak(this.type);
}

class BuildPlanEvent extends RouteBuilderEvent {}

class ExportEvent extends RouteBuilderEvent {
  final ExportFormat format;
  ExportEvent(this.format);
}

class GoToStep extends RouteBuilderEvent {
  final AppStep step;
  GoToStep(this.step);
}

class ResetAll extends RouteBuilderEvent {}

class MapControllerReady extends RouteBuilderEvent {
  final MapController controller;
  MapControllerReady(this.controller);
}

class SetImportedRoute extends RouteBuilderEvent {
  final RouteMap route;
  SetImportedRoute(this.route);
}
