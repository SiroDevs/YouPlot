const String kNominatimBase = 'https://nominatim.openstreetmap.org';
const String kOsrmBase = 'https://router.project-osrm.org/route/v1';
const String kOsmTileTemplate =
    'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
const String kOsmTileTemplateDark =
    'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png';

const String kOsmCycleTile =
    'https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png';
const String kOsmHikingTile =
    'https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png';
const String kElevationLookup = 'https://api.open-elevation.com/api/v1/lookup';

const String kAppName = 'YouPlot';
const String kSavedPlansKey = 'saved_plans_v1';
const String kAppCredits = '© Siro Devs';
const String kAppCredits2 = '© OpenStreetMap contributors';
const String kAppPackage = 'com.sirodevs.youplot';

enum AppStep { setup, waypoints, map, plan, review }

enum SportType {
  walking('Walking', '🚶', 'foot', 5.0),
  running('Running', '🏃', 'foot', 10.0),
  cycling('Cycling', '🚴', 'bike', 20.0);

  const SportType(
    this.label,
    this.emoji,
    this.osmrProfile,
    this.defaultSpeedKmh,
  );
  final String label;
  final String emoji;
  final String osmrProfile;
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
