package com.example.demo.model;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Bgra32Bitmap {

  private final byte[] backBuffer;
  private final int backBufferStride;
  private final int bytesPerPixel;

  private final WritableImage source;
  private final int pixelWidth;
  private final int pixelHeight;

  public Bgra32Bitmap(int pixelWidth, int pixelHeight) {
    this.source = new WritableImage(pixelWidth, pixelHeight);
    this.backBuffer = new byte[pixelWidth * pixelHeight * 4];
    this.backBufferStride = pixelWidth * 4;
    this.bytesPerPixel = 4;
    this.pixelWidth = pixelWidth;
    this.pixelHeight = pixelHeight;
  }

  public WritableImage getSource() {
    return source;
  }

  public int getPixelWidth() {
    return pixelWidth;
  }

  public int getPixelHeight() {
    return pixelHeight;
  }

  public void setPixel(int x, int y, Color color) {
    if (x <= 0 || x >= pixelWidth || y <= 0 || y >= pixelHeight) return;

    int index = (y * backBufferStride) + (x * bytesPerPixel);
    backBuffer[index] = color.getBlue();
    backBuffer[index + 1] = color.getGreen();
    backBuffer[index + 2] = color.getRed();
    backBuffer[index + 3] = (byte) 255;

    PixelWriter pixelWriter = source.getPixelWriter();
    pixelWriter.setArgb(
        x,
        y,
        (255 << 24)
            | ((color.getRed() & 0xFF) << 16)
            | ((color.getGreen() & 0xFF) << 8)
            | (color.getBlue() & 0xFF));
  }
}
