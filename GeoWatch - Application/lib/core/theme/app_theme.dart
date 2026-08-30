import 'package:flutter/material.dart';

import 'dark_theme.dart';
import 'light_theme.dart';

class AppTheme {
  static const Color primary = Color(0xFF06141B);
  static const Color secondary = Color(0xFF11212D);
  static const Color accent = Color(0xFFF43F5E); // Pink/emergency accent
  static const Color cardLight = Color(0xFFF4F6F8);
  static const Color cardDark = Color(0xFF11212D);
  static const Color surfaceLighter = Color(0xFF253745);
  static const Color textSecondary = Color(0xFF4A5C6A);
  static const Color alert = Color(0xFFE74C3C);
  static const Color lowRisk = Color(0xFFFBBF24);
  static const Color mediumRisk = Color(0xFFF97316);
  static const Color highRisk = alert;

  static const LinearGradient navyGradient = LinearGradient(
    colors: [primary, secondary],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static ThemeData get light => LightTheme.theme;
  static ThemeData get dark => DarkTheme.theme;
}
