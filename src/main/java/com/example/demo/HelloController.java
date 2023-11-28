package com.example.demo;

import com.example.demo.model.ImageInfo;
import com.example.demo.model.Result;
import com.example.demo.model.Triangle;
import com.example.demo.model.Vector4D;
import com.example.demo.service.DrawerService;
import com.example.demo.service.ParserService;
import com.example.demo.service.VertexService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.SneakyThrows;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.net.URL;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static com.example.demo.HelloApplication.HEIGHT;
import static com.example.demo.HelloApplication.WIDTH;
import static java.lang.Math.PI;

public class HelloController implements Initializable {

  private static final float ROTATION_SPEED = 0.1f;
  private static final float MOVE_SPEED = 5;

  @FXML private Canvas canvas;
  private Set<KeyCode> keysPressed = new HashSet<>();

  private ImageInfo positions =
      ImageInfo.builder()
          .positionZ(0)
          .cameraTarget(new Vector3D(0, 0, 0))
          .cameraPosition(new Vector3D(12, PI, 0))
          .camUp(new Vector3D(0, 1, 0))
          .build();
  private List<Vector3D> normals;
  private List<Triangle> triangles;
  private Vector4D[] transformedVertexes;
  private Vector3D[] transformedNormals;
  private final double[][] zBuffer = new double[WIDTH][HEIGHT];

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    readFromFile();
    draw();

    canvas.setOnKeyPressed(this::handleKeyPress);
    canvas.setOnKeyReleased(this::handleKeyRelease);

    canvas.setFocusTraversable(true);
  }

  @SneakyThrows
  private void readFromFile() {
    Result res =
        ParserService.parse(
            Files.readAllLines(Paths.get("src/main/resources/com/example/demo/azelow.obj")));
    positions.setVertexes(res.vertexes());
    normals = res.normals();
    triangles = res.triangles();
    transformedVertexes = new Vector4D[positions.getVertexes().size()];
    transformedNormals = new Vector3D[normals.size()];
  }

  private void draw() {
    var intBuffer = IntBuffer.allocate(WIDTH * HEIGHT);
    var pixelBuffer =
        new PixelBuffer<>(WIDTH, HEIGHT, intBuffer, PixelFormat.getIntArgbPreInstance());
    var image = new WritableImage(pixelBuffer);

    VertexService.convertVertexes(positions, WIDTH, HEIGHT, transformedVertexes);
    VertexService.transformNormals(normals, positions, transformedNormals);

    Vector3D viewDirection =
        positions
            .getCameraTarget()
            .subtract(
                VertexService.convertToOrthogonal(
                    positions.getCameraPosition(), positions.getCameraTarget()))
            .normalize();

    var bitmap = DrawerService.drawModel(
        transformedVertexes,
        transformedNormals,
        triangles,
        WIDTH,
        HEIGHT,
        zBuffer,
        new Vector3D(10, 0, 10),
        viewDirection);
    image = bitmap.getSource();
    pixelBuffer.updateBuffer(buffer -> null);
    canvas.getGraphicsContext2D().clearRect(0, 0, WIDTH, HEIGHT);
    canvas.getGraphicsContext2D().drawImage(image, 0.0, 0.0);
  }

  private void handleKeyPress(KeyEvent event) {
    keysPressed.add(event.getCode());

    if (keysPressed.contains(KeyCode.LEFT)) {
      positions.setRotationY(positions.getRotationY() - ROTATION_SPEED);
    }
    if (keysPressed.contains(KeyCode.RIGHT)) {
      positions.setRotationY(positions.getRotationY() + ROTATION_SPEED);
    }
    if (keysPressed.contains(KeyCode.UP)) {
      positions.setRotationX(positions.getRotationX() - ROTATION_SPEED);
    }
    if (keysPressed.contains(KeyCode.DOWN)) {
      positions.setRotationX(positions.getRotationX() + ROTATION_SPEED);
    }
    if (keysPressed.contains(KeyCode.W)) {
      positions.setPositionZ(positions.getPositionZ() + MOVE_SPEED);
      positions.setCameraTarget(
          new Vector3D(
              positions.getCameraTarget().getX(),
              positions.getCameraTarget().getY(),
              positions.getCameraTarget().getZ() + MOVE_SPEED));
    }
    if (keysPressed.contains(KeyCode.S)) {
      positions.setPositionZ(positions.getPositionZ() - MOVE_SPEED);
      positions.setCameraTarget(
          new Vector3D(
              positions.getCameraTarget().getX(),
              positions.getCameraTarget().getY(),
              positions.getCameraTarget().getZ() - MOVE_SPEED));
    }
    if (keysPressed.contains(KeyCode.A)) {
      positions.setPositionX(positions.getPositionX() + MOVE_SPEED);
      positions.setCameraTarget(
          new Vector3D(
              positions.getCameraTarget().getX() + MOVE_SPEED,
              positions.getCameraTarget().getY(),
              positions.getCameraTarget().getZ()));
    }
    if (keysPressed.contains(KeyCode.D)) {
      positions.setPositionX(positions.getPositionX() - MOVE_SPEED);
      positions.setCameraTarget(
          new Vector3D(
              positions.getCameraTarget().getX() - MOVE_SPEED,
              positions.getCameraTarget().getY(),
              positions.getCameraTarget().getZ()));
    }
    if (keysPressed.contains(KeyCode.N)) {
      positions.setCameraPosition(
          new Vector3D(
              positions.getCameraPosition().getX() + MOVE_SPEED,
              positions.getCameraPosition().getY(),
              positions.getCameraPosition().getZ()));
    }
    if (keysPressed.contains(KeyCode.M)) {
      positions.setCameraPosition(
          new Vector3D(
              positions.getCameraPosition().getX() - MOVE_SPEED,
              positions.getCameraPosition().getY(),
              positions.getCameraPosition().getZ()));
    }
    draw();
  }

  private void handleKeyRelease(KeyEvent event) {
    keysPressed.remove(event.getCode());
  }
}
