package com.plickers.client.android.javacapturetest;

import android.hardware.Camera;
import android.util.Log;

public class CameraIntegerOpener implements Capture.CameraOpener
{
    private static final String TAG         = "JavaCaptureTest::CameraIntegerOpener";

    private int                 cameraIndex = -1;

    @Override
    public Camera open()
    {
        Camera camera = null;

        Log.d(TAG, "Trying to open camera with new open(" + cameraIndex + ")");
        try
        {
            camera = Camera.open(cameraIndex);
        }
        catch (RuntimeException e)
        {
            Log.e(TAG, "Camera #" + cameraIndex + "failed to open: ", e);
            return null;
        }
        
        return camera;
    }

    @Override
    public void setIndex(int index)
    {
        cameraIndex = index;
    }
}
