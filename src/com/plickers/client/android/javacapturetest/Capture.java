package com.plickers.client.android.javacapturetest;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

public abstract class Capture
{
    private static final String TAG          = "JavaCaptureTest::Capture";

    protected Camera            camera;
    private CameraOpener        opener;

    private CaptureListener     callback     = null;
    private Mat                 baseMat;
    private Mat                 frame;

    public boolean              stopRequested;

    private int                 captureType  = Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA;
    private int                 targetWidth  = 1280;
    private int                 targetHeight = 720;

    private Camera.Size         frameSize;

    private boolean             previewOn    = false;
    private boolean             cameraInited = false;

    private Thread              thread;

    public Capture(CaptureListener callback, CameraOpener opener)
    {
        // set up callback
        this.callback = callback;

        // set up opener
        this.opener = opener;
    }

    public interface CameraOpener
    {
        //abstracts new and old open()/open(int) versions
        public abstract Camera open();

        //sets the camera index to be used. call before open.
        public abstract void setIndex(int index);
    }

    public interface CaptureListener
    {
        //called upon starting preview
        public void onCaptureStarted(int width, int height);

        //called upon stopping preview
        public void onCaptureStopped();

        //called when a frame is ready
        public void onFrameReady(Mat frame);
    }

    public void setCameraIndex(int index)
    {
        opener.setIndex(index);
    }

    public void setTargetSize(int width, int height)
    {
        targetWidth = width;
        targetHeight = height;
    }

    public void setPreviewFormat(int type)
    {
        if (type == Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA
                || type == Highgui.CV_CAP_ANDROID_GREY_FRAME)
        {
            captureType = type;
        }
        else
        {
            Log.e(TAG, "Invalid frame format! Only RGBA and Gray Scale are supported!");
        }
    }

    public boolean initCapture()
    {
        camera = opener.open();

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

        Log.i(TAG, "Selected preview size: " + frameSize.width + "x" + frameSize.height);

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
        Log.i(TAG, "releaseCapture");

        //block until thread stops
        forceStopThread();
        stopPreview();

        camera.setPreviewCallback(null);

        camera.release();
        cameraInited = false;
    }

    public void startPreview()
    {
        if (!previewOn && readyToPreview())
        {
            camera.startPreview();
            previewOn = true;

            // start processing thread
            Log.d(TAG, "Starting processing thread");

            stopRequested = false;

            thread = new Thread(new CaptureWorker());
            thread.start();

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
        return dummyPreviewDisplayReady() && cameraInited;
    }

    protected abstract boolean dummyPreviewDisplayReady();

    protected abstract boolean setDummyPreviewDisplay();
    
    private Camera.PreviewCallback onPreviewFrame = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera)
        {
            Log.i(TAG, "onPreviewFrame - data: " + data.length + " bytes");
            
            synchronized (Capture.this)
            {
                baseMat.put(0, 0, data);
                Capture.this.notify();
            }
        }
    };

    private void requestStopThread()
    {
        stopRequested = true;
    }

    private void forceStopThread()
    {
        requestStopThread();
        try
        {
            synchronized (this)
            {
                this.notify();
            }
            Log.i(TAG, "Waiting for thread...");
            if (thread != null) thread.join();
            Log.i(TAG, "...done");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            thread = null;
        }
    }

    // image handling thread
    private class CaptureWorker implements Runnable
    {
        public void run()
        {
            while (!stopRequested)
            {
                // wait for the next frame
                Log.i(TAG, "CaptureWorker::run - waiting for frame...");
                synchronized (Capture.this)
                {
                    try
                    {
                        Capture.this.wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "CaptureWorker::run - ...got frame");

                if (stopRequested)
                    break;

                // convert to frame as appropriate
                if (captureType == Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA)
                {
                    Imgproc.cvtColor(baseMat, frame, Imgproc.COLOR_YUV2RGBA_NV21, 4);
                }
                else
                {
                    frame = baseMat.submat(0, frameSize.height, 0, frameSize.width);
                }

                if (stopRequested)
                    break;

                // output frame
                if (!frame.empty() && callback != null)
                {
                    callback.onFrameReady(frame);
                }
            }
            Log.i(TAG, "CaptureWorker::run - end processing thread");
        }
    }

    private Camera.Size getBestPreviewSize(int targetWidth, int targetHeight,
            List<Camera.Size> sizes)
    {
        Camera.Size bestSize = null;
        int bestSizeDistance = 1000000000;

        if (sizes == null)
        {
            return null;
        }

        for (Camera.Size size : sizes)
        {
            Log.i(TAG, "getBestPreviewSize - size: " + size.width + "x" + size.height);

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
