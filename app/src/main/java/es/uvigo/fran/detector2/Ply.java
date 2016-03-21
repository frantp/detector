package es.uvigo.fran.detector2;

import org.opencv.core.Point3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

class Ply {

    private static final NumberFormat FMT =
            new DecimalFormat("##.#####", new DecimalFormatSymbols(Locale.ENGLISH));

    public static class Data {
        public ArrayList<Point3> vertices;
        public ArrayList<int[]> triangles;

        public Data(ArrayList<Point3> vertices, ArrayList<int[]> triangles) {
            this.vertices = vertices;
            this.triangles = triangles;
        }

        public Data() {
            this(new ArrayList<Point3>(), new ArrayList<int[]>());
        }
    }

    public static Data read(String path) throws IOException {
        boolean endHeader = false;
        boolean endVertices = false;
        int numVertices = 0;
        int numFaces = 0;
        int count = 0;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            Data data = null;
            String line;
            while ((line = br.readLine()) != null) {
                String[] chunks = line.split(" ");
                if (!endHeader) { // Header
                    if (chunks[0].equals("element")) {
                        if (chunks[1].equals("vertex")) {
                            numVertices = Integer.parseInt(chunks[2]);
                        } else if (chunks[1].equals("face")) {
                            numFaces = Integer.parseInt(chunks[2]);
                        }
                    } else if (chunks[0].equals("end_header")) {
                        data = new Data(
                                new ArrayList<Point3>(numVertices),
                                new ArrayList<int[]>(numFaces)
                        );
                        endHeader = true;
                    }
                } else if (!endVertices) { // Vertices
                    data.vertices.add(new Point3(
                            Float.parseFloat(chunks[0]),
                            Float.parseFloat(chunks[1]),
                            Float.parseFloat(chunks[2])
                    ));

                    count++;
                    if (count == numVertices) {
                        count = 0;
                        endVertices = true;
                    }
                } else { // Faces
                    int faceVertices = Integer.parseInt(chunks[0]);
                    for (int i = 2; i < faceVertices; i++) {
                        data.triangles.add(new int[]{
                                Integer.parseInt(chunks[1]),
                                Integer.parseInt(chunks[i]),
                                Integer.parseInt(chunks[i + 1])
                        });
                    }
                    count++;
                    if (count == numFaces) {
                        break;
                    }
                }
            }
            return data;
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static void write(String path, Data data) throws IOException {
        new File(path).getParentFile().mkdirs();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path));
            bw.write("ply");
            bw.newLine();
            bw.write("format ascii 1.0");
            bw.newLine();
            bw.write("element vertex " + data.vertices.size());
            bw.newLine();
            bw.write("property float x");
            bw.newLine();
            bw.write("property float y");
            bw.newLine();
            bw.write("property float z");
            bw.newLine();
            bw.write("element face " + data.triangles.size());
            bw.newLine();
            bw.write("property list uchar uint vertex_index");
            bw.newLine();
            bw.write("end_header");
            bw.newLine();
            for (Point3 vertex : data.vertices) {
                bw.write(FMT.format(vertex.x) + " " +
                        FMT.format(vertex.y) + " " +
                        FMT.format(vertex.z));
                bw.newLine();
            }
            for (int[] triangle : data.triangles) {
                bw.write("3 " + triangle[0] + " " + triangle[1] + " " + triangle[2]);
                bw.newLine();
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }
}
