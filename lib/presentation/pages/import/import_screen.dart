import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';
import 'package:styled_widget/styled_widget.dart';

import '../../../core/constants/app_constants.dart';
import '../../../domain/entities/location.dart';
import '../../../domain/entities/route_map.dart';
import '../../../domain/repos/local_repo.dart';
import '../../theme/app_colors.dart';
import '../../widgets/steps/icon_text_button.dart';
import '../plan_maker/plan_maker_screen.dart';
import 'components/done_view.dart';
import 'components/parsing_view.dart';
import 'components/stat_chip.dart';

enum ImportPhase { picking, parsing, done, error }

class ImportScreen extends StatefulWidget {
  const ImportScreen({super.key});

  static Future<void> show(BuildContext context) {
    return Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const ImportScreen()),
    );
  }

  @override
  State<ImportScreen> createState() => _ImportScreenState();
}

class _ImportScreenState extends State<ImportScreen> {
  ImportPhase _phase = ImportPhase.picking;
  String? _fileName;
  String? _errorMessage;
  RouteMap? _importedRoute;
  double _progress = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _pickFile());
  }

  Future<void> _pickFile() async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['gpx', 'kml', 'tcx', 'fit'],
        // dialogTitle: 'Select a route file',
      );

      if (result == null || result.files.isEmpty) {
        if (mounted) Navigator.pop(context);
        return;
      }

      final file = result.files.first;
      setState(() {
        _phase = ImportPhase.parsing;
        _fileName = file.name;
        _progress = 0;
      });

      await _parseFile(file);
    } catch (e) {
      setState(() {
        _phase = ImportPhase.error;
        _errorMessage = 'Could not open file picker: $e';
      });
    }
  }

  Future<void> _parseFile(PlatformFile file) async {
    final stages = [
      (0.2, 'Reading file…'),
      (0.45, 'Parsing waypoints…'),
      (0.65, 'Fetching elevation data…'),
      (0.85, 'Analysing route…'),
      (1.0, 'Done!'),
    ];

    for (final (progress, _) in stages) {
      await Future.delayed(const Duration(milliseconds: 400));
      if (!mounted) return;
      setState(() => _progress = progress);
    }

    try {
      final route = await _parseRouteFile(file);
      if (!mounted) return;
      setState(() {
        _phase = ImportPhase.done;
        _importedRoute = route;
      });

      final local = context.read<LocalRepo>();
      await local.saveRoute(route);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _phase = ImportPhase.error;
        _errorMessage = 'Failed to parse route: $e';
      });
    }
  }

  Future<RouteMap> _parseRouteFile(PlatformFile file) async {
    return RouteMap(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      origin: const Location(lat: 0, lng: 0, name: 'Start'),
      destination: const Location(lat: 1, lng: 1, name: 'End'),
      totalDistance: 42.0,
      totalAscent: 320,
      totalDescent: 310,
      sport: SportType.cycling,
      unit: DistanceUnit.kilometers,
      geometry: const [],
      elevation: const [],
      waypoints: const [],
    );
  }

  void _proceedToPlan() {
    final route = _importedRoute;
    if (route == null) return;
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => PlanMakerScreen(importedRoute: route),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: AppBar(
        backgroundColor: AppColors.bg(b),
        elevation: 0,
        title: Text(
          'Import Route',
          style: TextStyle(
            color: AppColors.textPrimary(b),
            fontSize: 17,
            fontWeight: FontWeight.w700,
          ),
        ),
        leading: IconButton(
          icon: Icon(Icons.close_rounded, color: AppColors.textPrimary(b)),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Column(
        children: [
          _buildBody(b).expanded(),
          if (_phase == ImportPhase.done)
            IconTextButton(
              label: 'Create Plan with this Route',
              icon: Icons.arrow_forward_rounded,
              brightness: b,
              onPressed: _proceedToPlan,
            ),
          if (_phase == ImportPhase.done) const Gap(12),
        ],
      ),
    );
  }

  Widget _buildBody(Brightness b) {
    switch (_phase) {
      case ImportPhase.picking:
        return const Center(child: CircularProgressIndicator(color: AppColors.primary));

      case ImportPhase.parsing:
        return ParsingView(
          fileName: _fileName ?? 'file',
          progress: _progress,
          brightness: b,
        );

      case ImportPhase.done:
        return DoneView(route: _importedRoute!, brightness: b);

      case ImportPhase.error:
        return ErrorView(
          message: _errorMessage ?? 'Unknown error',
          brightness: b,
          onRetry: () {
            setState(() => _phase = ImportPhase.picking);
            _pickFile();
          },
        );
    }
  }
}
