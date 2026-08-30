import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodels/auth_viewmodel.dart';
import 'registration_screen.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  static const routeName = '/settings';

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthViewModel>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile & Settings', style: TextStyle(fontWeight: FontWeight.w700)),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          children: [
            Center(
              child: Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.1),
                  border: Border.all(color: Colors.white10),
                ),
                child: const Icon(Icons.person_rounded, size: 64, color: Colors.white70),
              ),
            ),
            const SizedBox(height: 24),
            Text('Account Information', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    _buildInfoRow(context, Icons.badge_rounded, 'Name', auth.fullName ?? "Not Registered"),
                    const Divider(color: Colors.white10, height: 24),
                    _buildInfoRow(context, Icons.phone_rounded, 'Phone', auth.phoneNumber ?? "-"),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            Text('App Details', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    _buildInfoRow(context, Icons.shield_rounded, 'Product', 'MOBALERT'),
                    const Divider(color: Colors.white10, height: 24),
                    _buildInfoRow(context, Icons.info_outline_rounded, 'Version', '1.0.0'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 32),
            FilledButton.tonalIcon(
              onPressed: () async {
                await context.read<AuthViewModel>().logout();
                if (!context.mounted) return;
                Navigator.pushNamedAndRemoveUntil(
                  context,
                  RegistrationScreen.routeName,
                  (_) => false,
                );
              },
              icon: const Icon(Icons.logout),
              label: const Text('Logout'),
              style: FilledButton.styleFrom(
                backgroundColor: Colors.white10,
                foregroundColor: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(BuildContext context, IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 20, color: Colors.white54),
        const SizedBox(width: 12),
        Text(label, style: const TextStyle(color: Colors.white54)),
        const Spacer(),
        Text(value, style: const TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
      ],
    );
  }
}
