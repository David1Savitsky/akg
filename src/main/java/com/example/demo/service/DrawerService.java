package com.example.demo.service;

import static java.lang.Math.*;

import com.example.demo.model.Bgra32Bitmap;
import com.example.demo.model.Color;
import com.example.demo.model.Triangle;
import com.example.demo.model.Vector4D;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class DrawerService {

  private static boolean isInvisibleFace(List<Vector4D> vertexes) {
    var a = vertexes.get(0);
    var b = vertexes.get(1);
    var c = vertexes.get(2);

    var ab = b.subtract(a);
    var ac = c.subtract(a);

    var perpDotProduct = ab.getX() * ac.getY() - ab.getY() * ac.getX();
    return perpDotProduct > 0;
  }

  private static void drawTriangle(
      List<Vector4D> vertexes,
      List<Vector3D> normals,
      Bgra32Bitmap bitmap,
      double[][] zBuffer,
      Vector3D lightDirection,
      Vector3D viewDirection) {
    if (isInvisibleFace(vertexes)) {
      return;
    }
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

    var upY = (int) up.getY();
    var midY = (int) mid.getY();
    var downY = (int) down.getY();

    var firstSegmentHeight = midY - downY;
    var secondSegmentHeight = upY - midY;

    var totalHeight = upY - downY;
    for (var i = 0; i < totalHeight; i++) {
      var y = i + downY;
      if (y >= bitmap.getPixelHeight()) {
        break;
      }
      var secondHalf = i > firstSegmentHeight || midY == downY;
      var segmentHeight = secondHalf ? secondSegmentHeight : firstSegmentHeight;

      var alpha = i / (double) totalHeight;
      var a = up.subtract(down).multiply(Vector4D.createFromValue(alpha)).add(down);
      var beta = (i - (secondHalf ? firstSegmentHeight : 0)) / (double) segmentHeight;
      var b = resolveB(secondHalf, up, mid, down, beta);

      if (a.getX() > b.getX()) {
        fictive = a;
        a = b;
        b = fictive;
      }

      drawLine(zBuffer, bitmap, b, a, vertexes, normals, lightDirection, viewDirection, y);
    }
  }

  private static void drawLine(
      double[][] zBuffer,
      Bgra32Bitmap bitmap,
      Vector4D b,
      Vector4D a,
      List<Vector4D> vertexes,
      List<Vector3D> normals,
      Vector3D lightDirection,
      Vector3D viewDirection,
      int y) {
    var deltaX = b.getX() - a.getX() + 1;

    for (var x = (int) a.getX(); x <= (int) b.getX(); x++) {
      if (x >= bitmap.getPixelWidth()) {
        break;
      }
      var p = (x - a.getX()) / deltaX;

      var z = a.getZ() + p * (b.getZ() - a.getZ());
      try {
        if (zBuffer[x][y] > z) {
          zBuffer[x][y] = z;
          Color color =
              getPointColor(
                  new Vector3D(x, y, z), vertexes, normals, lightDirection, viewDirection);
          bitmap.setPixel(x, y, color);
        }
      } catch (Throwable e) {

      }
      ;
    }
  }

  private static final Vector3D AMB = new Vector3D(60, 10, 10);
  private static final Vector3D DIF = new Vector3D(250, 100, 30);
  private static final Vector3D SPEC =
      new Vector3D(255.0 / 50000.0, 255.0 / 50000.0, 255.0 / 50000.0);

  private static Color getPointColor(
      Vector3D point,
      List<Vector4D> vertexes,
      List<Vector3D> normals,
      Vector3D lightDirection,
      Vector3D viewDirection) {

    final float AmbientWeight = 0.5f;
    final float DiffuseWeight = 10f;
    final float SpecularWeight = 4f;

    var a = new Vector3D(vertexes.get(0).getX(), vertexes.get(0).getY(), vertexes.get(0).getZ());
    var b = new Vector3D(vertexes.get(1).getX(), vertexes.get(1).getY(), vertexes.get(1).getZ());
    var c = new Vector3D(vertexes.get(2).getX(), vertexes.get(2).getY(), vertexes.get(2).getZ());

    var area = Vector3D.crossProduct(b.subtract(a), c.subtract(a)).getNorm();

    var u = Vector3D.crossProduct(c.subtract(b), point.subtract(b)).getNorm() / area;
    var v = Vector3D.crossProduct(a.subtract(c), point.subtract(c)).getNorm() / area;
    var w = Vector3D.crossProduct(b.subtract(a), point.subtract(a)).getNorm() / area;

    var interpolatedNormal =
        normals
            .get(0)
            .scalarMultiply(u)
            .add(normals.get(1).scalarMultiply(v))
            .add(normals.get(2).scalarMultiply(w))
            .normalize();
    var lightLength = lightDirection.getNorm();

    var diffuse =
        max(
            0,
            Vector3D.dotProduct(interpolatedNormal, lightDirection) / (lightLength * lightLength));
    var specular =
        max(
            0,
            Vector3D.dotProduct(
                viewDirection,
                lightDirection.subtract(
                    interpolatedNormal.scalarMultiply(
                        2 * lightDirection.dotProduct(interpolatedNormal)))));

    var color =
        AMB.scalarMultiply(AmbientWeight)
            .add(DIF.scalarMultiply(diffuse).scalarMultiply(DiffuseWeight))
            .add(SPEC.scalarMultiply(pow(specular, SpecularWeight)));

    return new Color(
        (byte) max(0.0, min(color.getX(), 255.0)),
        (byte) max(0.0, min(color.getY(), 255.0)),
        (byte) max(0.0, min(color.getZ(), 255.0)));
  }

  public static Bgra32Bitmap drawModel(
          Vector4D[] vertexes,
          Vector3D[] normals,
          List<Triangle> triangles,
          int width,
          int height,
          double[][] zBuffer,
          Vector3D lightDirection,
          Vector3D viewDirection) {
    for (var i = 0; i < width; ++i) {
      for (var j = 0; j < height; ++j) {
        zBuffer[i][j] = Float.MAX_VALUE;
      }
    }

    Bgra32Bitmap bitmap = new Bgra32Bitmap(width, height);

    for (var triangle : triangles) {
      var indexes = triangle.getIndexes();
      var triangleVertexes =
              Arrays.stream(indexes).map(index -> vertexes[index.getVertex()]).toList();
      var normalVertexes = Arrays.stream(indexes).map(index -> normals[index.getNormal()]).toList();
      drawTriangle(
              triangleVertexes, normalVertexes, bitmap, zBuffer, lightDirection, viewDirection);
    }

    return bitmap;
  }

  private static Vector4D resolveB(
      boolean secondHalf, Vector4D up, Vector4D mid, Vector4D down, double beta) {
    if (secondHalf) {
      return up.subtract(mid).multiply(Vector4D.createFromValue(beta)).add(mid);
    } else {
      return mid.subtract(down).multiply(Vector4D.createFromValue(beta)).add(down);
    }
  }
}
