import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;
import 'package:permission_handler/permission_handler.dart';

import '../models/event_model.dart';
import '../repositories/incident_repository.dart';
import '../services/connectivity_service.dart';
import '../services/incident_service.dart';
import '../services/location_service.dart';
import '../viewmodels/auth_viewmodel.dart';
import '../viewmodels/incident_viewmodel.dart';
import '../widgets/input_field.dart';
import '../widgets/loading_indicator.dart';
import '../widgets/offline_banner.dart';
import '../widgets/primary_button.dart';
import '../widgets/section_title.dart';
import 'success_screen.dart';

class IncidentReportScreen extends StatelessWidget {
  const IncidentReportScreen({super.key});

  static const routeName = '/incident-report';

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => IncidentViewModel(
        incidentRepository: IncidentRepository(incidentService: IncidentService()),
        locationService: LocationService(),
      ),
      child: const _IncidentReportView(),
    );
  }
}

class _IncidentReportView extends StatefulWidget {
  const _IncidentReportView();

  @override
  State<_IncidentReportView> createState() => _IncidentReportViewState();
}

class _IncidentReportViewState extends State<_IncidentReportView> {
  static const List<String> _descriptionSuggestions = [
    'I saw a gun',
    'I saw a person being assaulted',
    'I saw a fight',
    'I saw someone injured',
    'I saw smoke',
    'I saw a fire',
    'Someone is unconscious',
    'Someone is being assaulted',
    'Someone is feeling unwell',
    'Someone is threatening people',
    'People are pushing',
    'People cannot move',
    'People are fighting',
    'People are trapped in the crowd',
  ];

  final TextEditingController _descriptionController = TextEditingController();
  final stt.SpeechToText _speech = stt.SpeechToText();
  bool _isListening = false;
  String _lastWords = '';
  TextEditingController? _autocompleteController;
  
  String _selectedLocaleId = 'en-IN';
  final List<Map<String, String>> _locales = [
    {'id': 'en-IN', 'name': 'English'},
    {'id': 'hi-IN', 'name': 'Hindi'},
    {'id': 'mr-IN', 'name': 'Marathi'},
  ];

  @override
  void dispose() {
    _descriptionController.dispose();
    _speech.cancel();
    super.dispose();
  }

