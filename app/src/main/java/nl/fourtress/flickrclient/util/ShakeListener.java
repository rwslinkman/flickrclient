package nl.fourtress.flickrclient.util;

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.util.Log;

import java.lang.UnsupportedOperationException;

public class ShakeListener implements SensorEventListener
{
    private static final String TAG = "ShakeListener";

    public interface OnShakeListener
    {
        void onShake();
    }

    private SensorManager mSensorManager;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private static final float SHAKE_THRESHOLD = 7f;
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;

    public ShakeListener(Context context)
    {
        mContext = context;
    }

    public void startListening(OnShakeListener listener)
    {
        mShakeListener = listener;
        listen();
    }

    private void listen() {
        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        // Listen to sensor to detect shake
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListening() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                if (acceleration > SHAKE_THRESHOLD) {
                    Log.d(TAG, "onSensorChanged: triggered with a " + acceleration);
                    mLastShakeTime = curTime;
                    if(mShakeListener != null) {
                        mShakeListener.onShake();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
        // NOP
    }
}