import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../../../core/errors/failures.dart';
import '../../../../domain/entities/entities.dart';

class LocationDatasource {
  Future<Location> getCurrentLocation() async {
    final status = await Permission.locationWhenInUse.request();
    if (status.isDenied || status.isPermanentlyDenied) {
      throw const LocationException('Location permission denied');
    }

    final enabled = await Geolocator.isLocationServiceEnabled();
    if (!enabled) throw const LocationException('Location services are off');

    final pos = await Geolocator.getCurrentPosition(
      desiredAccuracy: LocationAccuracy.high,
      timeLimit: const Duration(seconds: 10),
    );
    return Location(lat: pos.latitude, lng: pos.longitude, name: 'Current location');
  }
}
