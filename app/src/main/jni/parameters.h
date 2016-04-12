#ifndef __PARAMETERS_H
#define __PARAMETERS_H

#include <opencv2/features2d.hpp>

class ORBParams {
public:
    int num_features = 500;
    float scale_factor = 1.2f;
    int num_levels = 8;
    int edge_threshold = 31;
    int first_level = 0;
    int WTA_K = 2;
    int score_type = cv::ORB::HARRIS_SCORE;
    int patch_size = 31;
    int fast_threshold = 20;
};

class AKAZEParams {
public:
    int descriptor_type = cv::AKAZE::DESCRIPTOR_MLDB;
    int descriptor_size = 0;
    int descriptor_channels = 3;
    float threshold = 0.001f;
    int nOctaves = 4;
    int nOctaveLayers = 4;
    int diffusivity = cv::KAZE::DIFF_PM_G2;
};

#endif
