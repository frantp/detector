package es.uvigo.fran.detector2;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.List;

public class Enroller {

    static {
        System.loadLibrary("ocv");
    }

    private final long nativeAddr;

    public Enroller(double[] cameraParams, double[] distCoeffs) {
        nativeAddr = create(cameraParams, distCoeffs);
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

    public void enroll(Mat image, List<Point3> points3d, List<Point> points2d) {
        double[] points3dX = new double[points3d.size()];
        double[] points3dY = new double[points3d.size()];
        double[] points3dZ = new double[points3d.size()];
        for (int i = 0; i < points3d.size(); i++) {
            points3dX[i] = points3d.get(i).x;
            points3dY[i] = points3d.get(i).y;
            points3dZ[i] = points3d.get(i).z;
        }
        double[] points2dX = new double[points2d.size()];
        double[] points2dY = new double[points2d.size()];
        for (int i = 0; i < points2d.size(); i++) {
            points2dX[i] = points2d.get(i).x;
            points2dY[i] = points2d.get(i).y;
        }
        enroll(nativeAddr, image.getNativeObjAddr(),
                points3dX, points3dY, points3dZ, points2dX, points2dY);
    }

    public void saveModel(String path, boolean update) {
        saveModel(nativeAddr, path, update);
    }

    public void release() {
        destroy(nativeAddr);
    }

    public Enroller setDescriptorAlg(int descriptorAlg) {
        setdescriptorAlg(nativeAddr, descriptorAlg);
        return this;
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

    public Enroller setAkazeDescriptorType(int akazeDescriptorType) {
        setakazeDescriptorType(nativeAddr, akazeDescriptorType);
        return this;
    }

    public Enroller setAkazeDescriptorSize(int akazeDescriptorSize) {
        setakazeDescriptorSize(nativeAddr, akazeDescriptorSize);
        return this;
    }

    public Enroller setAkazeDescriptorChannels(int akazeDescriptorChannels) {
        setakazeDescriptorChannels(nativeAddr, akazeDescriptorChannels);
        return this;
    }

    public Enroller setAkazeThreshold(double akazeThreshold) {
        setakazeThreshold(nativeAddr, akazeThreshold);
        return this;
    }

    public Enroller setAkazeNOctaves(int akazeNOctaves) {
        setakazeNOctaves(nativeAddr, akazeNOctaves);
        return this;
    }

    public Enroller setAkazeNOctaveLayers(int akazeNOctaveLayers) {
        setakazeNOctaveLayers(nativeAddr, akazeNOctaveLayers);
        return this;
    }

    public Enroller setAkazeDiffusivity(int akazeDiffusivity) {
        setakazeDiffusivity(nativeAddr, akazeDiffusivity);
        return this;
    }

    public Enroller setPnpMethod(int pnpMethod) {
        setpnpMethod(nativeAddr, pnpMethod);
        return this;
    }

    private static native long create(double[] cameraParams, double[] distCoeffs);

    private static native void loadMesh(long addrThis, String path);

    private static native double[] getPointsX(long addrThis);

    private static native double[] getPointsY(long addrThis);

    private static native double[] getPointsZ(long addrThis);

    private static native void init(long addrThis);

    private static native void enroll(long addrThis, long addrImage,
                                      double[] points3dX, double[] points3dY, double[] points3dZ,
                                      double[] points2dX, double[] points2dY);

    private static native void saveModel(long addrThis, String path, boolean update);

    private static native void destroy(long addrThis);

    private static native void setdescriptorAlg(long addrThis, int descriptorAlg);

    private static native void setorbNumFeatures(long addrThis, int orbNumFeatures);

    private static native void setorbScaleFactor(long addrThis, double orbScaleFactor);

    private static native void setorbNumLevels(long addrThis, int orbNumLevels);

    private static native void setorbEdgeThreshold(long addrThis, int orbEdgeThreshold);

    private static native void setorbFirstLevel(long addrThis, int orbFirstLevel);

    private static native void setorbWtaK(long addrThis, int orbWtaK);

    private static native void setorbScoreType(long addrThis, int orbScoreType);

    private static native void setorbPatchSize(long addrThis, int orbPatchSize);

    private static native void setorbFastThreshold(long addrThis, int orbFastThreshold);

    private static native void setakazeDescriptorType(long addrThis, int akazeDescriptorType);

    private static native void setakazeDescriptorSize(long addrThis, int akazeDescriptorSize);

    private static native void setakazeDescriptorChannels(long addrThis, int akazeDescriptorChannels);

    private static native void setakazeThreshold(long addrThis, double akazeThreshold);

    private static native void setakazeNOctaves(long addrThis, int akazeNOctaves);

    private static native void setakazeNOctaveLayers(long addrThis, int akazeNOctaveLayers);

    private static native void setakazeDiffusivity(long addrThis, int akazeDiffusivity);

    private static native void setpnpMethod(long addrThis, int pnpMethod);
}
