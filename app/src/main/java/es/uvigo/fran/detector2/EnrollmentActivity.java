package es.uvigo.fran.detector2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentActivity extends FullscreenOpenCVActivity {

    private static final String TAG = "ENROLLMENT";

    private static final int SELECT_IMAGE = 100;

    //private final String imgPath = Environment.getExternalStorageDirectory() +
    //        "/detector/image.jpg";

    private static String meshPath = Environment.getExternalStorageDirectory() +
            "/detector/mesh.ply";

    private Menu mMenu;

    private Enroller mEnroller;
    private String mImagePath;
    private Mat mImage;
    private Point3[] mMeshPoints;
    private List<Point> mUserPoints;

    private double mScale;
    private int mOffsetX, mOffsetY;

    private TextView mTextView;
    private ImageButton mShutterView;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_enrollment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mImageView = (ImageView) findViewById(R.id.image_view);
        mTextView = (TextView) findViewById(R.id.text_view);
        mShutterView = (ImageButton) findViewById(R.id.btn_shutter);
        mTextView.setText("Select an image");
        mShutterView.setEnabled(false);
        mUserPoints = new ArrayList<>();
    }

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
                    mMenu.findItem(R.id.action_save).setVisible(false);
                    String path = getRealPathFromUri(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    mImageView.setImageBitmap(bitmap);
                    mImagePath = path;
                    setScaleAndOffset(bitmap.getWidth(), bitmap.getHeight(),
                            mImageView.getWidth(), mImageView.getHeight());
                    mImageView.setOnTouchListener(addPointListener);
                    updateText(mMeshPoints[0]);
                }
                break;
        }
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
        Log.e(TAG, "SCALE: " + mScale + ", xOFFSET: " + mOffsetX + ", yOFFSET: " + mOffsetY);
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
        switch (item.getItemId()) {
            case R.id.action_new:
                if (!isInitialized()) break;
                mShutterView.setEnabled(false);
                mUserPoints.clear();
                Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
                imagePickerIntent.setType("image/*");
                startActivityForResult(imagePickerIntent, SELECT_IMAGE);
                break;
            case R.id.action_save:
                if (!isInitialized()) break;
                final String[] modelNames = Utils.modelNames(this);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.select_model_title)
                        .setNeutralButton(R.string.new_model, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                                String modelPath = Utils.modelPath(EnrollmentActivity.this, input.getText().toString());
                                                mEnroller.saveModel(modelPath, false);
                                                Toast.makeText(EnrollmentActivity.this,
                                                        R.string.model_saved,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
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
                                                ListView lv = ((AlertDialog) dialog).getListView();
                                                int checked = lv.getCheckedItemPosition();
                                                mEnroller.saveModel(modelPath, checked == 0);
                                                Toast.makeText(EnrollmentActivity.this,
                                                        R.string.model_saved,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, EnrollmentSettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEnroller = new Enroller();
        mEnroller.loadMesh(meshPath);
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
        mEnroller.init();
        mMeshPoints = mEnroller.getPoints();
        mShutterView.setOnTouchListener(processListener);
    }

    private View.OnTouchListener addPointListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mUserPoints.isEmpty()) {
                if (mImagePath != null) {
                    Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                    mImage = new Mat();
                    org.opencv.android.Utils.bitmapToMat(bitmap, mImage);
                }
            }
            mUserPoints.add(new Point(
                    (event.getX() - mOffsetX) / mScale,
                    (event.getY() - mOffsetY) / mScale
            ));
            updateImage(mUserPoints);
            if (mUserPoints.size() < mMeshPoints.length) {
                updateText(mMeshPoints[mUserPoints.size()]);
            } else {
                mImageView.setOnTouchListener(null);
                mShutterView.setEnabled(true);
            }
            return false;
        }
    };

    private View.OnTouchListener processListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Toast.makeText(EnrollmentActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
            mTextView.setText("Processing...");
            Mat displayImage = mImage.clone();
            mEnroller.enroll(displayImage, mUserPoints);
            updateImage(displayImage);
            mTextView.setText("Done");
            mMenu.findItem(R.id.action_save).setVisible(true);
            return false;
        }
    };

    private void updateText(Point3 p) {
        mTextView.setText("Point (" + p.x + ", " + p.y + ", " + p.z + ")");
    }

    public void updateImage(List<Point> points) {
        Mat displayImage = mImage.clone();
        for (Point p : points) {
            Imgproc.circle(displayImage, p, 4, new Scalar(255, 0, 0), -1, 8, 0);
        }
        updateImage(displayImage);
    }

    private void updateImage(Mat image) {
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        org.opencv.android.Utils.matToBitmap(image, bitmap);
        mImageView.setImageBitmap(bitmap);
    }
}
