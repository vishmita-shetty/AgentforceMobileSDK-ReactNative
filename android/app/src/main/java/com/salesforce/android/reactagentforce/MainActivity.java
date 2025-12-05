/*
 * Copyright (c) 2024-present, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.android.reactagentforce;

import android.os.Bundle;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

/**
 * Simplified MainActivity for Agentforce Service Agent sample
 * No Salesforce Mobile SDK required - standard React Native activity
 */
public class MainActivity extends ReactActivity {

	/**
	 * Returns the name of the main component registered from JavaScript.
	 * This is used to schedule rendering of the component.
	 */
	@Override
	protected String getMainComponentName() {
		return "ReactAgentforce";
	}

	/**
	 * Returns the instance of the {@link ReactActivityDelegate}. We use a custom delegate
	 * that wraps the root view in RNGestureHandlerEnabledRootView for proper touch handling.
	 */
	@Override
	protected ReactActivityDelegate createReactActivityDelegate() {
		return new DefaultReactActivityDelegate(
			this,
			getMainComponentName(),
			DefaultNewArchitectureEntryPoint.getFabricEnabled()
		) {
			@Override
			protected com.facebook.react.ReactRootView createRootView() {
				RNGestureHandlerEnabledRootView rootView = new RNGestureHandlerEnabledRootView(MainActivity.this);
				return rootView;
			}
		};
	}

	/**
	 * Required for react-native-screens to work properly
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
	}
}
