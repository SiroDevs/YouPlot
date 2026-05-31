import 'package:equatable/equatable.dart';

class Location extends Equatable {
  final double lat;
  final double lng;
  final String? name;
  final String? address;

  const Location({required this.lat, required this.lng, this.name, this.address});

  @override
  List<Object?> get props => [lat, lng];

  @override
  String toString() => name ?? address ?? '$lat,$lng';
}
