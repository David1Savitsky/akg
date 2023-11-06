package com.example.demo.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public record Result(List<Vector3D> vertexes, List<Vector3D> normals, List<Triangle> triangles){}
