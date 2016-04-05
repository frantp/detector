// This sample is based on "Camera calibration With OpenCV" tutorial:
// http://docs.opencv.org/doc/tutorials/calib3d/camera_calibration/camera_calibration.html
//
// It uses standard OpenCV asymmetric circles grid pattern 11x4:
// https://github.com/Itseez/opencv/blob/2.4/doc/acircles_pattern.png.
// The results are the camera matrix and 5 distortion coefficients.
//
// Tap on highlighted pattern to capture pattern corners for calibration.
// Move pattern along the whole screen and capture data.
//
// When you've captured necessary amount of pattern corners (usually ~20 are enough),
// press "Calibrate" button for performing camera calibration.

package es.uvigo.fran.detector2;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;

public class CameraCalibrationActivity extends FullscreenOpenCVCameraActivity {
    private static final String TAG = "CAMERA CALIBRATION";

    private CameraCalibrator mCalibrator;
    private OnCameraFrameRender mOnCameraFrameRender;
    private int mWidth;
    private int mHeight;

    private ImageButton mShutterView;

    @Override
    protected int getContentView() {
        return R.layout.activity_camera_calibration;
    }

    @Override
    protected boolean showHome() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int[] size = Utils.getCurrentVideoSize(this);
        getCameraView().setMaxFrameSize(size[0], size[1]);

        mShutterView = (ImageButton) findViewById(R.id.btn_shutter);
        mShutterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalibrator.addCorners();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_calibration, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.preview_mode).setEnabled(mCalibrator.isCalibrated());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setVisibility();
        switch (item.getItemId()) {
            case R.id.calibration:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
                item.setChecked(true);
                return true;
            case R.id.undistortion:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new UndistortionFrameRender(mCalibrator));
                item.setChecked(true);
                return true;
            case R.id.comparison:
                mOnCameraFrameRender =
                        new OnCameraFrameRender(new ComparisonFrameRender(mCalibrator, mWidth, mHeight, getResources()));
                item.setChecked(true);
                return true;
            case R.id.calibrate:
                final Resources res = getResources();
                if (mCalibrator.getCornersBufferSize() < 2) {
                    (Toast.makeText(this, res.getString(R.string.more_samples), Toast.LENGTH_SHORT)).show();
                    return true;
                }

                mOnCameraFrameRender = new OnCameraFrameRender(new PreviewFrameRender());
                new AsyncTask<Void, Void, Void>() {
                    private ProgressDialog calibrationProgress;

                    @Override
                    protected void onPreExecute() {
                        calibrationProgress = new ProgressDialog(CameraCalibrationActivity.this);
                        calibrationProgress.setTitle(res.getString(R.string.calibrating));
                        calibrationProgress.setMessage(res.getString(R.string.please_wait));
                        calibrationProgress.setCancelable(false);
                        calibrationProgress.setIndeterminate(true);
                        calibrationProgress.show();
                    }

                    @Override
                    protected Void doInBackground(Void... arg0) {
                        mCalibrator.calibrate();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        calibrationProgress.dismiss();
                        mCalibrator.clearCorners();
                        mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
                        String resultMessage = (mCalibrator.isCalibrated()) ?
                                res.getString(R.string.calibration_successful) + " " + mCalibrator.getAvgReprojectionError() :
                                res.getString(R.string.calibration_unsuccessful);
                        Toast.makeText(CameraCalibrationActivity.this, resultMessage, Toast.LENGTH_SHORT).show();

                        if (mCalibrator.isCalibrated()) {
                            int[] size = Utils.getCurrentVideoSize(CameraCalibrationActivity.this);
                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(CameraCalibrationActivity.this);
                            SharedPreferences.Editor editor = prefs.edit();

                            Mat cameraParams = mCalibrator.getCameraMatrix();
                            double[] bufferC = new double[9];
                            cameraParams.get(0, 0, bufferC);
                            Log.e(TAG, "nfx = " + bufferC[0] / size[0]);
                            editor.putString("camera_nfx", String.valueOf(bufferC[0] / size[0]));
                            Log.e(TAG, "nfy = " + bufferC[4] / size[1]);
                            editor.putString("camera_nfy", String.valueOf(bufferC[4] / size[1]));
                            Log.e(TAG, "ncx = " + bufferC[2] / size[0]);
                            editor.putString("camera_ncx", String.valueOf(bufferC[2] / size[0]));
                            Log.e(TAG, "ncy = " + bufferC[5] / size[1]);
                            editor.putString("camera_ncy", String.valueOf(bufferC[5] / size[1]));

                            Mat distCoeffs = mCalibrator.getDistortionCoefficients();
                            double[] bufferD = new double[(int) distCoeffs.total()];
                            distCoeffs.get(0, 0, bufferD);
                            editor.putString("camera_dcn", String.valueOf(bufferD.length));
                            for (int i = 0; i < bufferD.length; i++) {
                                Log.e(TAG, "dc" + i + " = " + bufferD[i]);
                                editor.putString("camera_dc" + i, String.valueOf(bufferD[i]));
                            }
                            editor.apply();
                        }
                    }
                }.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCameraViewStarted(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            mCalibrator = new CameraCalibrator(mWidth, mHeight);
            if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated();
            }
            mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
        }
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return mOnCameraFrameRender.render(inputFrame);
    }
}
