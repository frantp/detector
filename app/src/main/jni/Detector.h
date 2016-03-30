#ifndef __DETECTOR_H
#define __DETECTOR_H

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d/calib3d.hpp>

#include "Model.h"
#include "Mesh.h"
#include "PnPProblem.h"

using namespace std;
using namespace cv;

class Detector {
    public:
        Detector(double* params_camera);
        void load_mesh(const string& path);
        void load_model(const string& path);
        void init();
        void detect(Mat& image);
        void contours(Mat& image);

        // ORB parameters
        int orb_num_features = 500;
		float orb_scale_factor = 1.2f;
		int orb_num_levels = 8;
		int orb_edge_threshold = 31;
		int orb_first_level = 0;
		int orb_WTA_K = 2;
		int orb_score_type = ORB::HARRIS_SCORE;
		int orb_patch_size = 31;
		int orb_fast_threshold = 20;

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
        Ptr<FeatureDetector> detector_;
        Ptr<DescriptorExtractor> extractor_;
        Ptr<DescriptorMatcher> matcher_;
        PnPProblem pnp_;
};

#endif
