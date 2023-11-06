package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Matrix4x4 implements Cloneable {

    private double m11;
    private double m12;
    private double m13;
    private double m14;
    private double m21;
    private double m22;
    private double m23;
    private double m24;
    private double m31;
    private double m32;
    private double m33;
    private double m34;
    private double m41;
    private double m42;
    private double m43;
    private double m44;

    private static final Matrix4x4 BASE = new Matrix4x4(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    );

    @SneakyThrows
    public static Matrix4x4 createTranslation(double x, double y, double z) {
        var res = (Matrix4x4) BASE.clone();
        res.m41 = x;
        res.m42 = y;
        res. m43 = z;
        return res;
    }

    @SneakyThrows
    public static Matrix4x4 createRotationX(double radians) {
        var res = (Matrix4x4) BASE.clone();

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        res.m22 = cos;
        res.m23 = sin;
        res.m32 = -sin;
        res.m33 = cos;

        return res;
    }

    @SneakyThrows
    public static Matrix4x4 createRotationY(double radians) {
        var res = (Matrix4x4) BASE.clone();

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        res.m11 = cos;
        res.m13 = -sin;
        res.m31 = sin;
        res.m33 = cos;

        return res;
    }

    public Matrix4x4 multiply(Matrix4x4 m) {
        var res = new Matrix4x4();

        res.m11 = this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31 + this.m14 * m.m41;
        res.m12 = this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32 + this.m14 * m.m42;
        res.m13 = this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33 + this.m14 * m.m43;
        res.m14 = this.m11 * m.m14 + this.m12 * m.m24 + this.m13 * m.m34 + this.m14 * m.m44;

        res.m21 = this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31 + this.m24 * m.m41;
        res.m22 = this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32 + this.m24 * m.m42;
        res.m23 = this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33 + this.m24 * m.m43;
        res.m24 = this.m21 * m.m14 + this.m22 * m.m24 + this.m23 * m.m34 + this.m24 * m.m44;

        res.m31 = this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31 + this.m34 * m.m41;
        res.m32 = this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32 + this.m34 * m.m42;
        res.m33 = this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33 + this.m34 * m.m43;
        res.m34 = this.m31 * m.m14 + this.m32 * m.m24 + this.m33 * m.m34 + this.m34 * m.m44;

        res.m41 = this.m41 * m.m11 + this.m42 * m.m21 + this.m43 * m.m31 + this.m44 * m.m41;
        res.m42 = this.m41 * m.m12 + this.m42 * m.m22 + this.m43 * m.m32 + this.m44 * m.m42;
        res.m43 = this.m41 * m.m13 + this.m42 * m.m23 + this.m43 * m.m33 + this.m44 * m.m43;
        res.m44 = this.m41 * m.m14 + this.m42 * m.m24 + this.m43 * m.m34 + this.m44 * m.m44;

        return res;
    }

    @SneakyThrows
    public static Matrix4x4 createScale(double x, double y, double z) {
        var res = (Matrix4x4) BASE.clone();
        res.m11 = x;
        res.m22 = y;
        res.m33 = z;
        return res;
    }

    @SneakyThrows
    public static Matrix4x4 createView(Vector3D cameraPosition, Vector3D cameraTarget, Vector3D cameraUpVector) {
        var zaxis = cameraPosition.subtract(cameraTarget).normalize();
        var xaxis = cameraUpVector.crossProduct(zaxis).normalize();
        var yaxis = zaxis.crossProduct(xaxis);

        var res = (Matrix4x4) BASE.clone();

        res.m11 = xaxis.getX();
        res.m12 = yaxis.getX();
        res.m13 = zaxis.getX();

        res.m21 = xaxis.getY();
        res.m22 = yaxis.getY();
        res.m23 = zaxis.getY();

        res.m31 = xaxis.getZ();
        res.m32 = yaxis.getZ();
        res.m33 = zaxis.getZ();

        res.m41 = -xaxis.dotProduct(cameraPosition);
        res.m42 = -yaxis.dotProduct(cameraPosition);
        res.m43 = -zaxis.dotProduct(cameraPosition);

        return res;
    }

    public static Matrix4x4 createProjection(double fov, double aspectRatio, double nearPlaneDistance, double farPlaneDistance) {
        double yScale = 1.0f / Math.tan(fov * 0.5f);
        double xScale = yScale / aspectRatio;

        Matrix4x4 res = new Matrix4x4();

        res.m11 = xScale;
        res.m12 = res.m13 = res.m14 = 0.0f;

        res.m22 = yScale;
        res.m21 = res.m23 = res.m24 = 0.0f;

        res.m31 = res.m32 = 0.0f;
        double negFarRange = Float.POSITIVE_INFINITY == farPlaneDistance
                ? -1.0f
                : farPlaneDistance / (nearPlaneDistance - farPlaneDistance);
        res.m33 = negFarRange;
        res.m34 = -1.0f;

        res.m41 = res.m42 = res.m44 = 0.0f;
        res.m43 = nearPlaneDistance * negFarRange;

        return res;
    }

    @SneakyThrows
    public static Matrix4x4 createViewPort(double x, double y, double width, double height, double minDepth, double maxDepth) {
        var res = (Matrix4x4) BASE.clone();

        res.m11 = width / 2;
        res.m22 = - (height / 2);
        res.m33 = maxDepth - minDepth;

        res.m41 = x + width / 2;
        res.m42 = y + height / 2;
        res.m43 = minDepth;

        return res;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
