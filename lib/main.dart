import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../core/di/injection.dart' as di;
import '../presentation/bloc/location_search/location_search_bloc.dart';
import '../presentation/bloc/route_builder/route_builder_bloc.dart';
import '../presentation/pages/home_page.dart';
import '../presentation/theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.light,
  ));

  await di.init();

  runApp(const TrailForgeApp());
}

class TrailForgeApp extends StatelessWidget {
  const TrailForgeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<RouteBuilderBloc>(
          create: (_) => di.sl<RouteBuilderBloc>(),
        ),
        BlocProvider<LocationSearchBloc>(
          create: (_) => di.sl<LocationSearchBloc>(),
        ),
      ],
      child: MaterialApp(
        title: 'TrailForge',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.dark,
        home: const HomePage(),
      ),
    );
  }
}
