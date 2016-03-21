#ifndef __MATCHING_H
#define __MATCHING_H

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

inline vector<KeyPoint> get_keypoints(
    const Mat& image,
    const Ptr<FeatureDetector> detector)
{
    vector<KeyPoint> keypoints;
    detector->detect(image, keypoints);
    return keypoints;
}

inline Mat get_descriptors(
    const Mat& image,
    vector<KeyPoint>& keypoints,
    const Ptr<DescriptorExtractor> extractor)
{
    Mat descriptors;
    extractor->compute(image, keypoints, descriptors);
    return descriptors;
}

std::vector<DMatch> robust_match(
    const Mat& descriptors1,
    const Mat& descriptors2,
    const Ptr<DescriptorMatcher> matcher,
    float ratio,
    bool fast);

#endif
