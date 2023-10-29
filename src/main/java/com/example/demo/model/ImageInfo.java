package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

@Getter
@Setter
public class ImageInfo {

    private Vector3D cameraPosition;
    private Vector3D cameraTarget;
    private Vector3D camUp;

    private float positionX;
    private float positionY;
    private float positionZ;
    private float RotationX;
    private float RotationY;

    private List<Vector3D> Vertexes; // TODO really needed???
}
