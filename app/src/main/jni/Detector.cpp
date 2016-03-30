#include "Detector.h"

#include "Utils.h"
#include "matching.h"


Detector::Detector(double* params_camera)
    : pnp_(params_camera)
    , mesh_loaded_(false)
    , model_loaded_(false)
    , initialized_(false)
{
}

void Detector::load_mesh(const std::string& path)
{
    mesh_.load(path);
    mesh_loaded_ = true;
}

void Detector::load_model(const std::string& path)
{
    model_.load(path);
    model_loaded_ = true;
}

void Detector::init()
{
    detector_ = ORB::create(
        orb_num_features,
        orb_scale_factor,
        orb_num_levels,
        orb_edge_threshold,
        orb_first_level,
        orb_WTA_K,
        orb_score_type,
        orb_patch_size,
        orb_fast_threshold
    );
    extractor_ = detector_;
    Ptr<flann::IndexParams> indexParams = makePtr<flann::LshIndexParams>(6, 12, 1);
    Ptr<flann::SearchParams> searchParams = makePtr<flann::SearchParams>(50);
    matcher_ = makePtr<FlannBasedMatcher>(indexParams, searchParams);
    initialized_ = true;
}

void Detector::detect(Mat& image)
{
    if (!mesh_loaded_ || !model_loaded_ || !initialized_) return;

    // Robust matching between scene descriptors and model descriptors
    vector<KeyPoint> keypoints_image = get_keypoints(image, detector_);
    Mat descriptors_image = get_descriptors(image, keypoints_image, extractor_);
    vector<DMatch> matches = robust_match(descriptors_image, model_.get_descriptors(), matcher_,
        matching_ratio, fast_matching);

    // Find out the 2D/3D correspondences
    vector<Point2f> candidates2d;
    vector<Point3f> candidates3d;
    for (auto& match : matches)
    {
        candidates2d.push_back(keypoints_image[match.queryIdx].pt);
        candidates3d.push_back(model_.get_points()[match.trainIdx]);
    }

    Mat inliers_idx;
    vector<Point2f> inliers2d;
    bool good_measurement = false;
    if (matches.size() > 0) // None matches, then RANSAC crashes
    {
        // Estimate the pose using RANSAC approach
        pnp_.estimatePoseRANSAC(candidates3d, candidates2d, pnp_method, inliers_idx,
                                ransac_iter_count, ransac_reprojection_error, ransac_confidence);
        good_measurement = inliers_idx.rows >= inliers_threshold;

        // Catch the inliers keypoints to draw
        for (int i = 0; i < inliers_idx.rows; ++i)
        {
            inliers2d.push_back(candidates2d[inliers_idx.at<int>(i)]); // add i-inlier 2D point
        }
    }

    // Draw
    draw2DPoints(image, candidates2d, RED);
    draw2DPoints(image, inliers2d, BLUE);
    if(good_measurement)
    {
        drawObjectMesh(image, &mesh_, &pnp_, GREEN);
    }
    int inliers_int  = inliers_idx.rows;
    int outliers_int = (int)matches.size() - inliers_int;
    string inliers_str  = IntToString(inliers_int);
    string outliers_str = IntToString(outliers_int);
    drawText1(image, "Inliers:  " + inliers_str, BLUE);
    drawText2(image, "Outliers: " + outliers_str, RED);
}

#include <opencv2/imgproc.hpp>

double minth = 50;
double maxth = 100;

#include <android/log.h>
#define LOG_TAG "NATIVE"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

string type2str(int type) {
  string r;

  uchar depth = type & CV_MAT_DEPTH_MASK;
  uchar chans = 1 + (type >> CV_CN_SHIFT);

  switch ( depth ) {
    case CV_8U:  r = "8U"; break;
    case CV_8S:  r = "8S"; break;
    case CV_16U: r = "16U"; break;
    case CV_16S: r = "16S"; break;
    case CV_32S: r = "32S"; break;
    case CV_32F: r = "32F"; break;
    case CV_64F: r = "64F"; break;
    default:     r = "User"; break;
  }

  r += "C";
  r += (chans+'0');

  return r;
}

void Detector::contours(Mat& image)
{/*
    LOGD("TYPE: %s", type2str(image.type()).c_str());
    // Convert image to gray and blur it
    //blur(image_gray, image_gray, Size(3, 3));
    Mat image_rgb, image_hsv, image_filt;
    cvtColor(image, image_rgb, CV_RGBA2RGB);
    cvtColor(image_rgb, image_hsv, CV_RGB2HSV);
    Mat channel[4];
    split(image_hsv, channel);
    channel[2].setTo(127);
    merge(channel, 3, image_hsv);
    cvtColor(image_hsv, image_rgb, CV_HSV2BGR);
    cvtColor(image_rgb, image, CV_RGB2RGBA);

    for (int i = 0; i < image.rows; ++i) {
        for (int j = 0; j < image.cols; ++j) {
            image.at<cv::Vec4b>(i, j)[0] = image_hsv.at<Vec3b>(i, j)[0];
            image.at<cv::Vec4b>(i, j)[1] = image_hsv.at<Vec3b>(i, j)[1];
            image.at<cv::Vec4b>(i, j)[2] = image_hsv.at<Vec3b>(i, j)[2];
        }
    }
    inRange(image_hsv, Scalar(0, 0, 0), Scalar(), image_filt);
    return;*/

    Mat image_gray;
    cvtColor(image, image_gray, CV_RGBA2GRAY);
    blur(image_gray, image_gray, Size(3, 3));

    Mat canny_output;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    Canny(image_gray, canny_output, minth, maxth, 3);
    findContours(canny_output, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

    for (int i = 0; i < contours.size(); ++i)
    {
        drawContours(image, contours, i, RED, 2, 8, hierarchy, 0, Point());
    }
}
