#include "Model.h"

Model::Model()
{
}

void Model::add(const cv::Point3f& point, const cv::Mat& descriptor)
{
    points_.push_back(point);
    descriptors_.push_back(descriptor);
}

void Model::save(const std::string path) const
{
    cv::FileStorage storage(path, cv::FileStorage::WRITE);
    storage << "points_3d" << points_;
    storage << "descriptors" << descriptors_;
    storage.release();
}

void Model::load(const std::string path)
{
    cv::FileStorage storage(path, cv::FileStorage::READ);
    storage["points_3d"] >> points_;
    storage["descriptors"] >> descriptors_;
    storage.release();
}
