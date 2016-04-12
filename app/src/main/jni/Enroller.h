#ifndef __ENROLLER_H
#define __ENROLLER_H

#include <opencv2/core.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/calib3d.hpp>

#include "Model.h"
#include "Mesh.h"
#include "PnPProblem.h"
#include "parameters.h"

using namespace std;
using namespace cv;

class Enroller {
    public:
        Enroller(const double* params_camera, double* dist_coeffs, std::size_t num_coeffs);
        void load_mesh(const string& path);
        void init();
        vector<Point3f> get_points() const;
        void enroll(Mat& image, const vector<Point3f>& points3d, const vector<Point2f>& points2d);
        void save_model(const string& path, bool update);

        int descriptor_alg = 0;
        ORBParams orb_params;
        AKAZEParams akaze_params;

        // PnP parameters
        int pnp_method = SOLVEPNP_ITERATIVE;

    private:
        bool mesh_loaded_, initialized_;
        Mesh mesh_;
        Model model_;
        //Ptr<FeatureDetector> detector_;
        //Ptr<DescriptorExtractor> extractor_;
        Ptr<Feature2D> alg_;
        PnPProblem pnp_;
};

#endif
