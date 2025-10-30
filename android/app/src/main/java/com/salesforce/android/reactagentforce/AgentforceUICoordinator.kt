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

import android.R
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.ReactApplicationContext

/**
 * Coordinates UI presentation and lifecycle for Agentforce chat views.
 *
 * This class handles the presentation and dismissal of Agentforce chat
 * interface as a full-screen dialog, converting Jetpack Compose Composables
 * to traditional Android Views.
 */
class AgentforceUICoordinator(private val context: Context) {

    companion object {
        private const val TAG = "AgentforceUICoordinator"
        private const val FRAGMENT_TAG = "AgentforceChatFragment"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentFragment: Fragment? = null

    /**
     * Presents the Agentforce chat view.
     *
     * @param chatComposable The Composable containing the chat interface
     * @param callback Callback for presentation result
     */
    fun presentChatView(
        chatComposable: @Composable () -> Unit,
        callback: AgentforceClientManager.PresentationCallback
    ) {
        mainHandler.post {
            try {
                val activity = getFragmentActivity()
                    ?: throw IllegalStateException("No fragment activity found")

                // Create fragment with Composable
                val fragment = AgentforceChatFragment.newInstance(chatComposable)
                currentFragment = fragment

                // Add fragment to container (assumes android.R.id.content as container)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.content, fragment, FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit()
                    
                callback.onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error presenting chat view", e)
                callback.onError(e)
            }
        }
    }

    /**
     * Dismisses the currently displayed chat view.
     *
     * @param callback Callback for dismissal result
     */
    fun dismissChatView(callback: AgentforceClientManager.DismissCallback) {
        mainHandler.post {
            try {
                val activity = getFragmentActivity()
                    ?: throw IllegalStateException("No fragment activity found")
                    
                // Remove fragment
                currentFragment?.let { fragment ->
                    activity.supportFragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitAllowingStateLoss()
                }
                
                currentFragment = null
                callback.onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing chat view", e)
                callback.onError(e)
            }
        }
    }

    /**
     * Gets the current FragmentActivity from the context.
     */
    private fun getFragmentActivity(): FragmentActivity? {
        return when (context) {
            is FragmentActivity -> context
            is ReactActivity -> context
            is ReactApplicationContext -> {
                // Get the current activity from ReactApplicationContext
                val currentActivity = context.currentActivity
                when (currentActivity) {
                    is FragmentActivity -> currentActivity
                    is ReactActivity -> currentActivity
                    else -> null
                }
            }
            else -> null
        }
    }

    /**
     * Fragment for displaying the Agentforce chat interface.
     *
     * This fragment wraps a Jetpack Compose Composable in a ComposeView
     * to display it in the traditional Android View system.
     */
    class AgentforceChatFragment : Fragment() {

        companion object {
            private const val TAG = "AgentforceChatFragment"

            // We store the composable as a static variable since it can't be serialized
            // This is safe for fragments that are created and used immediately
            private var chatComposable: (@Composable () -> Unit)? = null

            fun newInstance(composable: @Composable () -> Unit): AgentforceChatFragment {
                chatComposable = composable
                return AgentforceChatFragment()
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String?>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            AgentforceClientPermissions.handlePermissionResult(requestCode, permissions, grantResults)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Create a ComposeView to host the Composable
            return ComposeView(requireContext()).apply {
                // Dispose the composition when the view is detached
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                // Set the Composable content with top padding only (status bar + camera cutout)
                setContent {
                    // Wrap content with Box to add padding only at top (not bottom)
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .displayCutoutPadding()
                    ) {
                        chatComposable?.invoke() ?: run {
                            Log.e(TAG, "No chat composable available")
                        }
                    }
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            // Clear the static reference to prevent memory leaks
            chatComposable = null
        }
    }
}

