#include "matching.h"

vector<DMatch> filter_ratio(
    const vector<vector<DMatch>>& matches,
    float ratio)
{
    vector<DMatch> good_matches;
    for (auto& match : matches)
    {
        if (match.size() > 1 &&
            match[0].distance / match[1].distance < ratio)
        {
            good_matches.push_back(match[0]);
        }
    }
    return good_matches;
}

vector<DMatch> filter_symmetry(
    const vector<DMatch>& matches12,
    const vector<DMatch>& matches21)
{
    vector<DMatch> good_matches;
    for (auto& match12 : matches12)
    {
        for (auto& match21 : matches21)
        {
            if (match12.queryIdx == match21.trainIdx &&
                match21.queryIdx == match12.trainIdx)
            {
                good_matches.push_back(match12);
                break;
            }
        }
    }
    return good_matches;
}

std::vector<DMatch> robust_match(
    const Mat& descriptors1,
    const Mat& descriptors2,
    const Ptr<DescriptorMatcher> matcher,
    float ratio,
    bool fast)
{
    if (descriptors1.empty() || descriptors2.empty())
    {
        return std::vector<DMatch>();
    }

    vector<vector<DMatch>> matches12;
    matcher->knnMatch(descriptors1, descriptors2, matches12, 2);
    vector<DMatch> good_matches12 = filter_ratio(matches12, ratio);

    if (fast) return good_matches12;

    std::vector<std::vector<DMatch>> matches21;
    matcher->knnMatch(descriptors2, descriptors1, matches21, 2);
    std::vector<DMatch> good_matches21 = filter_ratio(matches21, ratio);

    return filter_symmetry(good_matches12, good_matches21);
}
