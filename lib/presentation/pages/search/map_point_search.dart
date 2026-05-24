import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../bloc/location_search/location_search_bloc.dart';
import '../../theme/app_colors.dart';
import 'components/idle_search_view.dart';
import 'components/pick_on_map_tile.dart';
import 'components/search_header.dart';
import 'components/search_results_list.dart';

class MapPointSearch extends StatefulWidget {
  final String hint;
  final bool showGps;
  final Brightness brightness;

  const MapPointSearch({
    super.key,
    required this.hint,
    required this.brightness,
    this.showGps = false,
  });

  @override
  State<MapPointSearch> createState() => _MapPointSearchState();
}

class _MapPointSearchState extends State<MapPointSearch> {
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
    setState(() {});
    final q = _ctrl.text;
    context.read<LocationSearchBloc>().add(QueryChanged(q));
  }

  // ignore: strict_top_level_inference
  void _select(location) {
    context.read<LocationSearchBloc>()
      ..add(SaveToHistory(location))
      ..add(ClearSearch());
    Navigator.pop(context, location);
  }

  void _clearSearch() {
    _ctrl.clear();
    context.read<LocationSearchBloc>().add(ClearSearch());
  }

  void _onQueryFill(String query) {
    _ctrl.text = query;
    _ctrl.selection = TextSelection.fromPosition(
      TextPosition(offset: query.length),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = widget.brightness == Brightness.dark;
    final bgColor = isDark ? const Color(0xFF111111) : Colors.white;

    return Scaffold(
      backgroundColor: bgColor,
      body: Column(
        children: [
          SearchHeader(
            controller: _ctrl,
            focusNode: _focus,
            hint: widget.hint,
            brightness: widget.brightness,
            showGps: widget.showGps,
            onBack: () {
              context.read<LocationSearchBloc>().add(ClearSearch());
              Navigator.pop(context);
            },
            onClear: _clearSearch,
          ),
          PickOnMapTile(brightness: widget.brightness, onSelected: _select),
          Divider(height: 0, color: AppColors.border(widget.brightness)),
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
              builder: (_, state) {
                if (state.loading) {
                  return const Center(
                    child: CircularProgressIndicator(strokeWidth: 2),
                  );
                }

                if (_ctrl.text.length < 2) {
                  return IdleSearchView(
                    brightness: widget.brightness,
                    onQueryFill: _onQueryFill,
                  );
                }

                return SearchResultsList(
                  results: state.results,
                  query: _ctrl.text,
                  brightness: widget.brightness,
                  onSelect: _select,
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}