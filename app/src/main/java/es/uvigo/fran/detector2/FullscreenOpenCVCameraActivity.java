package es.uvigo.fran.detector2;

import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public abstract class FullscreenOpenCVCameraActivity extends FullscreenOpenCVActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int DEFAULT_W = 640, DEFAULT_H = 480;

    private CameraBridgeViewBase mCameraView;

    public FullscreenOpenCVCameraActivity(int contentView, boolean showHome) {
        super(contentView, showHome);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setMaxFrameSize(DEFAULT_W, DEFAULT_H);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
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

    protected CameraBridgeViewBase getCameraView() {
        return mCameraView;
    }
}
