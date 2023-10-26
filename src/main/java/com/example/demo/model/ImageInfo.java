package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImageInfo {

    private Vector3 cameraPosition;
    private Vector3 cameraTarget;
    private Vector3 camUp;

    private float positionX;
    private float positionY;
    private float positionZ;
    private float RotationX;
    private float RotationY;

    private List<Vector3> Vertexes; // TODO really needed???
}
