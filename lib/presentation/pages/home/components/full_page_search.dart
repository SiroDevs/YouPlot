import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:gap/gap.dart';

import '../../../../domain/entities/location.dart';
import '../../../bloc/location_search/location_search_bloc.dart';
import '../../../theme/app_colors.dart';
import 'idle_search_view.dart';
import 'map_point_picker.dart';

class FullPageSearch extends StatefulWidget {
  final String hint;
  final bool showGps;
  final Brightness brightness;

  const FullPageSearch({
    super.key,
    required this.hint,
    required this.brightness,
    this.showGps = false,
  });

  @override
  State<FullPageSearch> createState() => _FullPageSearchState();
}

class _FullPageSearchState extends State<FullPageSearch> {
  final _ctrl = TextEditingController();
  final _focus = FocusNode();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _focus.requestFocus());
    _ctrl.addListener(_onTextChanged);
  }

  @override
  void dispose() {
    _ctrl.removeListener(_onTextChanged);
    _ctrl.dispose();
    _focus.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    // Keep suffix button in sync
    setState(() {});
    final q = _ctrl.text;
    context.read<LocationSearchBloc>().add(QueryChanged(q));
  }

  /// Called when a result is tapped — saves to history then pops.
  void _select(location) {
    context.read<LocationSearchBloc>()
      ..add(SaveToHistory(location))
      ..add(ClearSearch());
    Navigator.pop(context, location);
  }

  @override
  Widget build(BuildContext context) {
    final b = widget.brightness;
    final isDark = b == Brightness.dark;
    final bgColor = isDark ? const Color(0xFF111111) : Colors.white;

    return Scaffold(
      backgroundColor: bgColor,
      body: Column(
        children: [
          // ── Search bar ──────────────────────────────────────────────────
          SafeArea(
            child: Container(
              color: bgColor,
              padding: const EdgeInsets.fromLTRB(12, 12, 12, 8),
              child: Row(
                children: [
                  // Back
                  GestureDetector(
                    onTap: () {
                      context.read<LocationSearchBloc>().add(ClearSearch());
                      Navigator.pop(context);
                    },
                    child: Container(
                      width: 40, height: 40,
                      decoration: BoxDecoration(
                        color: AppColors.surface(b),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(Icons.arrow_back_rounded,
                          size: 20, color: AppColors.textPrimary(b)),
                    ),
                  ),
                  const Gap(10),

                  // Input
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(
                        color: AppColors.surface(b),
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(
                            color: AppColors.border(b), width: 0.5),
                        boxShadow: [
                          BoxShadow(
                              color: Colors.black.withValues(alpha: 0.08),
                              blurRadius: 8),
                        ],
                      ),
                      child: TextField(
                        controller: _ctrl,
                        focusNode: _focus,
                        style: TextStyle(
                            fontSize: 15, color: AppColors.textPrimary(b)),
                        textInputAction: TextInputAction.search,
                        decoration: InputDecoration(
                          hintText: widget.hint,
                          hintStyle: TextStyle(
                              color: AppColors.textMuted(b), fontSize: 15),
                          prefixIcon: Icon(Icons.search_rounded,
                              size: 20, color: AppColors.textMuted(b)),
                          suffixIcon: _ctrl.text.isNotEmpty
                              ? IconButton(
                                  icon: Icon(Icons.close_rounded,
                                      size: 18,
                                      color: AppColors.textMuted(b)),
                                  onPressed: () {
                                    _ctrl.clear();
                                    context
                                        .read<LocationSearchBloc>()
                                        .add(ClearSearch());
                                  },
                                )
                              : null,
                          border: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 12),
                        ),
                      ),
                    ),
                  ),

                  // GPS button (only on "Starting point" field)
                  if (widget.showGps) ...[
                    const Gap(10),
                    BlocBuilder<LocationSearchBloc, LocationSearchState>(
                      buildWhen: (p, c) => p.locating != c.locating,
                      builder: (_, s) => GestureDetector(
                        onTap: s.locating
                            ? null
                            : () => context
                                .read<LocationSearchBloc>()
                                .add(LocateMe()),
                        child: Container(
                          width: 40, height: 40,
                          decoration: BoxDecoration(
                            color: s.locating
                                ? AppColors.primaryDim
                                : AppColors.surface(b),
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: s.locating
                                  ? AppColors.primary
                                  : AppColors.border(b),
                              width: 0.5,
                            ),
                          ),
                          child: s.locating
                              ? Padding(
                                  padding: const EdgeInsets.all(10),
                                  child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: AppColors.primary),
                                )
                              : Icon(Icons.my_location_rounded,
                                  size: 20, color: AppColors.primary),
                        ),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),

          // ── Pick on map quick action ────────────────────────────────
          _PickOnMapTile(brightness: b, onSelected: _select),

          Divider(height: 0, color: AppColors.border(b)),

          // ── Results / idle view ─────────────────────────────────────────
          Expanded(
            child: BlocConsumer<LocationSearchBloc, LocationSearchState>(
              listenWhen: (p, c) =>
                  p.currentLocation != c.currentLocation &&
                  c.currentLocation != null,
              listener: (_, s) {
                if (s.currentLocation != null) {
                  _select(s.currentLocation!);
                }
              },
              builder: (_, s) {
                // Loading spinner
                if (s.loading) {
                  return Center(
                    child: CircularProgressIndicator(
                        strokeWidth: 2, color: AppColors.primary),
                  );
                }

                // Idle — show history (or empty state on first use)
                if (_ctrl.text.length < 2) {
                  return IdleSearchView(
                    brightness: b,
                    onQueryFill: (q) {
                      _ctrl.text = q;
                      _ctrl.selection = TextSelection.fromPosition(
                        TextPosition(offset: q.length),
                      );
                    },
                  );
                }

                // No results
                if (s.results.isEmpty) {
                  return Center(
                    child: Column(mainAxisSize: MainAxisSize.min, children: [
                      Icon(Icons.search_off_rounded,
                          size: 48, color: AppColors.textMuted(b)),
                      const Gap(12),
                      Text('No results for "${_ctrl.text}"',
                          style: TextStyle(
                              color: AppColors.textSecondary(b),
                              fontSize: 14)),
                    ]),
                  );
                }

                // Results list
                return ListView.separated(
                  itemCount: s.results.length,
                  separatorBuilder: (_, __) =>
                      Divider(height: 0, color: AppColors.border(b)),
                  itemBuilder: (_, i) {
                    final loc = s.results[i];
                    return ListTile(
                      contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20, vertical: 8),
                      leading: Container(
                        width: 40, height: 40,
                        decoration: BoxDecoration(
                          color: AppColors.surface(b),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(Icons.place_rounded,
                            size: 20, color: AppColors.primary),
                      ),
                      title: Text(loc.name ?? '',
                          style: TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w500,
                              color: AppColors.textPrimary(b))),
                      subtitle: loc.address != null
                          ? Text(loc.address!,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                  fontSize: 12,
                                  color: AppColors.textSecondary(b)))
                          : null,
                      trailing: Icon(Icons.north_west_rounded,
                          size: 16, color: AppColors.textMuted(b)),
                      onTap: () => _select(loc),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// "Pick a point on the map" quick-action tile shown above the search results
// ─────────────────────────────────────────────────────────────────────────────

class _PickOnMapTile extends StatelessWidget {
  final Brightness brightness;
  final void Function(Location) onSelected;

  const _PickOnMapTile({required this.brightness, required this.onSelected});

  @override
  Widget build(BuildContext context) {
    final b = brightness;
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: AppColors.primaryDim,
          shape: BoxShape.circle,
        ),
        child: const Icon(Icons.touch_app_rounded,
            size: 20, color: AppColors.primary),
      ),
      title: Text(
        'Pick a point on the map',
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w600,
          color: AppColors.textPrimary(b),
        ),
      ),
      subtitle: Text(
        'Tap anywhere to drop a pin',
        style: TextStyle(fontSize: 12, color: AppColors.textSecondary(b)),
      ),
      trailing: Icon(Icons.arrow_forward_ios_rounded,
          size: 14, color: AppColors.textMuted(b)),
      onTap: () async {
        // Clear any existing search first
        context.read<LocationSearchBloc>().add(ClearSearch());

        final loc = await Navigator.push<Location>(
          context,
          PageRouteBuilder(
            pageBuilder: (_, __, ___) =>
                MapPointPicker(brightness: b),
            transitionsBuilder: (_, anim, __, child) => FadeTransition(
              opacity: anim,
              child: child,
            ),
            transitionDuration: const Duration(milliseconds: 240),
          ),
        );

        if (loc != null) {
          onSelected(loc);
        }
      },
    );
  }
}
