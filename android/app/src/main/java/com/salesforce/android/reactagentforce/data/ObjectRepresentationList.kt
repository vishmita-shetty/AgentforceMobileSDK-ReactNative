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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject

/**
 * The list of ObjectRepresentations.
 *
 * @see <a href="https://developer.salesforce.com/docs/atlas.en-us.uiapi.meta/uiapi/ui_api_responses_batch_results_simplified.htm">Record</a>
 */
@Serializable
data class ObjectRepresentationList(
    @Serializable(with = ObjectSerializer::class)
    val results: List<ObjectRepresentation> = listOf()
)

/**
 * Serializer for ObjectInfo results JSON
 */
object ObjectSerializer : KSerializer<List<ObjectRepresentation>> {
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override fun deserialize(decoder: Decoder): List<ObjectRepresentation> {
        val results: MutableList<ObjectRepresentation> = mutableListOf()
        val jsonArray = (decoder as JsonDecoder).decodeJsonElement() as JsonArray
        jsonArray.forEach { jsonElement ->
            jsonElement.jsonObject["result"]?.let {
                json.decodeFromJsonElement(
                    ObjectRepresentation.serializer(),
                    it
                )
            }?.let { it2 -> results.add(it2) }
        }
        return results
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(Map::class.simpleName.toString())

    override fun serialize(encoder: Encoder, value: List<ObjectRepresentation>) {
        // No need to serialize
    }
}
