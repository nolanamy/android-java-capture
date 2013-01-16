package com.plickers.client.android.javacapturetest;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Capture
{
    private static final String TAG               = "JavaCaptureTest::Capture";

    private SurfaceView         dummySurface;
    private SurfaceHolder       dummyHolder;
    private Camera              camera;

    private CaptureListener     callback          = null;
    private Mat                 baseMat;
    private Mat                 frame;

    private int                 targetWidth       = 1280;
    private int                 targetHeight      = 720;
    private Camera.Size         frameSize;

    private boolean             previewOn         = false;
    private boolean             dummySurfaceReady = false;
    private boolean             cameraInited      = false;
    private boolean             displaySet      = false;

    public Capture(SurfaceView dummySurface, CaptureListener callback)
    {
        //set up callback
        this.callback = callback;

        // initialize dummy surface
        if (dummySurface == null)
        {
            Log.e(TAG, "dummySurface must not be null");
            return;
        }
        this.dummySurface = dummySurface;
        dummyHolder = dummySurface.getHolder();
        dummyHolder.addCallback(dummySurfaceCallback);
        dummyHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public interface CaptureListener {

        //called upon starting preview
        public void onCaptureStarted(int width, int height);

        //called upon stopping preview
        public void onCaptureStopped();

        //called when a frame is ready
        public void onFrameReady(Mat frame);

    }

    public boolean initCapture()
    {
        camera = Camera.open();
        //TODO: new open for new devices

        if (camera == null)
        {
            return false;
        }

        //set parameters
        Camera.Parameters params = camera.getParameters();
        Camera.Size size = getBestPreviewSize(targetWidth, targetHeight,
                params.getSupportedPreviewSizes());

        if (size == null) //no sizes found
        {
            camera.release();
            return false;
        }

        params.setPreviewSize(size.width, size.height);
        params.setPreviewFormat(ImageFormat.NV21);

        camera.setParameters(params);
        
        //check which parameters were set
        params = camera.getParameters();

        frameSize = params.getPreviewSize();

        //initialize images
        baseMat = new Mat(frameSize.height + (frameSize.height / 2), frameSize.width, CvType.CV_8UC1);
        frame = new Mat();

        //set callback
        camera.setPreviewCallback(onPreviewFrame);

        //set display (dummy, usually null for now)
        setDummyPreviewDisplay();

        cameraInited = true;
        return true;
    }

    public void releaseCapture()
    {
        stopPreview();
        
        camera.setPreviewCallback(null);
        
        camera.release();
        cameraInited = false;
    }

    public void startPreview()
    {
        if (!previewOn && readyToPreview())
        {
            //TODO: if camera initialized, etc.
            camera.startPreview();
            previewOn = true;

            if (callback != null)
                callback.onCaptureStarted(frameSize.width, frameSize.height);
        }
    }

    public void stopPreview()
    {
        if (previewOn)
        {
            camera.stopPreview();
            previewOn = false;

            if (callback != null)
                callback.onCaptureStopped();
        }
    }

    public boolean readyToPreview()
    {
        return dummySurfaceReady && cameraInited && displaySet;
    }
    
    public void setTargetSize(int width, int height)
    {
        targetWidth = width;
        targetHeight = height;
    }
    
    public boolean setDummyPreviewDisplay()
    {
        Log.i(TAG + "::setDummyPreviewDisplay()", "surface ready? " + dummySurfaceReady);
        //TODO: preview texture for new devices
        try
        {
            if(dummySurfaceReady)
            {
                camera.setPreviewDisplay(dummyHolder);
                displaySet = true;
            }
            else
            {
//                camera.setPreviewDisplay(null);
                displaySet = false;
            }
        }
        catch (Throwable t)
        {
            Log.e(TAG + "::initCapture", "Exception in setPreviewDisplay()", t);
            return false;
        }
        return true;
    }
    
    private Camera.PreviewCallback onPreviewFrame = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub
            Log.i(TAG + "::onPreviewFrame", "data: " + data.length + " bytes");
            
            baseMat.put(0, 0, data);
            Imgproc.cvtColor(baseMat, frame, Imgproc.COLOR_YUV2RGBA_NV21, 4);

            if (callback != null)
                callback.onFrameReady(frame);
        }
    };

    // handle dummy surface events
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
            initCapture();
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

    private Camera.Size getBestPreviewSize(int targetWidth, int targetHeight,
            List<Camera.Size> sizes)
    {
        Camera.Size bestSize = null;
        int bestSizeDistance = 1000000000;
        
        if(sizes == null)
        {
            return null;
        }

        for (Camera.Size size : sizes)
        {
            Log.i(TAG + "::getBestPreviewSize", "size: " + size.width + "x" + size.height);

            int widthDelta = targetWidth - size.width;
            int heightDelta = targetHeight - size.height;

            if (widthDelta * widthDelta + heightDelta * heightDelta < bestSizeDistance)
            {
                bestSize = size;
                bestSizeDistance = widthDelta * widthDelta + heightDelta * heightDelta;
            }
        }

        return bestSize;
    }
}
