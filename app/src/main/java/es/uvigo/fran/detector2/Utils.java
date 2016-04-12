package es.uvigo.fran.detector2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

abstract class Utils {
    private static String MODELS_FOLDER_NAME = "models";
    private static DecimalFormat NUM_FORMAT = new DecimalFormat("#.##");
    private static int FONT_FACE = Core.FONT_HERSHEY_SIMPLEX;
    private static Scalar BLACK = new Scalar(0, 0, 0);
    private static Scalar WHITE = new Scalar(255, 255, 255);

    public static File modelsFolder(Context context) {
        File modelsFolder = new File(context.getFilesDir(), MODELS_FOLDER_NAME);
        if (!modelsFolder.isDirectory()) {
            modelsFolder.mkdirs();
        }
        return modelsFolder;
    }

    public static String[] modelPaths(Context context) {
        File[] files = modelsFolder(context).listFiles();
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getPath();
        }
        return paths;
    }

    public static String[] modelNames(Context context) {
        File[] files = modelsFolder(context).listFiles();
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            int pos = name.lastIndexOf(".");
            if (pos > 0) {
                name = name.substring(0, pos);
            }
            names[i] = name;
        }
        return names;
    }

    public static String modelPath(Context context, String modelName) {
        return Utils.modelsFolder(context) + "/" + modelName + ".model";
    }

    public static String format(Point3 point) {
        return NUM_FORMAT.format(point.x) +
                "," + NUM_FORMAT.format(point.y) +
                "," + NUM_FORMAT.format(point.z);
    }

    private static int pointRadius(Mat image) {
        return Math.max(1, (int) Math.round(Math.min(image.rows(), image.cols()) * 0.01));
    }

    public static int pointThreshold(Mat image) {
        return pointRadius(image) * 10;
    }

    public static double fontScale(Mat image) {
        return Math.min(image.rows(), image.cols()) * 0.0009;
    }

    public static void drawPoint(Mat image, Point point, Scalar color) {
        Imgproc.circle(image, point, pointRadius(image), color, -1, 8, 0);
    }

    public static void drawText(Mat image, String text, Point pos, Scalar color, Point shift) {
        pos.x += shift.x;
        pos.y += shift.y;
        double scale = fontScale(image);
        int[] baseline = new int[1];
        Size size = Imgproc.getTextSize(text, FONT_FACE, scale, 1, baseline);
        int bs = baseline[0];
        Point leftBottom = new Point(pos.x - bs, pos.y + bs);
        Point rightTop = new Point(pos.x + size.width + bs, pos.y - size.height - bs);
        if (rightTop.x > image.width()) {
            double d = rightTop.x - leftBottom.x + 2 * shift.x;
            leftBottom.x -= d;
            rightTop.x -= d;
            pos.x -= d;
        }
        if (rightTop.y < 0) {
            double d = leftBottom.y - rightTop.y - 2 * shift.y;
            leftBottom.y += d;
            rightTop.y += d;
            pos.y += d;
        }
        Imgproc.rectangle(image, leftBottom, rightTop, color, Core.FILLED);
        Imgproc.putText(image, text, pos, FONT_FACE, scale, WHITE, 1);
    }

    public static void drawPointWithLabel(Mat image, Point point, String label, Scalar color) {
        int r = pointRadius(image);
        Point labelPos = new Point(point.x + r, point.y - r);
        drawPoint(image, point, color);
        drawText(image, label, labelPos, color.mul(new Scalar(0.5, 0.5, 0.5)), new Point(r, -r));
    }

    public static List<Camera.Size> getSupportedVideoSizes() {
        Camera camera = Camera.open();
        List<Camera.Size> sizes = camera.getParameters().getSupportedVideoSizes();
        camera.release();
        return sizes;
    }

    public static int[] getCurrentVideoSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String resolutionString = prefs.getString("detect_resolution",
                context.getResources().getString(R.string.pref_default_resolution));
        String[] frags = resolutionString.split("x");
        return new int[]{Integer.parseInt(frags[0]), Integer.parseInt(frags[1])};
    }
}
