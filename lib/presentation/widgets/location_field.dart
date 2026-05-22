import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/entities.dart';
import '../bloc/location_search/location_search_bloc.dart';
import '../theme/app_colors.dart';

class LocationField extends StatefulWidget {
  final String hint;
  final Location? value;
  final bool showGps;
  final ValueChanged<Location> onSelected;

  const LocationField({
    super.key,
    required this.hint,
    required this.onSelected,
    this.value,
    this.showGps = false,
  });

  @override
  State<LocationField> createState() => _LocationFieldState();
}

class _LocationFieldState extends State<LocationField> {
  final _ctrl = TextEditingController();
  final _focus = FocusNode();
  Timer? _debounce;
  bool _showDropdown = false;

  @override
  void initState() {
    super.initState();
    if (widget.value?.name != null) _ctrl.text = widget.value!.name!;
    _focus.addListener(() {
      if (!_focus.hasFocus) setState(() => _showDropdown = false);
    });
  }

  @override
  void didUpdateWidget(LocationField old) {
    super.didUpdateWidget(old);
    if (widget.value?.name != null && widget.value != old.value) {
      _ctrl.text = widget.value!.name!;
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    _focus.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onChanged(String q) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 350), () {
      context.read<LocationSearchBloc>().add(QueryChanged(q));
      setState(() => _showDropdown = q.length >= 2);
    });
  }

  @override
  Widget build(BuildContext context) {
    final b = Theme.of(context).brightness;
    return BlocListener<LocationSearchBloc, LocationSearchState>(
      listener: (ctx, s) {
        if (s.currentLocation != null && widget.showGps) {
          widget.onSelected(s.currentLocation!);
          _ctrl.text = s.currentLocation!.name ?? 'Current location';
          setState(() => _showDropdown = false);
          ctx.read<LocationSearchBloc>().add(ClearSearch());
        }
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: _ctrl,
            focusNode: _focus,
            onChanged: _onChanged,
            style: TextStyle(
                color: AppColors.textPrimary(b), fontSize: 14),
            decoration: InputDecoration(
              hintText: widget.hint,
              prefixIcon:
                  const Icon(Icons.search_rounded, size: 17),
              suffixIcon: widget.showGps
                  ? BlocBuilder<LocationSearchBloc, LocationSearchState>(
                      builder: (_, s) => s.locating
                          ? const Padding(
                              padding: EdgeInsets.all(12),
                              child: SizedBox(
                                width: 16,
                                height: 16,
                                child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: AppColors.primary),
                              ),
                            )
                          : IconButton(
                              icon: const Icon(
                                  Icons.my_location_rounded,
                                  size: 17),
                              onPressed: () => context
                                  .read<LocationSearchBloc>()
                                  .add(LocateMe()),
                            ),
                    )
                  : null,
            ),
          ),
          if (_showDropdown)
            BlocBuilder<LocationSearchBloc, LocationSearchState>(
              builder: (_, s) {
                if (s.loading) {
                  return Padding(
                    padding: const EdgeInsets.all(12),
                    child: Center(
                        child: CircularProgressIndicator(
                            strokeWidth: 2,
                            color: AppColors.primary)),
                  );
                }
                if (s.results.isEmpty) return const SizedBox.shrink();
                return Container(
                  margin: const EdgeInsets.only(top: 4),
                  decoration: BoxDecoration(
                    color: AppColors.surface(b),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(
                        color: AppColors.border(b), width: 0.5),
                    boxShadow: [
                      BoxShadow(
                          color: Colors.black.withValues(alpha: 0.2),
                          blurRadius: 12,
                          offset: const Offset(0, 4))
                    ],
                  ),
                  child: ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: s.results.length.clamp(0, 6),
                    separatorBuilder: (_, __) => Divider(
                        height: 0, color: AppColors.border(b)),
                    itemBuilder: (_, i) {
                      final loc = s.results[i];
                      return ListTile(
                        dense: true,
                        contentPadding: const EdgeInsets.symmetric(
                            horizontal: 14, vertical: 2),
                        leading: Icon(Icons.place_outlined,
                            size: 16,
                            color: AppColors.textSecondary(b)),
                        title: Text(loc.name ?? '',
                            style: TextStyle(
                                fontSize: 13,
                                color: AppColors.textPrimary(b))),
                        subtitle: loc.address != null
                            ? Text(loc.address!,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                    fontSize: 11,
                                    color:
                                        AppColors.textSecondary(b)))
                            : null,
                        onTap: () {
                          widget.onSelected(loc);
                          _ctrl.text =
                              loc.name ?? loc.address ?? '';
                          _focus.unfocus();
                          setState(() => _showDropdown = false);
                          context
                              .read<LocationSearchBloc>()
                              .add(ClearSearch());
                        },
                      );
                    },
                  ),
                );
              },
            ),
        ],
      ),
    );
  }
}
