# TrailForge — Setup Guide

## 1. Get a Mapbox token

1. Sign up at https://mapbox.com (free tier is enough)
2. Account → Tokens → Create a token with scopes:
   - `styles:read`, `tiles:read`, `geocoding:read`, `directions:read`
3. Replace `YOUR_MAPBOX_ACCESS_TOKEN` in three places:
   - `lib/core/constants/app_constants.dart` → `kMapboxToken`
   - `android/app/src/main/AndroidManifest.xml` → `<meta-data android:name="com.mapbox.token" .../>`
   - `ios/Runner/Info.plist` → `MBXAccessToken`

## 2. Android

In `android/app/build.gradle`:
```groovy
android {
    defaultConfig {
        minSdkVersion 21      // required by Mapbox Maps v2
        targetSdkVersion 34
    }
}
```

Add to `android/build.gradle` repositories:
```groovy
maven { url 'https://api.mapbox.com/downloads/v2/releases/maven' }
```

## 3. iOS

In `ios/Podfile`:
```ruby
platform :ios, '13.0'
```

Merge `ios/Runner/InfoAdditions.plist` keys into `ios/Runner/Info.plist`.

## 4. Run

```bash
flutter pub get
flutter run
```

## 5. Wire the real Mapbox map

In `lib/presentation/pages/home_page.dart`, inside `_MapPage._build()`,
replace the placeholder `Center(...)` with:

```dart
import 'package:mapbox_maps_flutter/mapbox_maps_flutter.dart';

MapboxMap(
  mapboxOptions: MapboxOptions(accessToken: kMapboxToken),
  styleUri: kMapboxStyleDark,
  cameraOptions: CameraOptions(
    center: Point(
      coordinates: Position(route.origin.lng, route.origin.lat),
    ),
    zoom: 9,
  ),
  onMapCreated: (controller) async {
    // Draw route polyline
    await controller.style.addSource(GeoJsonSource(
      id: 'route',
      data: jsonEncode({
        'type': 'Feature',
        'geometry': {
          'type': 'LineString',
          'coordinates': route.geometry,
        }
      }),
    ));
    await controller.style.addLayer(LineLayer(
      id: 'route-line',
      sourceId: 'route',
      lineColorExpression: ['literal', '#00D4A3'],
      lineWidthExpression: ['literal', 3.5],
      lineCapExpression: ['literal', 'round'],
      lineJoinExpression: ['literal', 'round'],
    ));

    // Add start/end markers
    for (final wp in route.waypoints) {
      await controller.annotations.createPointAnnotationManager().then((mgr) {
        mgr.create(PointAnnotationOptions(
          geometry: Point(coordinates: Position(wp.location.lng, wp.location.lat)),
        ));
      });
    }
  },
)
```

## 6. Project structure

```
lib/
├── core/
│   ├── constants/      app_constants.dart      enums, tokens, keys
│   ├── di/             injection.dart          GetIt wiring
│   ├── errors/         failures.dart           sealed Failure classes
│   ├── usecases/       usecase.dart            base abstract classes
│   └── utils/          formatters.dart         Fmt helpers
│
├── domain/                                     ← pure Dart, no Flutter
│   ├── entities/       entities.dart           Location, Route, RoutePlan…
│   ├── repositories/   repositories.dart       abstract interfaces
│   └── usecases/       usecases.dart           BuildRoute, BuildPlan, Export…
│
├── data/                                       ← implements domain
│   ├── datasources/    mapbox_datasource.dart  Directions + Geocoding + Elevation
│   │                   location_datasource.dart GPS
│   └── repositories/   *_impl.dart             concrete implementations
│
└── presentation/                               ← Flutter UI
    ├── bloc/
    │   ├── route_builder/   RouteBuilderBloc   7-step wizard, 14 events
    │   └── location_search/ LocationSearchBloc debounced search + GPS
    ├── pages/          home_page.dart          all 7 steps in one file
    ├── theme/          app_theme.dart          dark design system
    └── widgets/        widgets.dart            ElevationChart, LocationField…
```
