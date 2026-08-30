class OrganizerModel {
  final String name;
  final String phoneNumber;

  OrganizerModel({
    required this.name,
    required this.phoneNumber,
  });

  factory OrganizerModel.fromJson(Map<String, dynamic> json) {
    return OrganizerModel(
      name: json['name'] ?? 'Unknown Organizer',
      phoneNumber: json['phoneNumber'] ?? '',
    );
  }
}
