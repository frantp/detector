/*
 * Mesh.cpp
 *
 *  Created on: Apr 9, 2014
 *      Author: edgar
 */

#include "Mesh.h"
#include "CsvReader.h"


// --------------------------------------------------- //
//                   TRIANGLE CLASS                    //
// --------------------------------------------------- //

/**  The custom constructor of the Triangle Class */
Triangle::Triangle(int id, cv::Point3f V0, cv::Point3f V1, cv::Point3f V2)
{
  id_ = id; v0_ = V0; v1_ = V1; v2_ = V2;
}

/**  The default destructor of the Class */
Triangle::~Triangle()
{
  // TODO Auto-generated destructor stub
}


// --------------------------------------------------- //
//                     RAY CLASS                       //
// --------------------------------------------------- //

/**  The custom constructor of the Ray Class */
Ray::Ray(cv::Point3f P0, cv::Point3f P1) {
  p0_ = P0; p1_ = P1;
}

/**  The default destructor of the Class */
Ray::~Ray()
{
  // TODO Auto-generated destructor stub
}


// --------------------------------------------------- //
//                 OBJECT MESH CLASS                   //
// --------------------------------------------------- //

/** The default constructor of the ObjectMesh Class */
Mesh::Mesh() : vertices_(0) , triangles_(0), key_vertices_(0)
{
}

/** The default destructor of the ObjectMesh Class */
Mesh::~Mesh()
{
  // TODO Auto-generated destructor stub
}


/** Load a CSV with *.ply format **/
void Mesh::load(const std::string path)
{

  // Create the reader
  CsvReader csvReader(path);

  // Clear previous data
  vertices_.clear();
  triangles_.clear();
  key_vertices_.clear();

  // Read from .ply file
  csvReader.readPLY(vertices_, triangles_, key_vertices_);

}
