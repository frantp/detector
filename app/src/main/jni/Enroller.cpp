#include "Enroller.h"

#include "Utils.h"
#include "matching.h"

#include <opencv2/imgproc.hpp>


Enroller::Enroller(const double* params_camera, double* dist_coeffs, std::size_t num_coeffs)
    : pnp_(params_camera, dist_coeffs, num_coeffs)
    , mesh_loaded_(false)
    , initialized_(false)
{
}

void Enroller::load_mesh(const std::string& path)
{
    mesh_.load(path);
    mesh_loaded_ = true;
}

void Enroller::init()
{
    switch (descriptor_alg) {
    default:
    case 0:
        alg_ = ORB::create(
            orb_params.num_features,
            orb_params.scale_factor,
            orb_params.num_levels,
            orb_params.edge_threshold,
            orb_params.first_level,
            orb_params.WTA_K,
            orb_params.score_type,
            orb_params.patch_size,
            orb_params.fast_threshold
        );
        break;
    case 1:
        alg_ = AKAZE::create(
            akaze_params.descriptor_type,
            akaze_params.descriptor_size,
            akaze_params.descriptor_channels,
            akaze_params.threshold,
            akaze_params.nOctaves,
            akaze_params.nOctaveLayers,
            akaze_params.diffusivity
        );
        break;
    }
    initialized_ = true;
}

vector<Point3f> Enroller::get_points() const
{
    return mesh_.getKeyVertices();
}

#include <android/log.h>
#define LOG_TAG "NATIVE"
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

const int LEVELS = 2;
const double SCALE = 0.5;

void Enroller::enroll(Mat& image, const vector<Point3f>& points3d, const vector<Point2f>& points2d)
{
    if (!mesh_loaded_ || !initialized_) return;

    Mat image_gray;
    cvtColor(image, image_gray, CV_RGBA2GRAY);

    // Estimate pose given the registered points
    bool is_correspondence = pnp_.estimatePose(points3d, points2d, pnp_method);
    if (!is_correspondence) return;

    vector<Point2f> inliers, outliers;
    model_ = Model();
    for (unsigned l = 0; l <= LEVELS; ++l)
    {
        if (l > 0)
        {
            //Mat temp = current_image.clone();
            //resize(temp, current_image, Size(0, 0), SCALE, SCALE, INTER_LANCZOS4);
            pyrDown(image_gray, image_gray);
        }

        // Compute keypoints and descriptors
        //vector<KeyPoint> keypoints = get_keypoints(image, detector_);
        //Mat descriptors = get_descriptors(image, keypoints, extractor_);
        vector<KeyPoint> keypoints;
        Mat descriptors;
        alg_->detectAndCompute(image_gray, noArray(), keypoints, descriptors);

        // Check if keypoints are on the surface of the registration image and add to the model
        for (size_t i = 0; i < keypoints.size(); ++i) {
            Point2f point2d(keypoints[i].pt);
            for (unsigned n = 0; n < l; ++n)
            {
                point2d.x *= 1 / SCALE;
                point2d.y *= 1 / SCALE;
            }
            Point3f point3d;
            bool on_surface = pnp_.backproject2DPoint(&mesh_, point2d, point3d);
            if (on_surface)
            {
                model_.add(point3d, descriptors.row(i));
                inliers.push_back(point2d);
            }
            else
            {
                outliers.push_back(point2d);
            }
        }
    }

    // Draw
    draw2DPoints(image, outliers, RED);
    draw2DPoints(image, inliers, BLUE);
    drawObjectMesh(image, &mesh_, &pnp_, GREEN);
    drawText1(image, "Inliers: " + IntToString((int)inliers.size()), GREEN);
    drawText2(image, "Outliers: " + IntToString((int)outliers.size()), RED);
}

void Enroller::save_model(const string& path, bool update)
{
    if (update && exists(path))
    {
        Model old_model;
        old_model.load(path);
        for (size_t i = 0; i < model_.get_points().size(); ++i)
        {
            old_model.add(model_.get_points()[i], model_.get_descriptors().row(i));
        }
        old_model.save(path);
    }
    else
    {
        model_.save(path);
    }
}
