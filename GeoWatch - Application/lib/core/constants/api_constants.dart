class ApiConstants {
  static const String baseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: 'http://localhost:8080');

  static const String nearbyEvents = '/api/events/nearby';
  static const String incidents = '/api/incidents';
}