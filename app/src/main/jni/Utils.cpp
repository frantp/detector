/*
 * Utils.cpp
 *
 *  Created on: Mar 28, 2014
 *      Author: Edgar Riba
 */

#include <iostream>
#include <cmath>

#include "PnPProblem.h"
#include "Utils.h"

#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>

// For text
int fontFace = cv::FONT_ITALIC;

// For circles
int lineType = 8;

int d_(const cv::Mat& image)
{
    return std::max(1, (int) round(std::min(image.rows, image.cols) * 0.005));
}

int t_(const cv::Mat& image)
{
    return std::max(1, (int) round(std::min(image.rows, image.cols) * 0.002));
}

double s_(const cv::Mat& image)
{
    return std::min(image.rows, image.cols) * 0.0012;
}

// Draw a text with the number of entered points
void drawText1(cv::Mat image, std::string text, cv::Scalar color)
{
    int d = d_(image);
    cv::putText(image, text, cv::Point(d, 25 * d), fontFace, s_(image), color, t_(image), 8);
}

// Draw a text with the number of entered points
void drawText2(cv::Mat image, std::string text, cv::Scalar color)
{
    int d = d_(image);
    cv::putText(image, text, cv::Point(d, 33 * d), fontFace, s_(image), color, t_(image), 8);
}

// Draw only the 2D points
void draw2DPoints(cv::Mat image, std::vector<cv::Point2f>& list_points, cv::Scalar color)
{
    for (size_t i = 0; i < list_points.size(); ++i)
    {
        cv::Point2f point_2d = list_points[i];

        // Draw Selected points
        cv::circle(image, point_2d, d_(image), color, -1, lineType);
    }
}

// Draw the object mesh
void drawObjectMesh(cv::Mat image, const Mesh* mesh, PnPProblem* pnpProblem, cv::Scalar color)
{
    int t = t_(image);
    std::vector<std::vector<int> > list_triangles = mesh->getTriangles();
    for (size_t i = 0; i < list_triangles.size(); i++)
    {
        std::vector<int> tmp_triangle = list_triangles.at(i);

        cv::Point3f point_3d_0 = mesh->getVertex(tmp_triangle[0]);
        cv::Point3f point_3d_1 = mesh->getVertex(tmp_triangle[1]);
        cv::Point3f point_3d_2 = mesh->getVertex(tmp_triangle[2]);

        cv::Point2f point_2d_0 = pnpProblem->backproject3DPoint(point_3d_0);
        cv::Point2f point_2d_1 = pnpProblem->backproject3DPoint(point_3d_1);
        cv::Point2f point_2d_2 = pnpProblem->backproject3DPoint(point_3d_2);

        cv::line(image, point_2d_0, point_2d_1, color, t);
        cv::line(image, point_2d_1, point_2d_2, color, t);
        cv::line(image, point_2d_2, point_2d_0, color, t);
    }
}

// Computes the norm of the translation error
double get_translation_error(const cv::Mat& t_true, const cv::Mat& t)
{
    return cv::norm(t_true - t);
}

// Computes the norm of the rotation error
double get_rotation_error(const cv::Mat& R_true, const cv::Mat& R)
{
    cv::Mat error_vec, error_mat;
    error_mat = R_true * cv::Mat(R.inv()).mul(-1);
    cv::Rodrigues(error_mat, error_vec);
    return cv::norm(error_vec);
}

// Converts a given Rotation Matrix to Euler angles
cv::Mat rot2euler(const cv::Mat& rotationMatrix)
{
  cv::Mat euler(3,1,CV_64F);

  double m00 = rotationMatrix.at<double>(0,0);
  double m02 = rotationMatrix.at<double>(0,2);
  double m10 = rotationMatrix.at<double>(1,0);
  double m11 = rotationMatrix.at<double>(1,1);
  double m12 = rotationMatrix.at<double>(1,2);
  double m20 = rotationMatrix.at<double>(2,0);
  double m22 = rotationMatrix.at<double>(2,2);

  double x, y, z;

  // Assuming the angles are in radians.
  if (m10 > 0.998) { // singularity at north pole
    x = 0;
    y = CV_PI/2;
    z = atan2(m02,m22);
  }
  else if (m10 < -0.998) { // singularity at south pole
    x = 0;
    y = -CV_PI/2;
    z = atan2(m02,m22);
  }
  else
  {
    x = atan2(-m12,m11);
    y = asin(m10);
    z = atan2(-m20,m00);
  }

  euler.at<double>(0) = x;
  euler.at<double>(1) = y;
  euler.at<double>(2) = z;

  return euler;
}

// Converts a given Euler angles to Rotation Matrix
cv::Mat euler2rot(const cv::Mat& euler)
{
  cv::Mat rotationMatrix(3,3,CV_64F);

  double x = euler.at<double>(0);
  double y = euler.at<double>(1);
  double z = euler.at<double>(2);

  // Assuming the angles are in radians.
  double ch = cos(z);
  double sh = sin(z);
  double ca = cos(y);
  double sa = sin(y);
  double cb = cos(x);
  double sb = sin(x);

  double m00, m01, m02, m10, m11, m12, m20, m21, m22;

  m00 = ch * ca;
  m01 = sh*sb - ch*sa*cb;
  m02 = ch*sa*sb + sh*cb;
  m10 = sa;
  m11 = ca*cb;
  m12 = -ca*sb;
  m20 = -sh*ca;
  m21 = sh*sa*cb + ch*sb;
  m22 = -sh*sa*sb + ch*cb;

  rotationMatrix.at<double>(0,0) = m00;
  rotationMatrix.at<double>(0,1) = m01;
  rotationMatrix.at<double>(0,2) = m02;
  rotationMatrix.at<double>(1,0) = m10;
  rotationMatrix.at<double>(1,1) = m11;
  rotationMatrix.at<double>(1,2) = m12;
  rotationMatrix.at<double>(2,0) = m20;
  rotationMatrix.at<double>(2,1) = m21;
  rotationMatrix.at<double>(2,2) = m22;

  return rotationMatrix;
}

// Converts a given string to an integer
int StringToInt(const std::string& Text)
{
    std::istringstream ss(Text);
    int result;
    return ss >> result ? result : 0;
}

// Converts a given Mat to a string
std::string MatToString(const cv::Mat& mat)
{
    std::ostringstream ss;
    ss << mat;
    return ss.str();
}

// Converts a given float to a string
std::string FloatToString(float Number)
{
    std::ostringstream ss;
    ss << Number;
    return ss.str();
}

// Converts a given integer to a string
std::string IntToString(int Number)
{
    std::ostringstream ss;
    ss << Number;
    return ss.str();
}
