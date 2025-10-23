/*
 * Copyright (c) 2020-present, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import React from 'react';
import {
    StyleSheet,
    Text,
    View,
    FlatList,
    TouchableOpacity,
    Modal,
    Alert,
    NativeModules,
    Image,
    TextInput,
} from 'react-native';

import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { oauth, net } from 'react-native-force';

const { AgentforceManager } = NativeModules;

interface Response {
    records: Record[]
}

interface Record {
    Id: String,
    Name: String
}

interface Props {
}

interface State {
    data: Record[],
    showAgentforceModal: boolean,
    agentforceInitialized: boolean,
    agentId: string,
    isAgentIdEditable: boolean,
    orgId: string
}

class ContactListScreen extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            data: [],
            showAgentforceModal: false,
            agentforceInitialized: false,
            agentId: "0XxEE0000001CDd0AM",
            isAgentIdEditable: false,
            orgId: "00DEE000000XQRx"
        };
    }

    componentDidMount() {
        var that = this;
        oauth.getAuthCredentials(
            () => {
                that.fetchData();
                that.initializeAgentforce();
            }, // already logged in
            () => {
                oauth.authenticate(
                    () => {
                        that.fetchData();
                        that.initializeAgentforce();
                    },
                    (error) => console.log('Failed to authenticate:' + error)
                );
            });
    }

    fetchData() {
        var that = this;
        net.query('SELECT Id, Name FROM Contact LIMIT 100',
            (response: Response) => that.setState({ data: response.records }),
            (error) => console.log('Failed to query:' + error)
        );
    }

    initializeAgentforce() {
        oauth.getAuthCredentials(
            () => {
                // Get current user info from SalesforceSDK
                net.query('SELECT Id, Name FROM Organization LIMIT 1',
                    (response: any) => {
                        const orgId = response.records[0]?.Id;
                        if (orgId) {
                            this.setState({ orgId: orgId });
                            const config = {
                                agentId: this.state.agentId,
                                orgId: orgId,
                                endpoint: "https://YOUR_DOMAIN.my.salesforce.com" // Replace with your Salesforce domain
                            };

                            AgentforceManager.initializeAgentforce(config)
                                .then(() => {
                                    console.log('Agentforce initialized successfully');
                                    this.setState({ agentforceInitialized: true });
                                })
                                .catch((error: any) => {
                                    console.error('Failed to initialize Agentforce:', error);
                                    Alert.alert('Initialization Error', 'Failed to initialize Agentforce: ' + error.message);
                                });
                        } else {
                            console.error('Could not get organization ID');
                        }
                    },
                    (error) => console.error('Failed to get organization info:', error)
                );
            },
            () => console.log('User not authenticated')
        );
    }

    openAgentforceModal = () => {
        this.setState({ showAgentforceModal: true });
    }

    closeAgentforceModal = () => {
        this.setState({ showAgentforceModal: false });
    }

    toggleAgentIdEdit = () => {
        this.setState({ isAgentIdEditable: !this.state.isAgentIdEditable });
    }

    handleAgentIdChange = (text: string) => {
        this.setState({ agentId: text });
    }

    launchAgentforceSDK = () => {
        if (!this.state.agentforceInitialized) {
            console.log('Agentforce is not initialized yet. Please wait a moment and try again.');
            return;
        }

        if (!this.state.agentId || this.state.agentId.trim() === '') {
            Alert.alert('Error', 'Please enter a valid Agent ID before launching Agentforce.');
            return;
        }

        AgentforceManager.presentAgentforceChatView(this.state.agentId)
            .then(() => {
                console.log('Agentforce chat view presented successfully');
            })
            .catch((error: any) => {
                console.error('Failed to present Agentforce chat:', error);
            });
    }

    render() {
        return (
            <View style={styles.container}>
                <View style={styles.agentIdContainer}>
                    <Text style={styles.agentIdLabel}>Agent ID:</Text>
                    {this.state.isAgentIdEditable ? (
                        <View style={styles.agentIdInputRow}>
                            <TextInput
                                style={styles.agentIdInput}
                                value={this.state.agentId}
                                onChangeText={this.handleAgentIdChange}
                                placeholder="Enter Agent ID"
                                placeholderTextColor="#999"
                            />
                            <TouchableOpacity
                                style={styles.editButton}
                                onPress={this.toggleAgentIdEdit}
                            >
                                <Text style={styles.editButtonText}>✓</Text>
                            </TouchableOpacity>
                        </View>
                    ) : (
                        <View style={styles.agentIdDisplayRow}>
                            <Text style={styles.agentIdDisplay}>{this.state.agentId}</Text>
                            <TouchableOpacity
                                style={styles.editButton}
                                onPress={this.toggleAgentIdEdit}
                            >
                                <Text style={styles.editButtonText}>✎</Text>
                            </TouchableOpacity>
                        </View>
                    )}
                </View>

                <View style={styles.orgIdContainer}>
                    <Text style={styles.orgIdLabel}>Organization ID:</Text>
                    <Text style={styles.orgIdDisplay}>
                        {this.state.orgId || 'Not available'}
                    </Text>
                </View>

                <FlatList
                    data={this.state.data}
                    renderItem={({ item }) => <Text style={styles.item}>{item.Name}</Text>}
                    keyExtractor={(item, index) => 'key_' + index}
                />

                <TouchableOpacity
                    style={[styles.agentforceButton, !this.state.agentforceInitialized && styles.agentforceButtonDisabled]}
                    onPress={this.launchAgentforceSDK}
                    disabled={!this.state.agentforceInitialized}
                >
                    {this.state.agentforceInitialized ? (
                        <Image
                            source={require('./agentforce-icon.png')}
                            style={styles.agentforceButtonIcon}
                        />
                    ) : (
                        <Text style={styles.agentforceButtonText}>Initializing...</Text>
                    )}
                </TouchableOpacity>

            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingTop: 22,
        backgroundColor: 'white',
    },
    item: {
        padding: 10,
        fontSize: 18,
        height: 44,
    },
    agentforceButton: {
        position: 'absolute',
        bottom: 30,
        right: 20,
        backgroundColor: '#0070f3',
        width: 60,
        height: 60,
        borderRadius: 30,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 5,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
    },
    agentforceButtonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
        textAlign: 'center',
    },
    agentforceButtonIcon: {
        width: 40,
        height: 40,
        resizeMode: 'contain',
    },
    agentforceButtonDisabled: {
        backgroundColor: '#cccccc',
    },
    agentIdContainer: {
        backgroundColor: '#f8f9fa',
        padding: 15,
        marginTop: 10,
        marginHorizontal: 15,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: '#e9ecef',
    },
    agentIdLabel: {
        fontSize: 16,
        fontWeight: '600',
        color: '#343a40',
        marginBottom: 8,
    },
    agentIdInputRow: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    agentIdDisplayRow: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    agentIdInput: {
        flex: 1,
        borderWidth: 1,
        borderColor: '#ced4da',
        borderRadius: 6,
        paddingHorizontal: 12,
        paddingVertical: 8,
        fontSize: 14,
        backgroundColor: 'white',
        marginRight: 8,
    },
    agentIdDisplay: {
        flex: 1,
        fontSize: 14,
        color: '#495057',
        backgroundColor: 'transparent',
        paddingHorizontal: 12,
        paddingVertical: 8,
        marginRight: 8,
        fontFamily: 'monospace',
    },
    editButton: {
        backgroundColor: '#0070f3',
        width: 32,
        height: 32,
        borderRadius: 16,
        justifyContent: 'center',
        alignItems: 'center',
    },
    editButtonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
    },
    orgIdContainer: {
        backgroundColor: '#f8f9fa',
        padding: 15,
        marginTop: 5,
        marginHorizontal: 15,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: '#e9ecef',
    },
    orgIdLabel: {
        fontSize: 16,
        fontWeight: '600',
        color: '#343a40',
        marginBottom: 8,
    },
    orgIdDisplay: {
        fontSize: 14,
        color: '#6c757d',
        fontFamily: 'monospace',
        backgroundColor: '#ffffff',
        padding: 12,
        borderRadius: 6,
        borderWidth: 1,
        borderColor: '#dee2e6',
    },
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    modalContent: {
        backgroundColor: 'white',
        margin: 20,
        borderRadius: 20,
        padding: 30,
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 4,
        elevation: 5,
        minWidth: 300,
    },
    modalTitle: {
        fontSize: 22,
        fontWeight: 'bold',
        marginBottom: 15,
        textAlign: 'center',
        color: '#333',
    },
    modalMessage: {
        fontSize: 16,
        textAlign: 'center',
        marginBottom: 25,
        color: '#666',
        lineHeight: 22,
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        width: '100%',
    },
    modalButton: {
        flex: 1,
        paddingVertical: 12,
        paddingHorizontal: 20,
        borderRadius: 10,
        marginHorizontal: 5,
    },
    cancelButton: {
        backgroundColor: '#f1f1f1',
        borderWidth: 1,
        borderColor: '#ddd',
    },
    confirmButton: {
        backgroundColor: '#0070f3',
    },
    cancelButtonText: {
        color: '#666',
        fontSize: 16,
        fontWeight: '600',
        textAlign: 'center',
    },
    confirmButtonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: '600',
        textAlign: 'center',
    },
});

const Stack = createStackNavigator();

function App(): JSX.Element {
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen name="React Native Agentforce Integration" component={ContactListScreen} />
            </Stack.Navigator>
        </NavigationContainer>
    );
}

export default App;
