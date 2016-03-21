#ifndef __MODEL_H
#define __MODEL_H

#include <opencv2/core/core.hpp>

class Model
{
public:
    Model();

    inline std::vector<cv::Point3f> get_points() const { return points_; }
    inline cv::Mat get_descriptors() const { return descriptors_; }
    void add(const cv::Point3f& point3d, const cv::Mat&descriptor);

    void save(const std::string path) const;
    void load(const std::string path);

private:
    /** The list of 3D points on the model surface */
    std::vector<cv::Point3f> points_;
    /** The list of 2D points descriptors */
    cv::Mat descriptors_;
};

#endif /* MODEL_H_ */
