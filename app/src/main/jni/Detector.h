#ifndef __DETECTOR_H
#define __DETECTOR_H

#include <opencv2/core.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/calib3d.hpp>

#include "Model.h"
#include "Mesh.h"
#include "PnPProblem.h"
#include "parameters.h"

using namespace std;
using namespace cv;

class Detector {
    public:
        Detector(const double* params_camera, double* dist_coeffs, std::size_t num_coeffs);
        void load_mesh(const string& path);
        void load_model(const string& path);
        void init();
        void detect(Mat& image);
        void contours(Mat& image);

        int descriptor_alg = 0;
        ORBParams orb_params;
        AKAZEParams akaze_params;

        // Matcher parameters
        float matching_ratio = 0.70;
        bool fast_matching = true;

        // RANSAC parameters
        int ransac_iter_count = 500;
        float ransac_reprojection_error = 2.0;
        double ransac_confidence = 0.95;

        // PnP parameters
        int pnp_method = SOLVEPNP_ITERATIVE;

        // Threshold
        int inliers_threshold = 15;

    private:
        bool mesh_loaded_, model_loaded_, initialized_;
        Model model_;
        Mesh mesh_;
        //Ptr<FeatureDetector> detector_;
        //Ptr<DescriptorExtractor> extractor_;
        Ptr<Feature2D> alg_;
        Ptr<DescriptorMatcher> matcher_;
        PnPProblem pnp_;
};

#endif
