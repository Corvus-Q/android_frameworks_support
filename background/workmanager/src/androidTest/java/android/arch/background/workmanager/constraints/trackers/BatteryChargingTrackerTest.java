/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.arch.background.workmanager.constraints.trackers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.annotation.TargetApi;
import android.arch.background.workmanager.constraints.listeners.BatteryChargingListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class BatteryChargingTrackerTest {

    private BatteryChargingTracker mTracker;
    private BatteryChargingListener mListener;

    @Before
    public void setUp() {
        mTracker = new BatteryChargingTracker(InstrumentationRegistry.getTargetContext());
        mListener = mock(BatteryChargingListener.class);
        mTracker.mListeners.add(mListener);  // Add it silently so no broadcasts trigger.
    }

    @Test
    @SdkSuppress(maxSdkVersion = 22)
    public void testGetIntentFilter_beforeApi23() {
        IntentFilter intentFilter = mTracker.getIntentFilter();
        assertThat(intentFilter.hasAction(Intent.ACTION_BATTERY_CHANGED), is(true));
        assertThat(intentFilter.countActions(), is(1));
    }

    @Test
    @SdkSuppress(minSdkVersion = 23)
    public void testGetIntentFilter_afterApi23() {
        IntentFilter intentFilter = mTracker.getIntentFilter();
        assertThat(intentFilter.hasAction(BatteryManager.ACTION_CHARGING), is(true));
        assertThat(intentFilter.hasAction(BatteryManager.ACTION_DISCHARGING), is(true));
        assertThat(intentFilter.countActions(), is(2));
    }

    @Test
    public void testOnReceive_nullIntent_doesNotNotifyListeners() {
        mTracker.onReceive(
                InstrumentationRegistry.getTargetContext(),
                null);
        verify(mListener, never()).setBatteryCharging(anyBoolean());
    }

    @Test
    @SdkSuppress(maxSdkVersion = 22)
    public void testOnReceive_notifiesListeners_beforeApi23() {
        mTracker.onReceive(
                InstrumentationRegistry.getTargetContext(),
                createBatteryChangedIntent(true));
        verify(mListener).setBatteryCharging(true);
        mTracker.onReceive(
                InstrumentationRegistry.getTargetContext(),
                createBatteryChangedIntent(false));
        verify(mListener).setBatteryCharging(false);
    }

    @Test
    @SdkSuppress(minSdkVersion = 23)
    public void testOnReceive_notifiesListeners_afterApi23() {
        mTracker.onReceive(
                InstrumentationRegistry.getTargetContext(),
                createBatteryChangedIntent_afterApi23(true));
        verify(mListener).setBatteryCharging(true);
        mTracker.onReceive(
                InstrumentationRegistry.getTargetContext(),
                createBatteryChangedIntent_afterApi23(false));
        verify(mListener).setBatteryCharging(false);
    }

    private Intent createBatteryChangedIntent(boolean plugged) {
        Intent intent = new Intent(Intent.ACTION_BATTERY_CHANGED);
        intent.putExtra(BatteryManager.EXTRA_PLUGGED, plugged ? 1 : 0);
        return intent;
    }

    @TargetApi(23)
    private Intent createBatteryChangedIntent_afterApi23(boolean charging) {
        return new Intent(
                charging ? BatteryManager.ACTION_CHARGING : BatteryManager.ACTION_DISCHARGING);
    }
}
