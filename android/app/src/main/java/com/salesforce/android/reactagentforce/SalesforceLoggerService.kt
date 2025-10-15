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

import com.salesforce.androidsdk.util.SalesforceSDKLogger
import com.salesforce.android.mobile.interfaces.logging.*
import android.util.Log
/**
 * Logging service implementation that bridges Agentforce logging to Salesforce SDK logging.
 */
class SalesforceLoggerService : Logger {

    companion object {
        private const val TAG = "AgentforceLogger"
    }

    /**
     * Log a message with specified level.
     * 
     * @param logMessage The message to log
     * @param level The logging level
     * 
     * Note: Maps Agentforce log levels to appropriate Salesforce SDK log levels
     */
    override fun e(message: String) {
        Log.e(TAG, message)
        SalesforceSDKLogger.e(TAG, message)
    }

    override fun e(
        message: String,
        exception: Throwable,
    ) {
        Log.e(TAG, message)
        SalesforceSDKLogger.e(TAG, message)
    }

    override fun w(message: String) {
        Log.w(TAG, message)
        SalesforceSDKLogger.w(TAG, message)
    }

    override fun w(
        message: String,
        exception: Throwable,
    ) {
        Log.w(TAG, message)
        SalesforceSDKLogger.w(TAG, message)
    }

    override fun i(message: String) {
        Log.i(TAG, message)
        SalesforceSDKLogger.i(TAG, message)
    }

    override fun i(
        message: String,
        exception: Throwable,
    ) {
        Log.i(TAG, message)
        SalesforceSDKLogger.i(TAG, message)
    }
}

