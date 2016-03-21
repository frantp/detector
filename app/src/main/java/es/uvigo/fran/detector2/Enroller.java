package es.uvigo.fran.detector2;

import android.os.Environment;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.List;

public class Enroller {


    // Intrinsic camera parameters: UVC WEBCAM
    private static double f = 55;                           // focal length in mm
    private static double sx = 22.3, sy = 14.9;             // sensor size
    private static double width = 640, height = 480;        // image size

    private static double fx = width * f / sx;
    private static double fy = height * f / sy;
    private static double cx = width / 2;
    private static double cy = height / 2;

    //double fx = 910.3046264846927;
    //double fy = 910.3046264846927;
    //double cx = 639.5;
    //double cy = 383.5;

    // File paths
    private static String modelPath = Environment.getExternalStorageDirectory() +
            "/detector/cookies_ORB.yml";
    //        "/detector/model.txt";

    static {
        System.loadLibrary("ocv");
    }

    private final long nativeAddr;

    public Enroller() {
        nativeAddr = create(fx, fy, cx, cy);
    }

    public void loadMesh(String path) {
        loadMesh(nativeAddr, path);
    }

    public Point3[] getPoints() {
        double[] pointsX = getPointsX(nativeAddr);
        double[] pointsY = getPointsY(nativeAddr);
        double[] pointsZ = getPointsZ(nativeAddr);
        Point3[] points = new Point3[pointsX.length];
        for (int i = 0; i < pointsX.length; i++) {
            points[i] = new Point3(pointsX[i], pointsY[i], pointsZ[i]);
        }
        return points;
    }

    public void init() {
        init(nativeAddr);
    }

    public void enroll(Mat image, List<Point> points) {
        double[] pointsX = new double[points.size()];
        double[] pointsY = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsX[i] = points.get(i).x;
            pointsY[i] = points.get(i).y;
        }
        enroll(nativeAddr, image.getNativeObjAddr(), pointsX, pointsY);
    }

    public void saveModel(String path, boolean update) {
        saveModel(nativeAddr, path, update);
    }

    public void release() {
        destroy(nativeAddr);
    }

    public Enroller setOrbNumFeatures(int orbNumFeatures) {
        setorbNumFeatures(nativeAddr, orbNumFeatures);
        return this;
    }

    public Enroller setOrbScaleFactor(double orbScaleFactor) {
        setorbScaleFactor(nativeAddr, orbScaleFactor);
        return this;
    }

    public Enroller setOrbNumLevels(int orbNumLevels) {
        setorbNumLevels(nativeAddr, orbNumLevels);
        return this;
    }

    public Enroller setOrbEdgeThreshold(int orbEdgeThreshold) {
        setorbEdgeThreshold(nativeAddr, orbEdgeThreshold);
        return this;
    }

    public Enroller setOrbFirstLevel(int orbFirstLevel) {
        setorbFirstLevel(nativeAddr, orbFirstLevel);
        return this;
    }

    public Enroller setOrbWtaK(int orbWtaK) {
        setorbWtaK(nativeAddr, orbWtaK);
        return this;
    }

    public Enroller setOrbScoreType(int orbScoreType) {
        setorbScoreType(nativeAddr, orbScoreType);
        return this;
    }

    public Enroller setOrbPatchSize(int orbPatchSize) {
        setorbPatchSize(nativeAddr, orbPatchSize);
        return this;
    }

    public Enroller setOrbFastThreshold(int orbFastThreshold) {
        setorbFastThreshold(nativeAddr, orbFastThreshold);
        return this;
    }

    private static native long create(double fx, double fy, double cx, double cy);

    private static native void loadMesh(long addrThis, String path);

    private static native double[] getPointsX(long addrThis);

    private static native double[] getPointsY(long addrThis);

    private static native double[] getPointsZ(long addrThis);

    private static native void init(long addrThis);

    private static native void enroll(long addrThis, long addrImage,
                                      double[] pointsX, double[] pointsY);

    private static native void saveModel(long addrThis, String path, boolean update);

    private static native void destroy(long addrThis);

    private static native void setorbNumFeatures(long addrThis, int orbNumFeatures);

    private static native void setorbScaleFactor(long addrThis, double orbScaleFactor);

    private static native void setorbNumLevels(long addrThis, int orbNumLevels);

    private static native void setorbEdgeThreshold(long addrThis, int orbEdgeThreshold);

    private static native void setorbFirstLevel(long addrThis, int orbFirstLevel);

    private static native void setorbWtaK(long addrThis, int orbWtaK);

    private static native void setorbScoreType(long addrThis, int orbScoreType);

    private static native void setorbPatchSize(long addrThis, int orbPatchSize);

    private static native void setorbFastThreshold(long addrThis, int orbFastThreshold);
}
