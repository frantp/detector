/*
 * Mesh.h
 *
 *  Created on: Apr 9, 2014
 *      Author: edgar
 */

#ifndef MESH_H_
#define MESH_H_

#include <iostream>
#include <opencv2/core/core.hpp>


// --------------------------------------------------- //
//                 TRIANGLE CLASS                      //
// --------------------------------------------------- //

class Triangle {
public:

  explicit Triangle(int id, cv::Point3f V0, cv::Point3f V1, cv::Point3f V2);
  virtual ~Triangle();

  cv::Point3f getV0() const { return v0_; }
  cv::Point3f getV1() const { return v1_; }
  cv::Point3f getV2() const { return v2_; }

private:
  /** The identifier number of the triangle */
  int id_;
  /** The three vertices that defines the triangle */
  cv::Point3f v0_, v1_, v2_;
};


// --------------------------------------------------- //
//                     RAY CLASS                       //
// --------------------------------------------------- //

class Ray {
public:

  explicit Ray(cv::Point3f P0, cv::Point3f P1);
  virtual ~Ray();

  cv::Point3f getP0() { return p0_; }
  cv::Point3f getP1() { return p1_; }

private:
  /** The two points that defines the ray */
  cv::Point3f p0_, p1_;
};


// --------------------------------------------------- //
//                OBJECT MESH CLASS                    //
// --------------------------------------------------- //

class Mesh
{
public:

  Mesh();
  virtual ~Mesh();

  std::vector<cv::Point3f> getVertices() const { return vertices_; }
  std::vector<std::vector<int>> getTriangles() const { return triangles_; }
  std::vector<cv::Point3f> getKeyVertices() const { return key_vertices_; }
  cv::Point3f getVertex(int pos) const { return vertices_[pos]; }

  void load(const std::string path_file);

private:
  /* The list of triangles of the mesh */
  std::vector<cv::Point3f> vertices_;
  /* The list of triangles of the mesh */
  std::vector<std::vector<int>> triangles_;

  std::vector<cv::Point3f> key_vertices_;
};

#endif /* OBJECTMESH_H_ */
