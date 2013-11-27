package com.plickers.client.android.javacapturetest;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
//import android.graphics.Canvas;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
//import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class JavaCapture extends Activity implements Capture.CaptureListener
{
    private static final String TAG = "JavaCaptureTest::JavaCapture";

    private Capture             capture;
//    private SurfaceView         image;

    private Bitmap              bitmap;

    private SurfaceView         surfaceView         = null;

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

        surfaceView = (SurfaceView) findViewById(R.id.preview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            Log.i(TAG, "GINGERBREAD <= VERSION");
            capture = new CaptureWithSurfaceView(surfaceView,
                    this, new CameraIntegerOpener());
        }
        else
        {
            Log.i(TAG, "VERSION < GINGERBREAD");
            capture = new CaptureWithSurfaceView(surfaceView,
                    this, new CameraDefaultOpener());
        }
//        image = (SurfaceView) findViewById(R.id.image);

        final Button button = (Button) findViewById(R.id.button_run);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                capture.startPreview();
            }
        });
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
//            Surface surface = image.getHolder().getSurface();
//            Canvas canvas = surface.lockCanvas(null);
//            canvas.drawBitmap(bitmap, 0, 0, null);
//            surface.unlockCanvasAndPost(canvas);
        }
        catch (Exception e)
        {
            Log.e(TAG, "onFrameReady exception: " + e);
        }
    }

    //update the size of the Preview SurfaceView to match the Preview size
    @Override
    public void onPreviewSizeSet(int width, int height)
    {
        View view = surfaceView;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = width;
        params.width = height;
        Log.d(TAG, "updating SurfaceView size: " + params.width + "x" + params.height);
        view.setLayoutParams(params);

        //TODO also, center SurfaceView by comparing its size to the layout size
        //.... probably need to get the size of the layout in some android callback called when all views are set up
        //.... android gives size: 0x0 before that, it seems

        //        View rootView = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        //        Log.d(TAG, "rootView size: " + rootView.getMeasuredWidth() + "x" + rootView.getMeasuredHeight());

        //        view.setTranslationY((int) ((height - rootViewHeight)*0.5));
        //        view.setTranslationX((int) ((width - rootViewWidth)*0.5));
    }

}
