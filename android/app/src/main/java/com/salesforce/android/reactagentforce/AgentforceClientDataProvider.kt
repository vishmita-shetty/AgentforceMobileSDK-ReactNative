/*
 * Copyright (c) 2020-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.android.reactagentforce

import com.salesforce.android.reactagentforce.data.ObjectRepresentationList
import com.salesforce.android.reactagentforce.data.RecordUI
import com.salesforce.android.agentforcesdkimpl.data.DataProvider
import com.salesforce.android.agentforcesdkimpl.data.models.ObjectRepresentation
import com.salesforce.android.agentforcesdkimpl.data.models.UIAPIRecord
import com.salesforce.android.mobile.interfaces.network.Network
import com.salesforce.android.mobile.interfaces.network.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection

/**
 * Data Provider that makes simple ad hoc calls to UI APIs
 */
class AgentforceClientDataProvider(val network: Network?) : DataProvider {
    companion object {
        private const val API_NUM = 57
        private const val CURRENT_API = "v$API_NUM.0"
        private const val BASE_API = "services/data/"
        private const val LATEST_PATH = "$BASE_API$CURRENT_API"
        private const val OBJECT_INFO = "/ui-api/object-info/"
        private const val BATCH = "batch/"
        private const val RECORDS_UI = "/ui-api/record-ui/%s"
        private const val LAYOUT_TYPES = "layoutTypes"
        private const val RECORDS = "/ui-api/records/"
        private const val FIELDS = "fields"

        var dispatcher = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private val objectInfos = mutableMapOf<String, ObjectRepresentation>()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }


    /**
     * Get the [ObjectRepresentation]
     * Sample:/services/data/v57.0/ui-api/object-info/Account
     */
    override fun getObjectInfo(
        objectType: String,
        cachePolicy: DataProvider.CachePolicy?,
        completion: (Result<ObjectRepresentation>) -> Unit
    ) {

        // Return in memory objectInfo if available
        objectInfos[objectType]?.let {
            completion(Result.success(it))
            return
        }

        val queryPath = "$LATEST_PATH$OBJECT_INFO$objectType"
        val networkRequest = NetworkRequest(NetworkRequest.Method.GET, queryPath)

        dispatcher.launch {
            network?.let { network ->
                try {
                    requestNetworkSync(network, networkRequest)?.let { body ->
                        try {
                            val data = body.toString(Charsets.UTF_8)
                            val decodeBody =
                                json.decodeFromString(ObjectRepresentation.serializer(), data)
                            objectInfos[objectType] = decodeBody
                            completion(Result.success(decodeBody))
                        } catch (ex: Exception) {
                            completion(Result.failure(ex))
                        }
                    }
                } catch (ex: Exception) {
                    completion(Result.failure(ex))
                }
            }
        }
    }

    override fun getObjectInfos(
        objectTypes: List<String>,
        cachePolicy: DataProvider.CachePolicy?,
        completion: (Result<List<ObjectRepresentation>>) -> Unit
    ) {
        // Fetch from network, otherwise
        val queryPath = "$LATEST_PATH$OBJECT_INFO$BATCH${
            objectTypes.filter { it.isNotBlank() }.joinToString(",")
        }"
        val networkRequest =
            NetworkRequest(
                NetworkRequest.Method.GET,
                queryPath
            )
        // Note: we don't have cache policy implemented at the moment.
        // Will add in the future.

        dispatcher.launch {
            network?.let { network ->
                try {
                    requestNetworkSync(network, networkRequest)?.let { body ->
                        try {
                            val data = body.toString(Charsets.UTF_8)
                            val decodeBody =
                                json.decodeFromString(ObjectRepresentationList.serializer(), data)
                            completion(Result.success(decodeBody.results))
                        } catch (ex: Exception) {
                            completion(Result.failure(ex))
                        }
                    }
                } catch (ex: Exception) {
                    completion(Result.failure(ex))
                }
            }
        }
    }

    override fun getRecord(
        objectType: String,
        recordId: String,
        fields: List<String>,
        cachePolicy: DataProvider.CachePolicy?,
        completion: (Result<UIAPIRecord>) -> Unit
    ) {
        val queryFields = fields.joinToString(separator = ",") { "$objectType.$it" }
        val queryPath = "$LATEST_PATH$RECORDS$recordId"
        val networkRequest =
            NetworkRequest(
                NetworkRequest.Method.GET,
                queryPath,
                mapOf(
                    FIELDS to queryFields
                )
            )

        // Note: we don't have cache policy implemented at the moment.
        // Will add in the future.

        dispatcher.launch {
            network?.let { network ->
                try {
                    requestNetworkSync(network, networkRequest)?.let { body ->
                        try {
                            val data = body.toString(Charsets.UTF_8)
                            val decodeBody: UIAPIRecord = json.decodeFromString(data)
                            completion(Result.success(decodeBody))
                        } catch (ex: Exception) {
                            completion(Result.failure(ex))
                        }
                    }
                } catch (ex: Exception) {
                    completion(Result.failure(ex))
                }
            }
        }
    }

    override fun getRecords(
        layoutTypes: String,
        recordIds: List<String>,
        cachePolicy: DataProvider.CachePolicy?,
        completion: (Result<List<UIAPIRecord>>) -> Unit
    ) {
        val queryPath =
            String.format(
                LATEST_PATH + RECORDS_UI,
                recordIds.filter { it.isNotBlank() }.joinToString(",")
            )
        val networkRequest =
            NetworkRequest(
                NetworkRequest.Method.GET,
                queryPath,
                mapOf(
                    LAYOUT_TYPES to layoutTypes
                )
            )

        dispatcher.launch {
            network?.let { network ->
                try {
                    requestNetworkSync(network, networkRequest)?.let { body ->
                        try {
                            val data = body.toString(Charsets.UTF_8)
                            val decodeBody = json.decodeFromString(RecordUI.serializer(), data)

                            // Save objectInfos in memory
                            objectInfos.putAll(decodeBody.objectInfos)

                            // Return records
                            val list = decodeBody.records.values.toList()
                            completion(Result.success(list))
                        } catch (ex: Exception) {
                            completion(Result.failure(ex))
                        }
                    }
                } catch (ex: Exception) {
                    completion(Result.failure(ex))
                }
            }
        }
    }

    /**
     * Network sync by the [NetworkRequest]
     */
    private suspend fun requestNetworkSync(
        network: Network,
        networkRequest: NetworkRequest
    ): ByteArray? {
        val response = network.perform(networkRequest)

        if (response.statusCode != HttpURLConnection.HTTP_OK &&
            response.statusCode != HttpURLConnection.HTTP_CREATED
        ) {
            throw Exception(
                "Error performing network request. Status code: ${response.statusCode}"
            )
        }

        return response.body
    }
}
