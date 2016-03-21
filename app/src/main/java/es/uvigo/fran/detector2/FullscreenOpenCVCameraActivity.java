package es.uvigo.fran.detector2;

import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by fran on 11/03/16.
 */
public class FullscreenOpenCVCameraActivity extends FullscreenOpenCVActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int W = 640, H = 480;

    private CameraBridgeViewBase mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setMaxFrameSize(W, H);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null) mCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    public void init() {
        mCameraView.enableView();
    }
}
