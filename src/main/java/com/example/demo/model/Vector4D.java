package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

@Getter
@AllArgsConstructor
public class Vector4D {

    private double x;
    private double y;
    private double z;
    private double w;

    public Vector4D add(Vector4D term) {
        return new Vector4D(
                this.x + term.x,
                this.y + term.y,
                this.z + term.z,
                this.w + term.w
        );
    }

    public Vector4D subtract(Vector4D subtrahend) {
        return new Vector4D(
                this.x - subtrahend.x,
                this.y - subtrahend.y,
                this.z - subtrahend.z,
                this.w - subtrahend.w
        );
    }

    public Vector4D multiply(Vector4D multiplier) {
        return new Vector4D(
                this.x * multiplier.x,
                this.y * multiplier.y,
                this.z * multiplier.z,
                this.w * multiplier.w
        );
    }

    public Vector4D divide(Vector4D divider) {
        return new Vector4D(
                this.x / divider.x,
                this.y / divider.y,
                this.z / divider.z,
                this.w / divider.w
        );
    }

    public static Vector4D createFromValue(double value) {
        return new Vector4D(value, value, value, value);
    }

    public static Vector4D transform(Vector3D position, Matrix4x4 matrix4x4) {
        var x = (position.getX() * matrix4x4.getM11())
                + (position.getY() * matrix4x4.getM21())
                + (position.getZ() * matrix4x4.getM31())
                + matrix4x4.getM41();
        var y = (position.getX() * matrix4x4.getM12())
                + (position.getY() * matrix4x4.getM22())
                + (position.getZ() * matrix4x4.getM32())
                + matrix4x4.getM42();
        var z = (position.getX() * matrix4x4.getM13())
                + (position.getY() * matrix4x4.getM23())
                + (position.getZ() * matrix4x4.getM33())
                + matrix4x4.getM43();
        var w = (position.getX() * matrix4x4.getM14())
                + (position.getY() * matrix4x4.getM24())
                + (position.getZ() * matrix4x4.getM34())
                + matrix4x4.getM44();
        return new Vector4D(x, y, z, w);
    }

    public static Vector4D transform(Vector4D position, Matrix4x4 matrix4x4) {
        var x = (position.getX() * matrix4x4.getM11())
                + (position.getY() * matrix4x4.getM21())
                + (position.getZ() * matrix4x4.getM31())
                + (position.getW() * matrix4x4.getM41());
        var y = (position.getX() * matrix4x4.getM12())
                + (position.getY() * matrix4x4.getM22())
                + (position.getZ() * matrix4x4.getM32())
                + (position.getW() * matrix4x4.getM42());
        var z = (position.getX() * matrix4x4.getM13())
                + (position.getY() * matrix4x4.getM23())
                + (position.getZ() * matrix4x4.getM33())
                + (position.getW() * matrix4x4.getM43());
        var w = (position.getX() * matrix4x4.getM14())
                + (position.getY() * matrix4x4.getM24())
                + (position.getZ() * matrix4x4.getM34())
                + (position.getW() * matrix4x4.getM44());
        return new Vector4D(x, y, z, w);
    }
}
