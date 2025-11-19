package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
// 图像工具类
class ImageProcessor {
    // 读取图像为灰度矩阵
    public static double[][] loadImage(String filename) throws IOException {
        BufferedImage image = ImageIO.read(new File(filename));
        int width = image.getWidth();
        int height = image.getHeight();

        double[][] result = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                // 转换为灰度值 (0-1范围)
                result[y][x] = (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114) / 255.0;
            }
        }
        return result;
    }
    // 调整图像大小为2的幂次
    public static double[][] resizeToPowerOfTwo(double[][] input) {
        int newSize = 1;
        int maxDim = Math.max(input.length, input[0].length);
        while (newSize < maxDim) {
            newSize <<= 1;
        }
        double[][] result = new double[newSize][newSize];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                if (i < input.length && j < input[0].length) {
                    result[i][j] = input[i][j];
                } else {
                    result[i][j] = 0;
                }
            }
        }
        return result;
    }
    // 保存矩阵为图像
    public static void saveImage(double[][] matrix, String filename) throws IOException {
        int height = matrix.length;
        int width = matrix[0].length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 找到最小最大值用于归一化
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix[i][j] < min) min = matrix[i][j];
                if (matrix[i][j] > max) max = matrix[i][j];
            }
        }

        // 归一化并保存
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int value;
                if (max > min) {
                    value = (int) ((matrix[i][j] - min) / (max - min) * 255);
                } else {
                    value = 0;
                }
                value = Math.max(0, Math.min(255, value)); // 限制在0-255范围
                Color color = new Color(value, value, value);
                image.setRGB(j, i, color.getRGB());
            }
        }

        ImageIO.write(image, "jpg", new File(filename));
    }
}
