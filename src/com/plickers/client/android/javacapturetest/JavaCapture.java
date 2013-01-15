package com.plickers.client.android.javacapturetest;

import org.opencv.android.OpenCVLoader;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

public class JavaCapture extends Activity
{
    private static final String TAG               = "Plickers-JavaCaptureTest::JavaCapture";

    Capture capture;
    
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
        
        capture = new Capture((SurfaceView) findViewById(R.id.preview), null);
    }

    @Override
    public void onResume()
    {
        super.onResume();

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

}
