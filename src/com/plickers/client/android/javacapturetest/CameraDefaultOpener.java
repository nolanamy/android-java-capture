package com.plickers.client.android.javacapturetest;

import android.hardware.Camera;
import android.util.Log;

public class CameraDefaultOpener implements Capture.CameraOpener
{
    private static final String TAG = "JavaCaptureTest::CameraDefaultOpener";

    @Override
    public Camera open()
    {
        Camera camera;
        Log.d(TAG, "Trying to open camera with old open()");
        try
        {
            camera = Camera.open();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Camera is not available: ", e);
            return null;
        }
        return camera;
    }

    @Override
    public void setIndex(int index)
    {
        Log.w(TAG, "Old open; not setting index...");
    }

}
