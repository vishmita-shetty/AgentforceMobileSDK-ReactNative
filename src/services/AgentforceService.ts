import { NativeModules, Platform } from 'react-native';

const { AgentforceModule } = NativeModules;

export interface ServiceAgentConfig {
  serviceApiURL: string;
  organizationId: string;
  esDeveloperName: string;
}

/**
 * Service class for interacting with native Agentforce SDK
 * Provides a simple JavaScript interface for Service Agent functionality
 */
class AgentforceService {
  /**
   * Configure Service Agent with required parameters
   * @param config Service Agent configuration object
   * @returns Promise<boolean> indicating success
   */
  async configure(config: ServiceAgentConfig): Promise<boolean> {
    if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
      console.warn('Agentforce only supported on Android and iOS');
      return false;
    }

    if (!AgentforceModule) {
      console.error('AgentforceModule native module not found');
      return false;
    }

    try {
      const result = await AgentforceModule.configure(
        config.serviceApiURL,
        config.organizationId,
        config.esDeveloperName
      );
      console.log('Agentforce configured successfully');
      return result;
    } catch (error) {
      console.error('Failed to configure Agentforce:', error);
      throw error;
    }
  }

  /**
   * Launch the Agentforce conversation UI
   * @returns Promise<boolean> indicating success
   */
  async launchConversation(): Promise<boolean> {
    if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
      console.warn('Agentforce only supported on Android and iOS');
      return false;
    }

    if (!AgentforceModule) {
      console.error('AgentforceModule native module not found');
      return false;
    }

    try {
      const result = await AgentforceModule.launchConversation();
      console.log('Agentforce conversation launched successfully');
      return result;
    } catch (error) {
      console.error('Failed to launch conversation:', error);
      throw error;
    }
  }

  /**
   * Check if Agentforce SDK is configured and ready
   * @returns Promise<boolean> indicating if configured
   */
  async isConfigured(): Promise<boolean> {
    if (Platform.OS !== 'android' && Platform.OS !== 'ios') {
      return false;
    }

    if (!AgentforceModule) {
      return false;
    }

    try {
      return await AgentforceModule.isConfigured();
    } catch (error) {
      console.error('Failed to check configuration:', error);
      return false;
    }
  }
}

export default new AgentforceService();

