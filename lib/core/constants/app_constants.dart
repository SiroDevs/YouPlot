const String kMapboxToken = 'YOUR_MAPBOX_ACCESS_TOKEN';
const String kMapboxStyleOutdoors = 'mapbox://styles/mapbox/outdoors-v12';
const String kMapboxStyleDark = 'mapbox://styles/mapbox/dark-v11';
const String kMapboxDirections = 'https://api.mapbox.com/directions/v5/mapbox';
const String kMapboxGeocode = 'https://api.mapbox.com/geocoding/v5/mapbox.places';

const String kAppName = 'YouPlot';
const String kSavedPlansKey = 'saved_plans_v1';

enum SportType {
  walking('Walking', '🚶', 'walking', 5.0),
  running('Running', '🏃', 'walking', 10.0),
  cycling('Cycling', '🚴', 'cycling', 20.0),
  skating('Skating', '⛸️', 'cycling', 15.0),
  hiking('Hiking', '🥾', 'walking', 4.0);

  const SportType(this.label, this.emoji, this.mapboxProfile, this.defaultSpeedKmh);
  final String label;
  final String emoji;
  final String mapboxProfile;
  final double defaultSpeedKmh;
}

enum DistanceUnit {
  kilometers('km', 0.621371),
  miles('mi', 1.0);

  const DistanceUnit(this.symbol, this.toMilesFactor);
  final String symbol;
  final double toMilesFactor;
}

enum BreakType {
  breakfast('Breakfast', '🍳', 30),
  pitStop('Pit Stop', '⛽', 10),
  lunch('Lunch', '🥗', 45),
  supper('Supper', '🍽️', 60),
  overnight('Overnight rest', '😴', 480);

  const BreakType(this.label, this.emoji, this.defaultMinutes);
  final String label;
  final String emoji;
  final int defaultMinutes;
}

enum ExportFormat {
  gpx('GPX', '📍', 'GPS / Garmin / Komoot'),
  pdf('PDF', '📄', 'Printable document'),
  image('Image', '🖼️', 'Share as JPEG');

  const ExportFormat(this.label, this.emoji, this.description);
  final String label;
  final String emoji;
  final String description;
}

enum WaypointMode { suggest, custom, skip }
