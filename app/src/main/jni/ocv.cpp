#include <jni.h>

#include <opencv2/core/core.hpp>
#include <stdexcept>

#include "Enroller.h"
#include "Detector.h"

#include <android/log.h>
#define LOG_TAG "NATIVE"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT jlong JNICALL Java_es_uvigo_fran_detector2_Enroller_create(JNIEnv* env, jclass cls,
    jdoubleArray cameraParams, jdoubleArray distCoeffs)
{
    jdouble* camera_params = env->GetDoubleArrayElements(cameraParams, NULL);
    jdouble* dist_coeffs = env->GetDoubleArrayElements(distCoeffs, NULL);
    jsize num_coeffs = env->GetArrayLength(distCoeffs);
    Enroller* enroller = new Enroller(camera_params, dist_coeffs, num_coeffs);
    return (jlong)enroller;
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Enroller_loadMesh(JNIEnv* env, jclass cls,
    jlong addrThis, jstring path)
{
    Enroller* thiz = (Enroller*)addrThis;
    string mesh_path(env->GetStringUTFChars(path, NULL));
    thiz->load_mesh(mesh_path);
}

jdoubleArray getPoints_(JNIEnv* env, jlong addrThis, int index)
{
    Enroller* thiz = (Enroller*)addrThis;
    vector<Point3f> points = thiz->get_points();
    jdouble res[points.size()];
    for (unsigned int i = 0; i < points.size(); ++i)
    {
        switch (index) {
        case 0: res[i] = points[i].x; break;
        case 1: res[i] = points[i].y; break;
        case 2: res[i] = points[i].z; break;
        }
    }
    jdoubleArray result = env->NewDoubleArray(points.size());
    env->SetDoubleArrayRegion(result, 0, points.size(), res);
    return result;
}

JNIEXPORT jdoubleArray JNICALL Java_es_uvigo_fran_detector2_Enroller_getPointsX(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    return getPoints_(env, addrThis, 0);
}

JNIEXPORT jdoubleArray JNICALL Java_es_uvigo_fran_detector2_Enroller_getPointsY(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    return getPoints_(env, addrThis, 1);
}

JNIEXPORT jdoubleArray JNICALL Java_es_uvigo_fran_detector2_Enroller_getPointsZ(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    return getPoints_(env, addrThis, 2);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Enroller_init(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    Enroller* thiz = (Enroller*)addrThis;
    thiz->init();
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Enroller_enroll(JNIEnv* env, jclass cls,
    jlong addrThis, jlong addrImage,
    jdoubleArray points3dX, jdoubleArray points3dY, jdoubleArray points3dZ,
    jdoubleArray points2dX, jdoubleArray points2dY)
{
    Enroller* thiz = (Enroller*)addrThis;
    Mat image = *(Mat*)addrImage;
    jsize len = env->GetArrayLength(points3dX);
    if (len != env->GetArrayLength(points2dX)) {
        throw new std::invalid_argument("Points3d ans Points2d sizes differ");
    }

    vector<Point3f> points3d;
    jdouble* points3d_x = env->GetDoubleArrayElements(points3dX, NULL);
    jdouble* points3d_y = env->GetDoubleArrayElements(points3dY, NULL);
    jdouble* points3d_z = env->GetDoubleArrayElements(points3dZ, NULL);
    for (unsigned int i = 0; i < len; ++i) {
        points3d.push_back(Point3f(points3d_x[i], points3d_y[i], points3d_z[i]));
    }
    vector<Point2f> points2d;
    jdouble* points2d_x = env->GetDoubleArrayElements(points2dX, NULL);
    jdouble* points2d_y = env->GetDoubleArrayElements(points2dY, NULL);
    for (unsigned int i = 0; i < len; ++i) {
        points2d.push_back(Point2f(points2d_x[i], points2d_y[i]));
    }
    thiz->enroll(image, points3d, points2d);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Enroller_saveModel(JNIEnv* env, jclass cls,
    jlong addrThis, jstring path, jboolean update)
{
    Enroller* thiz = (Enroller*)addrThis;
    string model_path(env->GetStringUTFChars(path, NULL));
    thiz->save_model(model_path, update);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Enroller_destroy(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    Enroller* thiz = (Enroller*)addrThis;
    delete thiz;
}

JNIEXPORT jlong JNICALL Java_es_uvigo_fran_detector2_Detector_create(JNIEnv* env, jclass cls,
    jdoubleArray cameraParams, jdoubleArray distCoeffs)
{
    jdouble* camera_params = env->GetDoubleArrayElements(cameraParams, NULL);
    jdouble* dist_coeffs = env->GetDoubleArrayElements(distCoeffs, NULL);
    jsize num_coeffs = env->GetArrayLength(distCoeffs);
    Detector* detector = new Detector(camera_params, dist_coeffs, num_coeffs);
    return (jlong)detector;
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_loadMesh(JNIEnv* env, jclass cls,
    jlong addrThis, jstring path)
{
    Detector* thiz = (Detector*)addrThis;
    string mesh_path(env->GetStringUTFChars(path, NULL));
    thiz->load_mesh(mesh_path);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_loadModel(JNIEnv* env, jclass cls,
    jlong addrThis, jstring path)
{
    Detector* thiz = (Detector*)addrThis;
    string model_path(env->GetStringUTFChars(path, NULL));
    thiz->load_model(model_path);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_init(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    Detector* thiz = (Detector*)addrThis;
    thiz->init();
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_detect(JNIEnv* env, jclass cls,
    jlong addrThis, jlong addrImage)
{
    Detector* thiz = (Detector*)addrThis;
    Mat image = *(Mat*)addrImage;
    thiz->detect(image);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_contours(JNIEnv* env, jclass cls,
    jlong addrThis, jlong addrImage)
{
    Detector* thiz = (Detector*)addrThis;
    Mat image = *(Mat*)addrImage;
    thiz->contours(image);
}

JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_Detector_destroy(JNIEnv* env, jclass cls,
    jlong addrThis)
{
    Detector* thiz = (Detector*)addrThis;
    delete thiz;
}

#define SETTER(jcls, jtype, cvar, jvar) \
JNIEXPORT void JNICALL Java_es_uvigo_fran_detector2_##jcls##_set##jvar(JNIEnv* env, jclass cls, \
    jlong addrThis, jtype jvar) \
{ \
    jcls* thiz = (jcls*)addrThis; \
    thiz->cvar = jvar; \
}
#define SETTER_E(jtype, cvar, jvar) \
SETTER(Enroller, jtype, cvar, jvar)
#define SETTER_D(jtype, cvar, jvar) \
SETTER(Detector, jtype, cvar, jvar)
#define SETTER_ED(jtype, cvar, jvar) \
SETTER_E(jtype, cvar, jvar) \
SETTER_D(jtype, cvar, jvar)

SETTER_ED (jint,     orb_num_features, orbNumFeatures)
SETTER_ED (jdouble,  orb_scale_factor, orbScaleFactor)
SETTER_ED (jint,     orb_num_levels, orbNumLevels)
SETTER_ED (jint,     orb_edge_threshold, orbEdgeThreshold)
SETTER_ED (jint,     orb_first_level, orbFirstLevel)
SETTER_ED (jint,     orb_WTA_K, orbWtaK)
SETTER_ED (jint,     orb_score_type, orbScoreType)
SETTER_ED (jint,     orb_patch_size, orbPatchSize)
SETTER_ED (jint,     orb_fast_threshold, orbFastThreshold)
SETTER_D  (jdouble,  matching_ratio, matchingRatio)
SETTER_D  (jboolean, fast_matching, fastMatching)
SETTER_D  (jint,     ransac_iter_count, ransacIterCount)
SETTER_D  (jdouble,  ransac_reprojection_error, ransacReprojectionError)
SETTER_D  (jdouble,  ransac_confidence, ransacConfidence)
SETTER_D  (jint,     pnp_method, pnpMethod)
SETTER_D  (jint,     inliers_threshold, inliersThreshold)

}
