import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../core/errors/failures.dart';
import '../../domain/entities/location.dart';

class LocationDatasource {
  Future<Location> getCurrentLocation() async {
    // 1. Check / request permission — non-blocking fast path
    var status = await Permission.locationWhenInUse.status;
    if (status.isDenied) {
      status = await Permission.locationWhenInUse.request();
    }
    if (status.isDenied || status.isPermanentlyDenied) {
      throw const LocationException('Location permission denied');
    }

    // 2. Check if service is enabled
    final enabled = await Geolocator.isLocationServiceEnabled();
    if (!enabled) throw const LocationException('Location services are off');

    // 3. Try last known position first — instant, no GPS warm-up delay
    try {
      final last = await Geolocator.getLastKnownPosition();
      if (last != null) {
        return Location(
          lat: last.latitude,
          lng: last.longitude,
          name: 'Current location',
        );
      }
    } catch (_) {}

    // 4. Fall back to fresh position with a reasonable timeout
    final pos = await Geolocator.getCurrentPosition(
      desiredAccuracy: LocationAccuracy.medium, // faster than .high
      timeLimit: const Duration(seconds: 8),
    );
    return Location(
      lat: pos.latitude,
      lng: pos.longitude,
      name: 'Current location',
    );
  }
}
