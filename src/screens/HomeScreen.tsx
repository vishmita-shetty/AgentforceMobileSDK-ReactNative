import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  Image,
  Platform,
  ScrollView,
} from 'react-native';
import AgentforceService from '../services/AgentforceService';

interface HomeScreenProps {
  navigation: any;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const [isConfigured, setIsConfigured] = useState(false);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    checkConfiguration();
  }, []);

  // Refresh configuration status when screen gains focus
  useEffect(() => {
    const unsubscribe = navigation.addListener('focus', () => {
      checkConfiguration();
    });
    return unsubscribe;
  }, [navigation]);

  const checkConfiguration = async () => {
    setIsChecking(true);
    try {
      const configured = await AgentforceService.isConfigured();
      setIsConfigured(configured);
    } catch (error) {
      console.error('Error checking configuration:', error);
      setIsConfigured(false);
    } finally {
      setIsChecking(false);
    }
  };

  const handleLaunchAgentforce = async () => {
    if (!isConfigured) {
      Alert.alert(
        'Configuration Required',
        'Please configure Service Agent settings before launching.',
        [
          { text: 'Cancel', style: 'cancel' },
          {
            text: 'Configure',
            onPress: () => navigation.navigate('Settings'),
          },
        ]
      );
      return;
    }

    try {
      await AgentforceService.launchConversation();
    } catch (error) {
      Alert.alert(
        'Error',
        'Failed to launch Agentforce conversation. Please check your configuration.'
      );
    }
  };

  return (
    <View style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        {/* Logo */}
        <View style={styles.logoContainer}>
          <Image
            source={require('../../agentforce-icon.png')}
            style={styles.logo}
          />
        </View>

        {/* Title */}
        <Text style={styles.title}>Agentforce Service Agent</Text>
        <Text style={styles.subtitle}>
          Your AI-powered assistant is ready to help
        </Text>

        {/* Feature Cards */}
        <View style={styles.featuresContainer}>
          <View style={styles.featureCard}>
            <Text style={styles.featureIcon}>💬</Text>
            <Text style={styles.featureTitle}>Service Agent</Text>
            <Text style={styles.featureDescription}>
              Lightweight AI chat for customer support
            </Text>
          </View>

          <View style={styles.featureCard}>
            <Text style={styles.featureIcon}>⚡</Text>
            <Text style={styles.featureTitle}>No Authentication</Text>
            <Text style={styles.featureDescription}>
              Simple 3-field configuration
            </Text>
          </View>

          <View style={styles.featureCard}>
            <Text style={styles.featureIcon}>🚀</Text>
            <Text style={styles.featureTitle}>Quick Setup</Text>
            <Text style={styles.featureDescription}>
              Configure once and start chatting
            </Text>
          </View>
        </View>

        {/* Status */}
        {isChecking ? (
          <View style={styles.statusContainer}>
            <Text style={styles.statusText}>Checking configuration...</Text>
          </View>
        ) : !isConfigured ? (
          <View style={[styles.statusContainer, styles.statusWarning]}>
            <Text style={styles.statusTextWarning}>
              ⚠️ Configuration required
            </Text>
          </View>
        ) : (
          <View style={[styles.statusContainer, styles.statusSuccess]}>
            <Text style={styles.statusTextSuccess}>✓ Ready to launch</Text>
          </View>
        )}

        {/* Launch Button */}
        <TouchableOpacity
          style={[
            styles.launchButton,
            !isConfigured && styles.launchButtonDisabled,
          ]}
          onPress={handleLaunchAgentforce}
          disabled={!isConfigured || isChecking}
        >
          <Text style={styles.launchButtonText}>
            {isConfigured ? 'Launch Agentforce' : 'Configure First'}
          </Text>
        </TouchableOpacity>

        {/* Settings Button */}
        <TouchableOpacity
          style={styles.settingsButton}
          onPress={() => navigation.navigate('Settings')}
        >
          <Text style={styles.settingsButtonText}>⚙️ Settings</Text>
        </TouchableOpacity>

        {/* Platform Info */}
        <Text style={styles.platformText}>
          Running on {Platform.OS === 'ios' ? 'iOS' : 'Android'}
        </Text>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 20,
    alignItems: 'center',
    paddingBottom: 40,
  },
  logoContainer: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: '#ffffff',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 5,
  },
  logo: {
    width: 80,
    height: 80,
    resizeMode: 'contain',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#212529',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#6c757d',
    textAlign: 'center',
    marginBottom: 32,
    paddingHorizontal: 20,
  },
  featuresContainer: {
    width: '100%',
    marginBottom: 24,
  },
  featureCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 20,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 3,
  },
  featureIcon: {
    fontSize: 32,
    marginBottom: 8,
  },
  featureTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#212529',
    marginBottom: 4,
  },
  featureDescription: {
    fontSize: 14,
    color: '#6c757d',
    lineHeight: 20,
  },
  statusContainer: {
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 8,
    marginBottom: 20,
    width: '100%',
    alignItems: 'center',
    backgroundColor: '#e7f3ff',
  },
  statusWarning: {
    backgroundColor: '#fff3cd',
  },
  statusSuccess: {
    backgroundColor: '#d1e7dd',
  },
  statusText: {
    fontSize: 14,
    color: '#0070f3',
    fontWeight: '500',
  },
  statusTextWarning: {
    fontSize: 14,
    color: '#856404',
    fontWeight: '500',
  },
  statusTextSuccess: {
    fontSize: 14,
    color: '#0f5132',
    fontWeight: '500',
  },
  launchButton: {
    backgroundColor: '#0176D3',
    paddingHorizontal: 40,
    paddingVertical: 16,
    borderRadius: 8,
    width: '100%',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 4,
  },
  launchButtonDisabled: {
    backgroundColor: '#ccc',
  },
  launchButtonText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  settingsButton: {
    marginTop: 16,
    padding: 12,
  },
  settingsButtonText: {
    fontSize: 16,
    color: '#0176D3',
    fontWeight: '600',
  },
  platformText: {
    marginTop: 20,
    fontSize: 12,
    color: '#adb5bd',
  },
});

export default HomeScreen;

