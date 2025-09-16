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
    agentforceInitialized: boolean
}

class ContactListScreen extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = { data: [], showAgentforceModal: false, agentforceInitialized: false };
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
                            const config = {
                                agentId: "0XxEE0000000FlR0AU", // Replace with actual Agent ID
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

    launchAgentforceSDK = () => {
        if (!this.state.agentforceInitialized) {
            console.log('Agentforce is not initialized yet. Please wait a moment and try again.');
            return;
        }

        const agentId = "0XxEE0000000FlR0AU"; // Replace with actual Agent ID

        AgentforceManager.presentAgentforceChatView(agentId)
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
