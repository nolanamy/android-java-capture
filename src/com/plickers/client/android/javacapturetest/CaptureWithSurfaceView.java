package com.plickers.client.android.javacapturetest;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CaptureWithSurfaceView extends Capture
{
    private static final String TAG               = "JavaCaptureTest::CaptureWithSurfaceView";

    private SurfaceView         dummySurface;
    private SurfaceHolder       dummyHolder;

    private boolean             dummySurfaceReady = false;

    public CaptureWithSurfaceView(SurfaceView dummySurface, CaptureListener callback)
    {
        super(callback);

        Log.i(TAG, "Constructor");

        // initialize dummy surface
        if (dummySurface == null)
        {
            Log.e(TAG, "dummySurface must not be null");
            return;
        }
        this.dummySurface = dummySurface;
        dummyHolder = dummySurface.getHolder();
        dummyHolder.addCallback(dummySurfaceCallback);
        dummyHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //KEEP THIS
    }

    @Override
    public boolean dummyPreviewDisplayReady()
    {
        return dummySurfaceReady;
    }

    @Override
    public boolean setDummyPreviewDisplay()
    {
        try
        {
            if (dummySurfaceReady)
            {
                camera.setPreviewDisplay(dummyHolder);
            }
            else
            {
                camera.setPreviewDisplay(null);
            }
        }
        catch (Throwable t)
        {
            Log.e(TAG, "initCapture - Exception in setPreviewDisplay()", t);
            return false;
        }
        return true;
    }

    // handle dummySurface events
    //TODO: when are these called? esp. as compared to lifecycle events, etc.
    private SurfaceHolder.Callback dummySurfaceCallback = new SurfaceHolder.Callback()
    {
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            // no-op -- wait until
            // surfaceChanged()
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            dummySurfaceReady = true;
            setDummyPreviewDisplay();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // no-op
            dummySurfaceReady = false;
            //TODO: stop preview/release capture here?
        }
    };

}
