package es.uvigo.fran.detector2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class MainActivity extends FullscreenOpenCVCameraActivity {

    private static final String TAG = "MAIN";
    private Detector mDetector;
    private boolean mStarted;

    private TextView mTextView;
    private ImageButton mShutterView;


    private static String meshPath = Environment.getExternalStorageDirectory() +
            "/detector/mesh.ply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStarted = savedInstanceState.getBoolean("started");
        } else {
            mStarted = false;
        }

        mTextView = (TextView) findViewById(R.id.text_view);
        mShutterView = (ImageButton) findViewById(R.id.btn_shutter);
        mTextView.setText(mStarted ? R.string.detect_guide_stop : R.string.detect_guide_start);
        mShutterView.setEnabled(mStarted);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("started", mStarted);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_start_stop);
        if (mStarted) {
            item.setIcon(android.R.drawable.ic_media_pause);
            item.setTitle(R.string.action_stop);
        } else {
            item.setIcon(android.R.drawable.ic_media_play);
            item.setTitle(R.string.action_start);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_stop:
                if (!isInitialized()) break;
                mStarted = !mStarted;
                if (mStarted) {
                    item.setIcon(android.R.drawable.ic_media_pause);
                    item.setTitle(R.string.action_stop);
                } else {
                    item.setIcon(android.R.drawable.ic_media_play);
                    item.setTitle(R.string.action_start);
                }
                mTextView.setText(
                        mStarted ? R.string.detect_guide_stop : R.string.detect_guide_start);
                mShutterView.setEnabled(mStarted);
                break;
            case R.id.action_select_model:
                if (!isInitialized()) break;
                final String[] modelNames = Utils.modelNames(this);
                if (modelNames.length == 0) {
                    Toast.makeText(this, R.string.no_models, Toast.LENGTH_SHORT).show();
                    break;
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.select_model_title)
                        .setItems(modelNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String modelPath = Utils.modelPath(MainActivity.this, modelNames[which]);
                                mDetector.loadModel(modelPath);
                                Toast.makeText(MainActivity.this,
                                        R.string.model_loaded,
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                break;
            case R.id.action_enroll:
                if (!isInitialized()) break;
                startActivity(new Intent(this, EnrollmentActivity.class));
                break;
            case R.id.action_calibrate:
                if (!isInitialized()) break;
                startActivity(new Intent(this, CameraCalibrationActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, MainSettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void init() {
        super.init();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDetector = new Detector();
        mDetector.loadMesh(meshPath);
        mDetector.setOrbNumFeatures(Integer.parseInt(prefs.getString("detect_orb_num_features",
                getResources().getString(R.string.pref_default_orb_num_features))));
        mDetector.setOrbScaleFactor(Double.parseDouble(prefs.getString("detect_orb_scale_factor",
                getResources().getString(R.string.pref_default_orb_scale_factor))));
        mDetector.setOrbNumLevels(Integer.parseInt(prefs.getString("detect_orb_num_levels",
                getResources().getString(R.string.pref_default_orb_num_levels))));
        mDetector.setOrbEdgeThreshold(Integer.parseInt(prefs.getString("detect_orb_edge_threshold",
                getResources().getString(R.string.pref_default_orb_edge_threshold))));
        mDetector.setOrbFirstLevel(Integer.parseInt(prefs.getString("detect_orb_first_level",
                getResources().getString(R.string.pref_default_orb_first_level))));
        mDetector.setOrbWtaK(Integer.parseInt(prefs.getString("detect_orb_wta_k",
                getResources().getString(R.string.pref_default_orb_wta_k))));
        mDetector.setOrbScoreType(Integer.parseInt(prefs.getString("detect_orb_score_type",
                getResources().getString(R.string.pref_default_orb_score_type))));
        mDetector.setOrbPatchSize(Integer.parseInt(prefs.getString("detect_orb_patch_size",
                getResources().getString(R.string.pref_default_orb_patch_size))));
        mDetector.setOrbFastThreshold(Integer.parseInt(prefs.getString("detect_orb_fast_threshold",
                getResources().getString(R.string.pref_default_orb_fast_threshold))));
        mDetector.setMatchingRatio(Double.parseDouble(prefs.getString("detect_matching_ratio",
                getResources().getString(R.string.pref_default_matching_ratio))));
        mDetector.setFastMatching(prefs.getBoolean("detect_fast_matching",
                getResources().getBoolean(R.bool.pref_default_fast_matching)));
        mDetector.setRansacIterCount(Integer.parseInt(prefs.getString("detect_ransac_iter_count",
                getResources().getString(R.string.pref_default_ransac_iter_count))));
        mDetector.setRansacReprojectionError(Double.parseDouble(prefs.getString("detect_ransac_reprojection_error",
                getResources().getString(R.string.pref_default_ransac_reprojection_error))));
        mDetector.setRansacConfidence(Double.parseDouble(prefs.getString("detect_ransac_confidence",
                getResources().getString(R.string.pref_default_ransac_confidence))));
        mDetector.setPnpMethod(Integer.parseInt(prefs.getString("detect_pnp_method",
                getResources().getString(R.string.pref_default_pnp_method))));
        mDetector.setInliersThreshold(Integer.parseInt(prefs.getString("detect_inliers_threshold",
                getResources().getString(R.string.pref_default_inliers_threshold))));
        mDetector.init();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if (mStarted) mDetector.detect(frame);
        return frame;
    }
}
