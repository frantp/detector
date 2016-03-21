#include "CsvReader.h"

/** The default constructor of the CSV reader Class */
CsvReader::CsvReader(const string &path, const char &separator){
    file_.open(path.c_str(), ifstream::in);
    separator_ = separator;
}

/* Read a plane text file with .ply format */
void CsvReader::readPLY(vector<Point3f> &list_vertex, vector<vector<int> > &list_triangles)
{
    std::string line, tmp_str, n;
    int num_vertices = 0, num_faces = 0;
    int count = 0;
    bool end_header = false;
    bool end_vertices = false;

    // Read the whole *.ply file
    while (getline(file_, line)) {
        stringstream liness(line);

        // read header
        if (!end_header)
        {
            getline(liness, tmp_str, separator_);
            if (tmp_str == "element")
            {
                getline(liness, tmp_str, separator_);
                getline(liness, n);
                if (tmp_str == "vertex") num_vertices = StringToInt(n);
                if (tmp_str == "face") num_faces = StringToInt(n);
            }
            if (tmp_str == "end_header") end_header = true;
        }

        // read vertex and add into 'list_vertex'
        else if (!end_vertices)
        {
            string x, y, z;
            getline(liness, x, separator_);
            getline(liness, y, separator_);
            getline(liness, z);

            cv::Point3f tmp_p;
            tmp_p.x = (float)StringToInt(x);
            tmp_p.y = (float)StringToInt(y);
            tmp_p.z = (float)StringToInt(z);
            list_vertex.push_back(tmp_p);

            count++;
            if (count == num_vertices)
            {
                count = 0;
                end_vertices = true;
            }
        }
        // read faces and add into 'list_triangles'
        else
        {
            string num_pts_face_str;
            getline(liness, num_pts_face_str, separator_);
            int num_pts_face = StringToInt(num_pts_face_str);
            getline(liness, tmp_str, separator_);
            int ptA = StringToInt(tmp_str);
            getline(liness, tmp_str, separator_);
            int ptB = StringToInt(tmp_str);
            for (int i = 2; i < num_pts_face; ++i)
            {
                getline(liness, tmp_str, separator_);
                int ptC = StringToInt(tmp_str);
                list_triangles.push_back({ptA, ptB, ptC});
                ptB = ptC;
            }

            count++;
            if (count == num_faces) {
                break;
            }
        }
    }
}
