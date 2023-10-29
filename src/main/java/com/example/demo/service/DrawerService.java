package com.example.demo.service;

import com.example.demo.model.Bgra32Bitmap;
import com.example.demo.model.Triangle;
import com.example.demo.model.Vector4D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.List;

public class DrawerService {

    public static Bgra32Bitmap drawModel(Vector4D[] vertexes, Vector3D[] normals, List<Triangle> triangles, int width,
                                         int height, float[][] zBuffer, Vector3D lightDirection) {
        for (var i = 0; i < width; ++i) {
            for (var j = 0; j < height; ++j) {
                zBuffer[i][j] = Float.MAX_VALUE;
            }
        }

        Bgra32Bitmap bitmap = new Bgra32Bitmap(width, height);

        for (var j = 0; j < triangles.size(); j++) {
            var indexes = triangles.get(j).getIndexes();
            var triangleVertexes = Arrays.stream(indexes).map(index -> vertexes[index.getVertex()]).toList();
            var normalVertexes = Arrays.stream(indexes).map(index -> normals[index.getNormal()]).toList();
            drawTriangle(triangleVertexes, normalVertexes, bitmap, zBuffer, lightDirection);
        }

        // TODO rewrite bitmap
        return bitmap;
    }

    private static boolean isBackFace(List<Vector4D> vertexes) {
        var a = vertexes.get(0);
        var b = vertexes.get(1);
        var c = vertexes.get(2);

        var ab = b.subtract(a);
        var ac = c.subtract(a);

        var perpDotProduct = ab.getX() * ac.getY() - ab.getY() * ac.getX();
        return perpDotProduct > 0;
    }

    private static void drawLine(double x0, double y0, double x1, double y1, byte r, byte g, byte b, Bgra32Bitmap bitmap) {
        // TODO Do I really need it
        var x0i = round(x0);
        var y0i = round(y0);
        var x1i = round(x1);
        var y1i = round(y1);
        var steps = Math.max(Math.abs(x1i - x0i), Math.abs(y1i - y0i));
        if (steps <= 0) {
            return;
        }

        var dx = (x1i - x0i) / steps;
        var dy = (y1i - y0i) / steps;
        float x = x0i;
        float y = y0i;
        for (var i = 0; i < steps; ++i) {
            var xi = round(x);
            var yi = round(y);
            bitmap.setPixel(xi, yi, r, g, b);
            x += dx;
            y += dy;
        }
    }

    private static void drawTriangle(List<Vector4D> vertexes, List<Vector3D> normals, Bgra32Bitmap bitmap,
                                     double[][] zBuffer, Vector3D lightDirection) {
        if (isBackFace(vertexes)) {
            return;
        }
        var intensity = normals.stream()
                .map(v -> v.dotProduct(lightDirection.negate()))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        intensity = Math.max(intensity, 0);
        var red = convertToColor(intensity);
        var green = convertToColor(intensity);
        var blue = convertToColor(intensity);

        var up = vertexes.get(2);
        var mid = vertexes.get(1);
        var down = vertexes.get(0);

        Vector4D fictive;
        if (down.getY() > mid.getY()) {
            fictive = down;
            down = mid;
            mid = fictive;
        }
        if (down.getY() > up.getY()) {
            fictive = down;
            down = up;
            up = fictive;
        }
        if (mid.getY() > up.getY()) {
            fictive = up;
            up = mid;
            mid = fictive;
        }
        if (down.getY() < 0) {
            return;
        }

        var upY = (int)up.getY();
        var midY = (int)mid.getY();
        var downY = (int)down.getY();

        var firstSegmentHeight = midY - downY;
        var secondSegmentHeight = upY - midY;

        var totalHeight = upY - downY;
        for (var i = 0; i < totalHeight; i++) {
            var y = i + downY;
            if (y >= bitmap.getPixelHeight()) {
                break;;
            }

            var secondHalf = i > firstSegmentHeight || midY == downY;
            var segmentHeight = secondHalf ? secondSegmentHeight : firstSegmentHeight;

            var alpha = i / totalHeight;
            var a = up.subtract(down)
                    .multiply(Vector4D.createFromValue(alpha))
                    .add(down);

            var beta = (i - (secondHalf ? firstSegmentHeight : 0)) / segmentHeight;
            var b = resolveB(secondHalf, up, mid, down, beta);

            if (a.getX() > b.getX()) {
                fictive = a;
                a = b;
                b = fictive;
            }

            var deltaX = b.getX() - a.getX() + 1;

            for (var x = (int)a.getX(); x <= (int)b.getX(); x++) {
                if (x >= bitmap.getPixelWidth()) {
                    break;
                }
                var p = (x - a.getX()) / deltaX;

                var z = a.getZ() + p * (b.getZ() - a.getZ());
                if (zBuffer[x][y] > z) {
                    zBuffer[x][y] = z;
                    bitmap.setPixel(x, y, red, green, blue);
                }
            }
        }
    }

    private static Vector4D resolveB(boolean secondHalf, Vector4D up, Vector4D mid, Vector4D down, double beta) {
        if (secondHalf) {
            return up.subtract(mid)
                    .multiply(Vector4D.createFromValue(beta))
                    .add(mid);
        } else {
            return mid.subtract(down)
                    .multiply(Vector4D.createFromValue(beta))
                    .add(down);
        }
    }

    private static byte convertToColor(double intensity) {
        var c = 200 * intensity;
        return (byte) c;
    }

    private static int round(double numb) { // TODO can be 0
        return (int) Math.round(numb);
    }
}