  Future<void> _startListening() async {
    final hasPermission = await Permission.microphone.request().isGranted;
    if (!hasPermission) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Microphone permission denied. You can still type.')),
        );
      }
      return;
    }

    final available = await _speech.initialize(
      onStatus: (status) {
        if (status == 'done' || status == 'notListening') {
          if (mounted) setState(() => _isListening = false);
        }
      },
      onError: (errorNotification) {
        if (mounted) setState(() => _isListening = false);
      },
    );

    if (available) {
      if (mounted) setState(() => _isListening = true);
      await _speech.listen(
        localeId: _selectedLocaleId,
        onResult: (result) {
          if (mounted) {
            setState(() {
              _lastWords = result.recognizedWords;
              if (_autocompleteController != null) {
                // Update autocomplete controller to show in text field
                // Ensure cursor moves to end
                _autocompleteController!.value = TextEditingValue(
                  text: _lastWords,
                  selection: TextSelection.collapsed(offset: _lastWords.length),
                );
              } else {
                _descriptionController.text = _lastWords;
              }
            });
          }
        },
      );
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Speech recognition is unavailable on this device.')),
        );
      }
    }
  }

  Future<void> _stopListening() async {
    await _speech.stop();
    if (mounted) setState(() => _isListening = false);
  }

  @override
  Widget build(BuildContext context) {
    final event = ModalRoute.of(context)?.settings.arguments as EventModel?;
    final vm = context.watch<IncidentViewModel>();
    final isOnline = context.watch<ConnectivityService>().isOnline;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Report Incident', style: TextStyle(fontWeight: FontWeight.w700)),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: SafeArea(
        child: Column(
          children: [
            const OfflineBanner(),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    SectionTitle(
                      title: 'Quick Incident Report',
                      subtitle: event == null
                          ? 'Share what happened to alert organizers quickly.'
                          : 'Reporting for ${event.name}',
                    ),
                    const SizedBox(height: 24),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Describe what you saw? (Optional)',
                          style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                        ),
                        const SizedBox(height: 8),
                        Autocomplete<String>(
                          optionsBuilder: (TextEditingValue textEditingValue) {
                            if (textEditingValue.text.isEmpty) {
                              return const Iterable<String>.empty();
                            }
                            final query = textEditingValue.text.toLowerCase();
                            return _descriptionSuggestions.where(
                              (suggestion) => suggestion.toLowerCase().contains(query),
                            );
                          },
                          onSelected: (String selection) {
                            _descriptionController.text = selection;
                          },
                          fieldViewBuilder: (context, textEditingController, focusNode, onFieldSubmitted) {
                            _autocompleteController = textEditingController;
                            // Link our local controller so it stays in sync
                            textEditingController.addListener(() {
                              _descriptionController.text = textEditingController.text;
                            });
                            return TextField(
                              controller: textEditingController,
                              focusNode: focusNode,
                              maxLines: 4,
                              decoration: const InputDecoration(
                                hintText: 'I saw...',
                              ),
                            );
                          },
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Row(
                        children: [
                          OutlinedButton.icon(
                            onPressed: _isListening ? _stopListening : _startListening,
                            icon: Icon(
                              _isListening ? Icons.mic : Icons.mic_none,
                              color: _isListening ? Colors.red : null,
                            ),
                            label: Text(_isListening ? 'Listening...' : 'Speak'),
                            style: OutlinedButton.styleFrom(
                              foregroundColor: _isListening ? Colors.red : Theme.of(context).colorScheme.primary,
                              side: BorderSide(
                                color: _isListening ? Colors.red : Theme.of(context).colorScheme.primary,
                              ),
                            ),
                          ),
                          const SizedBox(width: 16),
                          DropdownButton<String>(
                            value: _selectedLocaleId,
                            underline: const SizedBox(),
                            icon: const Icon(Icons.arrow_drop_down, color: Colors.grey),
                            items: _locales.map((locale) {
                              return DropdownMenuItem<String>(
                                value: locale['id'],
                                child: Text(locale['name']!),
                              );
                            }).toList(),
                            onChanged: (val) {
                              if (val != null) setState(() => _selectedLocaleId = val);
                            },
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                    if (vm.errorMessage != null)
                      Text(
                        vm.errorMessage!,
                        style: TextStyle(
                          color: Theme.of(context).colorScheme.error,
                          fontSize: 13,
                        ),
                      ),
                    const SizedBox(height: 16),
                    vm.isSubmitting
                        ? const LoadingIndicator(label: 'Submitting incident...')
                        : Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              PrimaryButton(
                                label: 'Submit Report',
                                icon: Icons.send_rounded,
                                onPressed: isOnline ? () => _submit(vm, event) : null,
                              ),
                              const SizedBox(height: 12),
                              TextButton(
                                onPressed: isOnline ? () {
                                  _descriptionController.clear();
                                  _submit(vm, event);
                                } : null,
                                child: const Text('Skip Description & Submit'),
                              ),
                            ],
                          ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _submit(IncidentViewModel vm, EventModel? event) async {
    if (event == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select an event before reporting.')),
      );
      return;
    }
    
    HapticFeedback.lightImpact();

    final auth = context.read<AuthViewModel>();
    final name = auth.fullName ?? 'Anonymous';
    final phone = auth.phoneNumber ?? '0000000000';
    
final success = await vm.submitIncident(
  eventId: event.id,
  name: name,
  phoneNumber: phone,
  description: _descriptionController.text.trim(),
);

if (!mounted || !success) return;

Navigator.pushReplacementNamed(
  context,
  SuccessScreen.routeName,
  arguments: {
    'incidentId': vm.lastSubmittedIncidentId,
  },
);
  }
}
