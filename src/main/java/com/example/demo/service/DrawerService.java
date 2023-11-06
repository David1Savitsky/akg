package com.example.demo.service;

import com.example.demo.model.Triangle;
import com.example.demo.model.Vector4D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.List;

import static com.example.demo.HelloApplication.HEIGHT;
import static com.example.demo.HelloApplication.WIDTH;

public class DrawerService {

    public static void drawModel(Vector4D[] vertexes, Vector3D[] normals, List<Triangle> triangles, int width,
                                         int height, double[][] zBuffer, Vector3D lightDirection, int[] pixels) {
        for (var i = 0; i < width; ++i) {
            for (var j = 0; j < height; ++j) {
                zBuffer[i][j] = Float.MAX_VALUE;
            }
        }

        for (var triangle: triangles) {
            var indexes = triangle.getIndexes();
            var triangleVertexes = Arrays.stream(indexes).map(index -> vertexes[index.getVertex()]).toList();
            var normalVertexes = Arrays.stream(indexes).map(index -> normals[index.getNormal()]).toList();
            renderTriangle(triangleVertexes, normalVertexes, zBuffer, lightDirection, pixels);
        }

//        triangles.stream()
//                .parallel()
//                .forEach(triangle -> {
//
//                });
    }

    private static boolean isInvisibleFace(List<Vector4D> vertexes) {
        var a = vertexes.get(0);
        var b = vertexes.get(1);
        var c = vertexes.get(2);

        var ab = b.subtract(a);
        var ac = c.subtract(a);

        var perpDotProduct = ab.getX() * ac.getY() - ab.getY() * ac.getX();
        return perpDotProduct > 0;
    }

    private static void renderTriangle(List<Vector4D> vertexes, List<Vector3D> normals,
                                       double[][] zBuffer, Vector3D lightDirection, int[] pixels) {
        if (isInvisibleFace(vertexes)) {
            return;
        }
        var intensity = calcIntensity(normals, lightDirection);
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
            if (y >= HEIGHT) {
                break;
            }
            var secondHalf = i > firstSegmentHeight || midY == downY;
            var segmentHeight = secondHalf ? secondSegmentHeight : firstSegmentHeight;
            var alpha = i / (double)totalHeight;
            var a = up.subtract(down)
                    .multiply(Vector4D.createFromValue(alpha))
                    .add(down);
            var beta = (i - (secondHalf ? firstSegmentHeight : 0)) / (double)segmentHeight;
            var b = resolveB(secondHalf, up, mid, down, beta);

            if (a.getX() > b.getX()) {
                fictive = a;
                a = b;
                b = fictive;
            }

            renderLine(zBuffer, pixels, b, a, y, red, green, blue);
        }
    }

    private static double calcIntensity(List<Vector3D> normals, Vector3D lightDirection) {
        var intensity = normals.stream()
                .map(v -> v.dotProduct(lightDirection.negate()))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        intensity = Math.max(intensity, 0);
        return intensity;
    }

    private static void renderLine(double[][] zBuffer, int[] pixels, Vector4D b, Vector4D a, int y, short red, short green, short blue) {
        var deltaX = b.getX() - a.getX() + 1;

        for (var x = (int) a.getX(); x <= (int) b.getX(); x++) {
            if (x >= WIDTH) {
                break;
            }
            var p = (x - a.getX()) / deltaX;

            var z = a.getZ() + p * (b.getZ() - a.getZ());
            try {
                if (zBuffer[x][y] > z) {
                    zBuffer[x][y] = z;
                    renderPoint(pixels, x, y, red, green, blue);
                }
            } catch (Throwable e){

            };

        }
    }

    private static void renderPoint(int[] arr, int x, int y, short r, short g, short b) {
        var pixelIndex = (x % WIDTH) + (y * WIDTH);

        if (pixelIndex < 0 || pixelIndex >= arr.length) {
            return;
        }

        arr[(x % WIDTH) + (y * WIDTH)] = argbInt(r, g, b);
    }

    private static int argbInt(short r, short g, short b) {
        return  (255 << 24) | (r << 16) | (g << 8) | b;
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

    private static short convertToColor(double intensity) {
        var c = 200 * intensity;
        return (short) ((short)c & 0xff);
    }
}
