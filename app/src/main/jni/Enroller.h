#ifndef __ENROLLER_H
#define __ENROLLER_H

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>

#include "Model.h"
#include "Mesh.h"
#include "PnPProblem.h"

using namespace std;
using namespace cv;

class Enroller {
    public:
        Enroller(double* params_camera);
        void load_mesh(const string& path);
        void init();
        vector<Point3f> get_points() const;
        void enroll(Mat& image, const vector<Point2f>& points);
        void save_model(const string& path, bool update);

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

    private:
        bool mesh_loaded_, initialized_;
        Mesh mesh_;
        Model model_;
        Ptr<FeatureDetector> detector_;
        Ptr<DescriptorExtractor> extractor_;
        PnPProblem pnp_;
};

#endif
