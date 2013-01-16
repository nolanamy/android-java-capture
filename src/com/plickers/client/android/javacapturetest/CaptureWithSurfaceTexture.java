package com.plickers.client.android.javacapturetest;

import java.io.IOException;

import android.graphics.SurfaceTexture;
import android.util.Log;

public class CaptureWithSurfaceTexture extends Capture
{
    private static final String TAG              = "JavaCaptureTest::CaptureWithSurfaceTexture";

    private static final int    MAGIC_TEXTURE_ID = 11;

    private SurfaceTexture      dummyTexture;

    public CaptureWithSurfaceTexture(CaptureListener callback, CameraOpener opener)
    {
        super(callback, opener);

        Log.i(TAG, "Constructor");
    }

    @Override
    protected boolean dummyPreviewDisplayReady()
    {
        //oh, surfaceTexture was born ready
        return true;
    }

    @Override
    protected boolean setDummyPreviewDisplay()
    {
        dummyTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
        // getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //??? TODO: use this? need surface?
        try
        {
            camera.setPreviewTexture(dummyTexture);
        }
        catch (IOException e)
        {
            Log.e(TAG, "initCapture - Exception in setPreviewDisplay()", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
