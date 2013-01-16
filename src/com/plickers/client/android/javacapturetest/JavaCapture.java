package com.plickers.client.android.javacapturetest;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;

public class JavaCapture extends Activity implements Capture.CaptureListener
{
    private static final String TAG = "JavaCaptureTest::JavaCapture";

    private Capture             capture;
    private SurfaceView         image;

    private Bitmap              bitmap;

    static
    {
        if (!OpenCVLoader.initDebug())
        {
            // TODO: Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            Log.i(TAG, "HONEYCOMB <= VERSION");
            capture = new CaptureWithSurfaceTexture(this, new CameraIntegerOpener());
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            Log.i(TAG, "GINGERBREAD <= VERSION < HONEYCOMB");
            capture = new CaptureWithSurfaceView((SurfaceView) findViewById(R.id.preview),
                    this, new CameraIntegerOpener());
        }
        else
        {
            Log.i(TAG, "VERSION < GINGERBREAD");
            capture = new CaptureWithSurfaceView((SurfaceView) findViewById(R.id.preview),
                    this, new CameraDefaultOpener());
        }
        image = (SurfaceView) findViewById(R.id.image);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        capture.setCameraIndex(0);
        capture.initCapture();
    }

    @Override
    public void onPause()
    {
        capture.releaseCapture();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG, "Menu Item selected " + item);
        if (item.getItemId() == R.id.menu_start)
        {
            capture.startPreview();
        }
        return true;
    }

    @Override
    public void onCaptureStarted(int width, int height)
    {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onCaptureStopped()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFrameReady(Mat frame)
    {
        Log.i(TAG, "onFrameReady");

        Utils.matToBitmap(frame, bitmap);

        try
        {
            Surface surface = image.getHolder().getSurface();
            Canvas canvas = surface.lockCanvas(null);
            canvas.drawBitmap(bitmap, 0, 0, null);
            surface.unlockCanvasAndPost(canvas);
        }
        catch (Exception e)
        {
            Log.e(TAG, "onFrameReady exception: " + e);
        }
    }

}
