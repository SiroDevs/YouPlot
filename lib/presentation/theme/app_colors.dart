import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';

class AppColors {
  // Primary brand — warm brown
  static const primary        = Color(0xFF7C4A2D);   // rich brown
  static const primaryLight   = Color(0xFFA0633E);   // lighter brown
  static const primaryDark    = Color(0xFF5C3320);   // deep brown
  static const primaryDim     = Color(0x337C4A2D);   // brown @ 20%

  // Dark mode surfaces
  static const bgDark         = Color(0xFF111111);
  static const surfaceDark    = Color(0xFF1A1A1A);
  static const cardDark       = Color(0xFF222222);
  static const borderDark     = Color(0xFF2E2E2E);

  // Light mode surfaces
  static const bgLight        = Color(0xFFF5F0EB);
  static const surfaceLight   = Color(0xFFFFFFFF);
  static const cardLight      = Color(0xFFFFFBF7);
  static const borderLight    = Color(0xFFE0D5CA);

  // Semantic
  static const danger         = Color(0xFFEF4444);
  static const success        = Color(0xFF10B981);
  static const warning        = Color(0xFFF59E0B);
  static const accent         = Color(0xFFFF6B35);

  // Sport colors (unchanged)
  static Color sport(SportType s) {
    switch (s) {
      case SportType.walking:  return const Color(0xFF60A5FA);
      case SportType.running:  return const Color(0xFFF87171);
      case SportType.cycling:  return const Color(0xFF34D399);
    }
  }

  // Context-aware helpers (call with Theme.of(ctx).brightness)
  static Color bg(Brightness b)       => b == Brightness.dark ? bgDark      : bgLight;
  static Color surface(Brightness b)  => b == Brightness.dark ? surfaceDark  : surfaceLight;
  static Color card(Brightness b)     => b == Brightness.dark ? cardDark     : cardLight;
  static Color border(Brightness b)   => b == Brightness.dark ? borderDark   : borderLight;
  static Color textPrimary(Brightness b)   => b == Brightness.dark ? const Color(0xFFEEEEEE) : const Color(0xFF1A1008);
  static Color textSecondary(Brightness b) => b == Brightness.dark ? const Color(0xFF888888) : const Color(0xFF6B5744);
  static Color textMuted(Brightness b)     => b == Brightness.dark ? const Color(0xFF444444) : const Color(0xFFBBA898);
}
