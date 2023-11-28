package com.example.demo.service;

import com.example.demo.model.ImageInfo;
import com.example.demo.model.Matrix4x4;
import com.example.demo.model.Vector4D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class VertexService {
    private static final float FOV = (float) (Math.PI / 8.0d);
    private static final float NEAR_PLANE_DISTANCE = 0.1f;
    private static final float FAR_PLANE_DISTANCE = 100;

    public static Vector3D convertToOrthogonal(Vector3D v, Vector3D rotationPoint) {
        var r = v.getX();
        var phi = v.getY();
        var zenith = v.getZ();

        var vv = new Vector3D(r * Math.cos(zenith) * Math.sin(phi),
                r * Math.sin(zenith),
                -r * Math.cos(zenith) * Math.cos(phi));
        return vv.add(rotationPoint);
    }

    public static void convertVertexes(ImageInfo info, double gridWidth, double gridHeight, Vector4D[] res) {
        float xMin = 0;
        float yMin = 0;
        float minDepth = 0;
        float maxDepth = 1;

        var transformationMatrix = Matrix4x4.createTranslation(info.getPositionX(), info.getPositionY(), info.getPositionZ());
        var rotationMatrix = Matrix4x4.createRotationX(info.getRotationX())
                .multiply(Matrix4x4.createRotationY(info.getRotationY()));
        var scaleMatrix = Matrix4x4.createScale(0.4d, 0.4d, 0.4d);

        var orthogonal = convertToOrthogonal(info.getCameraPosition(), info.getCameraTarget());
        var viewMatrix = Matrix4x4.createView(orthogonal,
                info.getCameraTarget(), info.getCamUp());
        var projectionMatrix = Matrix4x4.createProjection(FOV, gridWidth / gridHeight, NEAR_PLANE_DISTANCE,
                FAR_PLANE_DISTANCE);
        var viewPortMatrix = Matrix4x4.createViewPort(xMin, yMin, gridWidth ,gridHeight, minDepth, maxDepth);

        var matrix = scaleMatrix.multiply(rotationMatrix)
                .multiply(transformationMatrix)
                .multiply(viewMatrix)
                .multiply(projectionMatrix);
        Vector4D result;
        for (int i = 0; i < info.getVertexes().size(); i++) {
            result = Vector4D.transform(info.getVertexes().get(i), matrix);
            result = result.divide(Vector4D.createFromValue(result.getW()));
            res[i] = Vector4D.transform(result, viewPortMatrix);
        }
    }

    public static void transformNormals(List<Vector3D> normals, ImageInfo info, Vector3D[] res) {
        var rotationMatrix = Matrix4x4.createRotationX(info.getRotationX())
                .multiply(Matrix4x4.createRotationY(info.getRotationY()));
        for (int i = 0; i < normals.size(); i++) {
            var vec = Vector4D.transform(normals.get(i), rotationMatrix);
            res[i] = new Vector3D(vec.getX(), vec.getY(), vec.getZ());
        }
    }
}
