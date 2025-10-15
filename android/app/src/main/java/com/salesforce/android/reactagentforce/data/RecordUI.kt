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
package com.salesforce.android.reactagentforce.data

import com.salesforce.android.agentforcesdkimpl.data.models.ObjectRepresentation
import com.salesforce.android.agentforcesdkimpl.data.models.RecordLayoutRepresentation
import com.salesforce.android.agentforcesdkimpl.data.models.SingleRecordUI
import com.salesforce.android.agentforcesdkimpl.data.models.UIAPIRecord
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import java.util.Locale

/**
 * This represents results of records-ui call. This includes object data and record data.
 *
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.uiapi.meta/uiapi/ui_api_resources_record_ui.htm">UI API Get Records</a>
 */
@Serializable
data class RecordUI(
    /** The metadata for the given records. **/
    val objectInfos: Map<String, ObjectRepresentation> = mapOf(),

    /** The data for the given records. **/
    @Serializable(with = RecordsSerializer::class)
    val records: Map<String, UIAPIRecord> = mapOf(),

    /** The layout data for the record types returned **/
    val layouts: Map<String, Map<String, Map<String, Map<String, RecordLayoutRepresentation>>>> = mapOf()
) {
    /** Get a singular record from the response **/
    fun getRecord(recordId: String, layoutType: String, mode: String): SingleRecordUI? {
        val record = records[recordId] ?: return null
        val recordTypeId = record.recordTypeId

        val layoutTypeString = layoutType.lowercase(Locale.ENGLISH).replaceFirstChar { it.titlecase() }
        val recordLayout = layouts[record.apiName]?.get(recordTypeId)?.get(layoutTypeString)?.get(mode)

        return SingleRecordUI(objectInfos, record, recordLayout)
    }
}

/**
 * Serializer for records JSON
 */
object RecordsSerializer : KSerializer<Map<String, UIAPIRecord>> {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override fun deserialize(decoder: Decoder): Map<String, UIAPIRecord> {
        val results: MutableMap<String, UIAPIRecord> = mutableMapOf()
        val jsonObject = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
        for (key in jsonObject.keys) {
            jsonObject[key]?.let {
                results[key] = json.decodeFromJsonElement(UIAPIRecord.serializer(), it)
            }
        }
        return results
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(Map::class.simpleName.toString())

    override fun serialize(encoder: Encoder, value: Map<String, UIAPIRecord>) {
        // No need to serialize
    }
}
