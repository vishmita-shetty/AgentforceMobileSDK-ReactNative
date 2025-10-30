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

import com.salesforce.android.mobile.interfaces.navigation.Navigation
import com.salesforce.android.mobile.interfaces.navigation.destination.App
import com.salesforce.android.mobile.interfaces.navigation.destination.Destination
import com.salesforce.android.mobile.interfaces.navigation.destination.Record

/**
 * Navigation service implementation for Agentforce SDK.
 *
 * This service handles navigation requests from the Agentforce chat interface,
 * intercepting record navigation events and forwarding them to React Native
 * through the event emitter callback.
 */
class SalesforceNavigationService(
    private val onNavigate: ((String, String?) -> Unit)?
) : Navigation {

    /**
     * Handles navigation to a destination.
     *
     * This is a convenience method that delegates to the two-parameter version
     * with replace=false as the default behavior.
     *
     * @param destination The navigation destination from Agentforce
     */
    override fun goto(destination: Destination) {
        goto(destination, false)
    }

    /**
     * Handles navigation to a destination with replace option.
     *
     * When the destination is a Record (e.g., Contact, Account), this method
     * extracts the record ID and type, then invokes the callback to notify
     * React Native of the navigation event.
     *
     * @param destination The navigation destination from Agentforce
     * @param replace Whether to replace the current screen (true) or push new (false)
     */
    override fun goto(destination: Destination, replace: Boolean) {
        if (destination is Record) {
            onNavigate?.invoke(destination.id, destination.type)
        }
        // Note: The replace parameter is currently ignored but could be passed
        // to React Native if needed for navigation stack management
    }

    /**
     * Handles opening external apps.
     *
     * @param app The app to open
     * @return OpenResult indicating if the app was opened (OPEN), not opened (NOTOPEN),
     *         or needs to be installed from the store (STORE)
     */
    override fun openApp(app: App): App.OpenResult {
        // Return NOTOPEN as we're not handling external app opening in this implementation
        // This could be enhanced to support opening external apps if needed
        return App.OpenResult.NOTOPEN
    }
}
