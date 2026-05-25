import 'package:flutter/material.dart';
import 'package:gap/gap.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../../../core/constants/app_constants.dart';
import '../../theme/app_colors.dart';
import 'widgets/general.dart';
import 'widgets/theme_tile.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late SharedPreferences _prefs;
  SportType _sport = SportType.cycling;
  DistanceUnit _unit = DistanceUnit.kilometers;
  bool _loaded = false;

  static const _kSportKey = 'sport_pref_v1';
  static const _kUnitKey = 'unit_pref_v1';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _prefs = prefs;
      _sport = SportType.values.firstWhere(
        (s) => s.name == prefs.getString(_kSportKey),
        orElse: () => SportType.cycling,
      );
      _unit = DistanceUnit.values.firstWhere(
        (u) => u.name == prefs.getString(_kUnitKey),
        orElse: () => DistanceUnit.kilometers,
      );
      _loaded = true;
    });
  }

  void _setSport(SportType s) {
    setState(() => _sport = s);
    _prefs.setString(_kSportKey, s.name);
  }

  void _setUnit(DistanceUnit u) {
    setState(() => _unit = u);
    _prefs.setString(_kUnitKey, u.name);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final b = theme.brightness;

    return Scaffold(
      backgroundColor: AppColors.bg(b),
      appBar: AppBar(
        backgroundColor: AppColors.bg(b),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: AppColors.textPrimary(b)),
          onPressed: () => Navigator.pop(context),
        ),
        title: Row(children: [
          Container(
            width: 26, height: 26,
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(6),
            ),
            child: const Icon(Icons.settings_rounded, size: 15, color: Colors.white),
          ),
          const Gap(10),
          Text('Settings', style: theme.textTheme.titleLarge),
        ]),
      ),
      body: !_loaded
          ? const Center(child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.primary))
          : ListView(
              padding: const EdgeInsets.all(20),
              children: [
                SectionHeader(label: 'Appearance', brightness: b),
                const Gap(12),
                SettingsCard(
                  brightness: b,
                  children: [ThemeTile(brightness: b)],
                ),

                const Gap(24),

                SectionHeader(label: 'Units', brightness: b),
                const Gap(12),
                SettingsCard(
                  brightness: b,
                  children: [
                    DropdownTile<DistanceUnit>(
                      brightness: b,
                      icon: Icons.straighten_rounded,
                      label: 'Distance unit',
                      value: _unit,
                      items: DistanceUnit.values,
                      itemLabel: (u) => u == DistanceUnit.kilometers
                          ? 'Kilometers (km)'
                          : 'Miles (mi)',
                      onChanged: (u) { if (u != null) _setUnit(u); },
                    ),
                  ],
                ),

                const Gap(24),

                SectionHeader(label: 'Activity', brightness: b),
                const Gap(12),
                SettingsCard(
                  brightness: b,
                  children: [
                    DropdownTile<SportType>(
                      brightness: b,
                      icon: Icons.directions_run_rounded,
                      label: 'Default sport',
                      value: _sport,
                      items: SportType.values,
                      itemLabel: (s) => '${s.emoji}  ${s.label}',
                      onChanged: (s) { if (s != null) _setSport(s); },
                    ),
                  ],
                ),

                const Gap(40),

                Center(
                  child: Text(
                    'YouPlot v1.0.0',
                    style: TextStyle(fontSize: 11, color: AppColors.textMuted(b)),
                  ),
                ),
                const Gap(20),
              ],
            ),
    );
  }
}
