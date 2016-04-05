#include "Enroller.h"

#include <opencv2/calib3d/calib3d.hpp>

#include "Utils.h"
#include "matching.h"


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
    initialized_ = true;
}

vector<Point3f> Enroller::get_points() const
{
    return mesh_.getVertexList();
}

void Enroller::enroll(Mat& image, const vector<Point3f>& points3d, const vector<Point2f>& points2d)
{
    if (!mesh_loaded_ || !initialized_) return;

    // Estimate pose given the registered points
    bool is_correspondence = pnp_.estimatePose(
        points3d, points2d, SOLVEPNP_ITERATIVE);

    // Compute keypoints and descriptors
    vector<KeyPoint> keypoints = get_keypoints(image, detector_);
    Mat descriptors = get_descriptors(image, keypoints, extractor_);

    // Check if keypoints are on the surface of the registration image and add to the model
    vector<Point2f> inliers, outliers;
    model_ = Model();
    for (size_t i = 0; i < keypoints.size(); ++i) {
        Point2f point2d(keypoints[i].pt);
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
