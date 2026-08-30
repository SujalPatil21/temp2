class IncidentRequest {
  IncidentRequest({
    required this.eventId,
    required this.name,
    required this.phoneNumber,
    required this.latitude,
    required this.longitude,
    required this.timestamp,
    this.description,
  });

  final int eventId;
  final String name;
  final String phoneNumber;
  final double latitude;
  final double longitude;
  final DateTime timestamp;
  final String? description;

  Map<String, dynamic> toJson() {
    return {
      'eventId': eventId,
      'name': name,
      'phoneNumber': phoneNumber,
      'latitude': latitude,
      'longitude': longitude,
      'timestamp': timestamp.toIso8601String(),
      if (description != null && description!.isNotEmpty) 'description': description,
    };
  }
}
