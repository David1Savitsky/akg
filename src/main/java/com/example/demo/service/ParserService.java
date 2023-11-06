package com.example.demo.service;

import com.example.demo.model.Result;
import com.example.demo.model.Triangle;
import com.example.demo.model.TriangleIndexes;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserService {

    public static Result parse(List<String> content) {
        var vertexes = new ArrayList<Vector3D>();
        var normals = new ArrayList<Vector3D>();
        var triangles = new ArrayList<Triangle>();

        for (var line : content) {
            var words = line.split(" +");
            if (words.length == 0) continue;
            switch (words[0]) {
                case "v" -> {
                    var x = Float.parseFloat(words[1]);
                    var y = Float.parseFloat(words[2]);
                    var z = Float.parseFloat(words[3]);
                    vertexes.add(new Vector3D(x, y, z));
                }
                case "vn" -> {
                    var x = Float.parseFloat(words[1]);
                    var y = Float.parseFloat(words[2]);
                    var z = Float.parseFloat(words[3]);
                    normals.add(new Vector3D(x, y, z));
                }
                case "f" -> {
                    var argsIndexes = Arrays.stream(words, 1, words.length).toList();

                    for (var i = 0; i < argsIndexes.size() - 2; ++i) {
                        var indexes = argsIndexes.get(0).split("/");
                        var triangleIndexes =  new TriangleIndexes[3];

                        triangleIndexes[0] = new TriangleIndexes(
                                Integer.parseInt(indexes[0]) - 1,
                                Integer.parseInt(indexes[2]) - 1
                        );

                        for (int j = 1; j < 3; ++j) {
                            indexes = argsIndexes.get((j + i) % argsIndexes.size()).split("/");
                            triangleIndexes[j] = new TriangleIndexes(
                                    Integer.parseInt(indexes[0]) - 1,
                                    Integer.parseInt(indexes[2]) - 1
                            );
                        }

                        triangles.add(new Triangle(triangleIndexes));
                    }
                }
            }
        }
        return new Result(vertexes, normals, triangles);
    }
}
