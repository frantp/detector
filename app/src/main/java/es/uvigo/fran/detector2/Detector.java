package es.uvigo.fran.detector2;

import org.opencv.core.Mat;

class Detector {

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

    static {
        System.loadLibrary("ocv");
    }

    private final long nativeAddr;

    public Detector() {
        nativeAddr = create(fx, fy, cx, cy);
    }

    public void loadMesh(String path) {
        loadMesh(nativeAddr, path);
    }

    public void loadModel(String path) {
        loadModel(nativeAddr, path);
    }

    public void init() {
        init(nativeAddr);
    }

    public void detect(Mat frame) {
        detect(nativeAddr, frame.getNativeObjAddr());
    }

    public void release() {
        destroy(nativeAddr);
    }

    public Detector setOrbNumFeatures(int orbNumFeatures) {
        setorbNumFeatures(nativeAddr, orbNumFeatures);
        return this;
    }

    public Detector setOrbScaleFactor(double orbScaleFactor) {
        setorbScaleFactor(nativeAddr, orbScaleFactor);
        return this;
    }

    public Detector setOrbNumLevels(int orbNumLevels) {
        setorbNumLevels(nativeAddr, orbNumLevels);
        return this;
    }

    public Detector setOrbEdgeThreshold(int orbEdgeThreshold) {
        setorbEdgeThreshold(nativeAddr, orbEdgeThreshold);
        return this;
    }

    public Detector setOrbFirstLevel(int orbFirstLevel) {
        setorbFirstLevel(nativeAddr, orbFirstLevel);
        return this;
    }

    public Detector setOrbWtaK(int orbWtaK) {
        setorbWtaK(nativeAddr, orbWtaK);
        return this;
    }

    public Detector setOrbScoreType(int orbScoreType) {
        setorbScoreType(nativeAddr, orbScoreType);
        return this;
    }

    public Detector setOrbPatchSize(int orbPatchSize) {
        setorbPatchSize(nativeAddr, orbPatchSize);
        return this;
    }

    public Detector setOrbFastThreshold(int orbFastThreshold) {
        setorbFastThreshold(nativeAddr, orbFastThreshold);
        return this;
    }

    public Detector setMatchingRatio(double matchingRatio) {
        setmatchingRatio(nativeAddr, matchingRatio);
        return this;
    }

    public Detector setFastMatching(boolean fastMatching) {
        setfastMatching(nativeAddr, fastMatching);
        return this;
    }

    public Detector setRansacIterCount(int ransacIterCount) {
        setransacIterCount(nativeAddr, ransacIterCount);
        return this;
    }

    public Detector setRansacReprojectionError(double ransacReprojectionError) {
        setransacReprojectionError(nativeAddr, ransacReprojectionError);
        return this;
    }

    public Detector setRansacConfidence(double ransacConfidence) {
        setransacConfidence(nativeAddr, ransacConfidence);
        return this;
    }

    public Detector setPnpMethod(int pnpMethod) {
        setpnpMethod(nativeAddr, pnpMethod);
        return this;
    }

    public Detector setInliersThreshold(int inliersThreshold) {
        setinliersThreshold(nativeAddr, inliersThreshold);
        return this;
    }

    private static native long create(double fx, double fy, double cx, double cy);

    private static native void loadMesh(long addrThis, String path);

    private static native void loadModel(long addrThis, String path);

    private static native void init(long addrThis);

    private static native void detect(long addrThis, long addrImage);

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

    private static native void setmatchingRatio(long addrThis, double matchingRatio);

    private static native void setfastMatching(long addrThis, boolean fastMatching);

    private static native void setransacIterCount(long addrThis, int ransacIterCount);

    private static native void setransacReprojectionError(long addrThis, double ransacReprojectionError);

    private static native void setransacConfidence(long addrThis, double ransacConfidence);

    private static native void setpnpMethod(long addrThis, int pnpMethod);

    private static native void setinliersThreshold(long addrThis, int inliersThreshold);
}