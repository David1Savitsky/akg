package com.example.demo.model;

import java.util.List;

public record ObjFileParseResult(List<Vector3> vertexes, List<Vector3> normals, List<Triangle> triangles){}
