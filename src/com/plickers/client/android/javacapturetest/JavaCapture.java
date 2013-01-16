package com.plickers.client.android.javacapturetest;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.ImageView;

public class JavaCapture extends Activity implements Capture.CaptureListener
{
    private static final String TAG = "Plickers-JavaCaptureTest::JavaCapture";

    private Capture             capture;
    private ImageView           image;

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

        capture = new Capture((SurfaceView) findViewById(R.id.preview), this);
//        image = (ImageView) findViewById(R.id.image);
    }

    @Override
    public void onResume()
    {
        super.onResume();

//        capture.initCapture();
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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

        Utils.matToBitmap(frame, bitmap);
//        ImageView image = (ImageView) findViewById(R.id.image);
//        image.setImageBitmap(bitmap);
    }

}
