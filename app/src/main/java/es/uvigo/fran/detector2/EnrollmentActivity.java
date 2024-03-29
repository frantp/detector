package es.uvigo.fran.detector2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnrollmentActivity extends FullscreenOpenCVCameraActivity {

    private static final String TAG = "ENROLLMENT";

    private static final int SELECT_IMAGE = 100;
    private static final Scalar COLOR = new Scalar(0, 192, 0);

    private static String meshPath = Environment.getExternalStorageDirectory() +
            "/detector/mesh.ply";

    private Menu mMenu;

    boolean mCapturing;

    private Enroller mEnroller;
    private Mat mImage;
    private Point3[] mMeshPoints;
    private List<Point> mUserPoints;

    private double mScale;
    private int mOffsetX, mOffsetY;

    private TextView mTextView;
    private ImageButton mShutterView;
    private ImageView mImageView;

    public EnrollmentActivity() {
        super(R.layout.activity_enrollment, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int[] size = Utils.getCurrentVideoSize(this);
        getCameraView().setMaxFrameSize(size[0], size[1]);

        mCapturing = false;
        mImageView = (ImageView) findViewById(R.id.image_view);
        mTextView = (TextView) findViewById(R.id.text_view);
        mShutterView = (ImageButton) findViewById(R.id.btn_shutter);
        mTextView.setText("Select an image");
        mShutterView.setEnabled(false);
        mShutterView.setOnTouchListener(shutterListener);
        mUserPoints = new ArrayList<>();
    }

    /*
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            outState.putString("imagePath", mImagePath);
            double[] userPointsX = new double[mUserPoints.size()];
            double[] userPointsY = new double[mUserPoints.size()];
            for (int i = 0; i < mUserPoints.size(); i++) {
                userPointsX[i] = mUserPoints.get(i).x;
                userPointsY[i] = mUserPoints.get(i).y;
            }
            outState.putDoubleArray("userPointsX", userPointsX);
            outState.putDoubleArray("userPointsY", userPointsY);
            super.onSaveInstanceState(outState);
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            String imagePath = savedInstanceState.getString("imagePath");
            setImagePath(imagePath);
            double[] userPointsX = savedInstanceState.getDoubleArray("userPointsX");
            double[] userPointsY = savedInstanceState.getDoubleArray("userPointsY");
            int len = Math.min(userPointsX.length, userPointsY.length);
            mUserPoints.clear();
            for (int i = 0; i < len; i++) {
                mUserPoints.add(new Point(userPointsX[i], userPointsY[i]));
            }
            updateImage();
        }
    */
    private String getRealPathFromUri(Uri uri) {
        String result;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    String path = getRealPathFromUri(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    setImage(bitmap);
                }
                break;
        }
    }

    private void setImage(Bitmap bitmap) {
        mShutterView.setEnabled(false);
        mMenu.findItem(R.id.action_save).setVisible(false);
        mUserPoints.clear();
        initEnroller(bitmap.getWidth(), bitmap.getHeight());
        mImageView.setImageBitmap(bitmap);
        setScaleAndOffset(bitmap.getWidth(), bitmap.getHeight(),
                mImageView.getWidth(), mImageView.getHeight());
        mImageView.setOnTouchListener(addPointListener);
        updateText(mMeshPoints[0]);
    }

    private void setScaleAndOffset(int ix, int iy, int vx, int vy) {
        double sx = (double) vx / ix;
        double sy = (double) vy / iy;
        if (sx < sy) {
            mScale = sx;
            mOffsetX = 0;
            mOffsetY = (int) (vy - iy * mScale) / 2;
        } else {
            mScale = sy;
            mOffsetX = (int) (vx - ix * mScale) / 2;
            mOffsetY = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_enrollment, menu);
        mMenu = menu;
        mMenu.findItem(R.id.action_save).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mCapturing) {
            getCameraView().disableView();
            getCameraView().setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mCapturing = false;
        }
        switch (item.getItemId()) {
            case R.id.action_start: {
                mCapturing = true;
                mTextView.setText("Press shutter to capture image");
                mImageView.setVisibility(View.INVISIBLE);
                getCameraView().setVisibility(View.VISIBLE);
                getCameraView().enableView();
                break;
            }
            case R.id.action_load: {
                if (!isInitialized()) break;
                Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
                imagePickerIntent.setType("image/*");
                startActivityForResult(imagePickerIntent, SELECT_IMAGE);
                break;
            }
            case R.id.action_delete: {
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
                                setVisibility();
                                String modelPath = Utils.modelPath(EnrollmentActivity.this, modelNames[which]);
                                new File(modelPath).delete();
                                Toast.makeText(EnrollmentActivity.this,
                                        R.string.model_deleted,
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                setVisibility();
                break;
            }
            case R.id.action_save: {
                if (!isInitialized()) break;
                final String[] modelNames = Utils.modelNames(this);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.select_model_title)
                        .setNeutralButton(R.string.new_model, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setVisibility();
                                final EditText input = new EditText(EnrollmentActivity.this);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                new AlertDialog.Builder(EnrollmentActivity.this)
                                        .setTitle(R.string.new_model_title)
                                        .setView(input)
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                setVisibility();
                                                String modelPath = Utils.modelPath(EnrollmentActivity.this, input.getText().toString());
                                                mEnroller.saveModel(modelPath, false);
                                                Toast.makeText(EnrollmentActivity.this,
                                                        R.string.model_saved,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                setVisibility();
                            }
                        })
                        .setItems(modelNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String modelPath = Utils.modelPath(EnrollmentActivity.this, modelNames[which]);
                                final String[] options = new String[]{
                                        getString(R.string.update),
                                        getString(R.string.overwrite),
                                };
                                new AlertDialog.Builder(EnrollmentActivity.this)
                                        .setTitle(R.string.existing_model_title)
                                        .setSingleChoiceItems(options, 0, null)
                                        .setNegativeButton(R.string.cancel, null)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                setVisibility();
                                                ListView lv = ((AlertDialog) dialog).getListView();
                                                int checked = lv.getCheckedItemPosition();
                                                mEnroller.saveModel(modelPath, checked == 0);
                                                Toast.makeText(EnrollmentActivity.this,
                                                        R.string.model_saved,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                setVisibility();
                            }
                        })
                        .show();
                setVisibility();
                break;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, EnrollmentSettingsActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initEnroller(int width, int height) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int[] size = Utils.getCurrentVideoSize(this);
        double nfx = Double.parseDouble(prefs.getString("camera_nfx",
                getResources().getString(R.string.pref_default_camera_nfx)));
        double nfy = Double.parseDouble(prefs.getString("camera_nfy",
                getResources().getString(R.string.pref_default_camera_nfy)));
        double ncx = Double.parseDouble(prefs.getString("camera_ncx",
                getResources().getString(R.string.pref_default_camera_ncx)));
        double ncy = Double.parseDouble(prefs.getString("camera_ncy",
                getResources().getString(R.string.pref_default_camera_ncy)));
        double[] cameraParams = new double[]{
                nfx * size[0], nfy * size[1], ncx * size[0], ncy * size[1]
        };
        int dcn = Integer.parseInt(prefs.getString("camera_dcn",
                getResources().getString(R.string.pref_default_camera_dcn)));
        double[] distCoeffs = new double[dcn];
        for (int i = 0; i < dcn; i++) {
            distCoeffs[i] = Double.parseDouble(prefs.getString("camera_dc" + i,
                    getResources().getString(R.string.pref_default_camera_dci)));
        }
        mEnroller = new Enroller(cameraParams, distCoeffs);
        mEnroller.loadMesh(meshPath);

        mEnroller.setDescriptorAlg(Integer.parseInt(prefs.getString("enroll_descriptor_alg",
                getResources().getString(R.string.pref_default_descriptor_alg))));

        mEnroller.setOrbNumFeatures(Integer.parseInt(prefs.getString("enroll_orb_num_features",
                getResources().getString(R.string.pref_default_orb_num_features))));
        mEnroller.setOrbScaleFactor(Double.parseDouble(prefs.getString("enroll_orb_scale_factor",
                getResources().getString(R.string.pref_default_orb_scale_factor))));
        mEnroller.setOrbNumLevels(Integer.parseInt(prefs.getString("enroll_orb_num_levels",
                getResources().getString(R.string.pref_default_orb_num_levels))));
        mEnroller.setOrbEdgeThreshold(Integer.parseInt(prefs.getString("enroll_orb_edge_threshold",
                getResources().getString(R.string.pref_default_orb_edge_threshold))));
        mEnroller.setOrbFirstLevel(Integer.parseInt(prefs.getString("enroll_orb_first_level",
                getResources().getString(R.string.pref_default_orb_first_level))));
        mEnroller.setOrbWtaK(Integer.parseInt(prefs.getString("enroll_orb_wta_k",
                getResources().getString(R.string.pref_default_orb_wta_k))));
        mEnroller.setOrbScoreType(Integer.parseInt(prefs.getString("enroll_orb_score_type",
                getResources().getString(R.string.pref_default_orb_score_type))));
        mEnroller.setOrbPatchSize(Integer.parseInt(prefs.getString("enroll_orb_patch_size",
                getResources().getString(R.string.pref_default_orb_patch_size))));
        mEnroller.setOrbFastThreshold(Integer.parseInt(prefs.getString("enroll_orb_fast_threshold",
                getResources().getString(R.string.pref_default_orb_fast_threshold))));


        mEnroller.setAkazeDescriptorType(Integer.parseInt(prefs.getString("enroll_akaze_descriptor_type",
                getResources().getString(R.string.pref_default_akaze_descriptor_type))));
        mEnroller.setAkazeDescriptorSize(Integer.parseInt(prefs.getString("enroll_akaze_descriptor_size",
                getResources().getString(R.string.pref_default_akaze_descriptor_size))));
        mEnroller.setAkazeDescriptorChannels(Integer.parseInt(prefs.getString("enroll_akaze_descriptor_channels",
                getResources().getString(R.string.pref_default_akaze_descriptor_channels))));
        mEnroller.setAkazeThreshold(Double.parseDouble(prefs.getString("enroll_akaze_threshold",
                getResources().getString(R.string.pref_default_akaze_threshold))));
        mEnroller.setAkazeNOctaves(Integer.parseInt(prefs.getString("enroll_akaze_n_octaves",
                getResources().getString(R.string.pref_default_akaze_n_octaves))));
        mEnroller.setAkazeNOctaveLayers(Integer.parseInt(prefs.getString("enroll_akaze_n_octave_layers",
                getResources().getString(R.string.pref_default_akaze_n_octave_layers))));
        mEnroller.setAkazeDiffusivity(Integer.parseInt(prefs.getString("enroll_akaze_diffusivity",
                getResources().getString(R.string.pref_default_akaze_diffusivity))));

        mEnroller.setPnpMethod(Integer.parseInt(prefs.getString("enroll_pnp_method",
                getResources().getString(R.string.pref_default_pnp_method))));

        mEnroller.init();

        mMeshPoints = mEnroller.getPoints();
    }

    private View.OnTouchListener addPointListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Point touch = new Point(
                    (event.getX() - mOffsetX) / mScale,
                    (event.getY() - mOffsetY) / mScale
            );
            if (touch.x < 0 || (mImage != null && touch.x > mImage.width()) ||
                    touch.y < 0 || (mImage != null && touch.y > mImage.height())) {
                return false;
            }
            mMenu.findItem(R.id.action_save).setVisible(false);
            if (mUserPoints.isEmpty()) {
                Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                if (mImage != null) mImage.release();
                mImage = new Mat();
                org.opencv.android.Utils.bitmapToMat(bitmap, mImage);
            }
            if (mUserPoints.size() < mMeshPoints.length) {
                mUserPoints.add(touch);
                if (mUserPoints.size() < mMeshPoints.length) {
                    updateText(mMeshPoints[mUserPoints.size()]);
                } else {
                    mTextView.setText("Move points or press shutter to process");
                    mShutterView.setEnabled(true);
                }
                updateImage();
                return false;
            } else {
                // Get closest point
                double mind2 = Double.MAX_VALUE;
                Point minp = null;
                for (Point p : mUserPoints) {
                    double dx = touch.x - p.x;
                    double dy = touch.y - p.y;
                    double d2 = dx * dx + dy * dy;
                    if (d2 < mind2) {
                        mind2 = d2;
                        minp = p;
                    }
                }

                // Threshold
                int th = Utils.pointThreshold(mImage);
                if (mind2 < th * th) {
                    minp.x = touch.x;
                    minp.y = touch.y;
                    updateImage();
                }
                mShutterView.setEnabled(true);
                return true;
            }
        }
    };

    private View.OnTouchListener shutterListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mShutterView.setEnabled(false);
            if (mCapturing) {
                mCapturing = false;
                getCameraView().disableView();
                getCameraView().setVisibility(View.INVISIBLE);
                mImageView.setVisibility(View.VISIBLE);
                Bitmap bitmap = Bitmap.createBitmap(mImage.cols(), mImage.rows(),
                        Bitmap.Config.ARGB_8888);
                org.opencv.android.Utils.matToBitmap(mImage, bitmap);
                setImage(bitmap);
            } else {
                Toast.makeText(EnrollmentActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                mTextView.setText("Processing...");
                new AsyncEnrollUpdate().execute();
            }
            return false;
        }
    };

    private void updateText(Point3 p) {
        mTextView.setText("Point (" + Utils.format(p) + ")");
    }

    public void updateImage() {
        Mat displayImage = mImage.clone();
        drawPoints(displayImage);
        updateImage(displayImage);
        displayImage.release();
    }

    private void updateImage(Mat image) {
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(image, bitmap);
        mImageView.setImageBitmap(bitmap);
    }

    private void drawPoints(Mat image) {
        for (int i = 0; i < mUserPoints.size(); i++) {
            Utils.drawPointWithLabel(image, mUserPoints.get(i), Utils.format(mMeshPoints[i]), COLOR);
        }
    }

    private class AsyncEnrollUpdate extends AsyncTask<Void, Void, Void> {
        private Mat displayImage;

        @Override
        protected Void doInBackground(Void... params) {
            displayImage = mImage.clone();
            mEnroller.enroll(displayImage, Arrays.asList(mMeshPoints), mUserPoints);
            drawPoints(displayImage);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateImage(displayImage);
            displayImage.release();
            Toast.makeText(EnrollmentActivity.this, "Done", Toast.LENGTH_SHORT).show();
            mTextView.setText("Save the model or move the points again");
            mMenu.findItem(R.id.action_save).setVisible(true);
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mShutterView.post(mEnableShutterRunnable);
        Mat image = inputFrame.rgba();
        mImage = image.clone();
        return image;
    }

    private final Runnable mEnableShutterRunnable = new Runnable() {
        @Override
        public void run() {
            mShutterView.setEnabled(true);
        }
    };
}
