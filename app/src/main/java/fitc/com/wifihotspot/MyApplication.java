package fitc.com.wifihotspot;

import android.app.Application;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class MyApplication extends Application {

	private static MyApplication singleton;
	private WifiManager.LocalOnlyHotspotReservation mHotspotReservation;

	public static MyApplication getInstance(){
		return singleton;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public WifiManager.LocalOnlyHotspotReservation getHotspotReservation() {
		return mHotspotReservation;
	}

	public WifiManager.LocalOnlyHotspotCallback getHotspotCallback() {
		return mLocalOnlyHotspotCallback;
	}

	/**
	 *
	 */
	WifiManager.LocalOnlyHotspotCallback mLocalOnlyHotspotCallback = new WifiManager.LocalOnlyHotspotCallback() {

		@Override
		public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
			super.onStarted(reservation);
			Log.d(TAG, "Wifi Hotspot is on now");
			mHotspotReservation = reservation;
		}

		@Override
		public void onStopped() {
			super.onStopped();
			Log.d(TAG, "onStopped: ");
		}

		@Override
		public void onFailed(int reason) {
			super.onFailed(reason);
			Log.d(TAG, "onFailed: ");
		}
	};

}