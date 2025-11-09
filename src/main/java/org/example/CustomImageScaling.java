package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CustomImageScaling {
    /**
     * 双线性插值
     * @param src 源图像
     * @param dstWidth 目标宽度
     * @param dstHeight 目标高度
     * @return 缩放后的图像
     */
    public static BufferedImage bilinearInterpolation(BufferedImage src, int dstWidth, int dstHeight) {
        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, src.getType());
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        // 计算缩放比例
        double scaleX = (double) srcWidth / dstWidth;
        double scaleY = (double) srcHeight / dstHeight;
        for (int y = 0; y < dstHeight; y++) {
            for (int x = 0; x < dstWidth; x++) {
                // 计算在源图像中的对应位置
                double srcX = x * scaleX;
                double srcY = y * scaleY;
                // 获取四个最近的像素点
                int x1 = (int) Math.floor(srcX);
                int y1 = (int) Math.floor(srcY);
                int x2 = Math.min(x1 + 1, srcWidth - 1);
                int y2 = Math.min(y1 + 1, srcHeight - 1);
                // 计算权重
                double wx = srcX - x1;
                double wy = srcY - y1;
                double w1 = (1 - wx) * (1 - wy);
                double w2 = wx * (1 - wy);
                double w3 = (1 - wx) * wy;
                double w4 = wx * wy;
                // 获取四个像素的RGB值
                int rgb1 = src.getRGB(x1, y1);
                int rgb2 = src.getRGB(x2, y1);
                int rgb3 = src.getRGB(x1, y2);
                int rgb4 = src.getRGB(x2, y2);
                // 分别对R、G、B通道进行插值
                int r = (int) (
                        ((rgb1 >> 16) & 0xFF) * w1 +
                                ((rgb2 >> 16) & 0xFF) * w2 +
                                ((rgb3 >> 16) & 0xFF) * w3 +
                                ((rgb4 >> 16) & 0xFF) * w4
                );
                int g = (int) (
                        ((rgb1 >> 8) & 0xFF) * w1 +
                                ((rgb2 >> 8) & 0xFF) * w2 +
                                ((rgb3 >> 8) & 0xFF) * w3 +
                                ((rgb4 >> 8) & 0xFF) * w4
                );
                int b = (int) (
                        (rgb1 & 0xFF) * w1 +
                                (rgb2 & 0xFF) * w2 +
                                (rgb3 & 0xFF) * w3 +
                                (rgb4 & 0xFF) * w4
                );
                // 确保值在0-255范围内
                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);
                // 设置目标像素
                int rgb = (r << 16) | (g << 8) | b;
                dst.setRGB(x, y, rgb);
            }
        }
        return dst;
    }
    /**
     * 双三次卷积插值核函数（Bicubic kernel）
     * @param x 距离
     * @return 权重
     */
    private static double bicubicKernel(double x) {
        double a = -0.5; // 常用参数
        x = Math.abs(x);
        if (x <= 1) {
            return (a + 2) * x * x * x - (a + 3) * x * x + 1;
        } else if (x < 2) {
            return a * x * x * x - 5 * a * x * x + 8 * a * x - 4 * a;
        } else {
            return 0;
        }
    }
    /**
     * 三次卷积插值
     * @param src 源图像
     * @param dstWidth 目标宽度
     * @param dstHeight 目标高度
     * @return 缩放后的图像
     */
    public static BufferedImage bicubicInterpolation(BufferedImage src, int dstWidth, int dstHeight) {
        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, src.getType());
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        // 计算缩放比例
        double scaleX = (double) srcWidth / dstWidth;
        double scaleY = (double) srcHeight / dstHeight;
        for (int y = 0; y < dstHeight; y++) {
            for (int x = 0; x < dstWidth; x++) {
                // 计算在源图像中的对应位置
                double srcX = x * scaleX;
                double srcY = y * scaleY;
                // 获取16个最近的像素点
                int baseX = (int) Math.floor(srcX);
                int baseY = (int) Math.floor(srcY);
                double r = 0, g = 0, b = 0;
                double totalWeight = 0;
                // 遍历4x4的邻域
                for (int j = -1; j <= 2; j++) {
                    for (int i = -1; i <= 2; i++) {
                        int sampleX = baseX + i;
                        int sampleY = baseY + j;
                        // 边界处理
                        if (sampleX < 0) sampleX = 0;
                        if (sampleX >= srcWidth) sampleX = srcWidth - 1;
                        if (sampleY < 0) sampleY = 0;
                        if (sampleY >= srcHeight) sampleY = srcHeight - 1;
                        // 计算权重
                        double dx = Math.abs(srcX - sampleX);
                        double dy = Math.abs(srcY - sampleY);
                        double weightX = bicubicKernel(dx);
                        double weightY = bicubicKernel(dy);
                        double weight = weightX * weightY;
                        // 获取像素值
                        int rgb = src.getRGB(sampleX, sampleY);
                        int pixelR = (rgb >> 16) & 0xFF;
                        int pixelG = (rgb >> 8) & 0xFF;
                        int pixelB = rgb & 0xFF;

                        // 累加加权值
                        r += pixelR * weight;
                        g += pixelG * weight;
                        b += pixelB * weight;
                        totalWeight += weight;
                    }
                }
                // 归一化
                if (totalWeight > 0) {
                    r /= totalWeight;
                    g /= totalWeight;
                    b /= totalWeight;
                }
                // 确保值在0-255范围内
                int finalR = Math.min(Math.max((int) Math.round(r), 0), 255);
                int finalG = Math.min(Math.max((int) Math.round(g), 0), 255);
                int finalB = Math.min(Math.max((int) Math.round(b), 0), 255);
                // 设置目标像素
                int rgb = (finalR << 16) | (finalG << 8) | finalB;
                dst.setRGB(x, y, rgb);
            }
        }

        return dst;
    }
    /**
     * 最近邻插值（作为对比）
     * @param src 源图像
     * @param dstWidth 目标宽度
     * @param dstHeight 目标高度
     * @return 缩放后的图像
     */
    public static BufferedImage nearestNeighbor(BufferedImage src, int dstWidth, int dstHeight) {
        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, src.getType());
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double scaleX = (double) srcWidth / dstWidth;
        double scaleY = (double) srcHeight / dstHeight;
        for (int y = 0; y < dstHeight; y++) {
            for (int x = 0; x < dstWidth; x++) {
                int srcX = (int) (x * scaleX);
                int srcY = (int) (y * scaleY);
                // 边界检查
                srcX = Math.min(srcX, srcWidth - 1);
                srcY = Math.min(srcY, srcHeight - 1);

                int rgb = src.getRGB(srcX, srcY);
                dst.setRGB(x, y, rgb);
            }
        }
        return dst;
    }
    /**
     * 缩放图像
     * @param inputPath 输入图像路径
     * @param outputPath 输出图像路径
     * @param scaleFactor 缩放因子
     * @param method 插值方法
     */
    public static void scaleImage(String inputPath, String outputPath, double scaleFactor, String method) {
        try {
            // 读取图像
            BufferedImage srcImage = ImageIO.read(new File(inputPath));
            if (srcImage == null) {
                System.err.println("无法读取图像: " + inputPath);
                return;
            }
            // 计算目标尺寸
            int dstWidth = (int) (srcImage.getWidth() * scaleFactor);
            int dstHeight = (int) (srcImage.getHeight() * scaleFactor);
            // 确保最小尺寸为1
            dstWidth = Math.max(dstWidth, 1);
            dstHeight = Math.max(dstHeight, 1);
            BufferedImage dstImage;
            // 选择插值方法
            long startTime = System.currentTimeMillis();
            switch (method.toUpperCase()) {
                case "BILINEAR":
                    dstImage = bilinearInterpolation(srcImage, dstWidth, dstHeight);
                    break;
                case "BICUBIC":
                    dstImage = bicubicInterpolation(srcImage, dstWidth, dstHeight);
                    break;
                case "NEAREST":
                    dstImage = nearestNeighbor(srcImage, dstWidth, dstHeight);
                    break;
                default:
                    System.err.println("未知的插值方法: " + method);
                    return;
            }
            long endTime = System.currentTimeMillis();
            // 保存图像
            String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
            ImageIO.write(dstImage, format, new File(outputPath));
            System.out.printf("图像缩放成功: %s (%.2fx, %s, %dx%d -> %dx%d, 耗时: %dms)%n",
                    outputPath, scaleFactor, method,
                    srcImage.getWidth(), srcImage.getHeight(),
                    dstWidth, dstHeight, endTime - startTime);
        } catch (IOException e) {
            System.err.println("处理图像时出错: " + e.getMessage());
        }
    }
    /**
     * 批量处理图像缩放
     */
    public static void batchScaleImages(String inputFolder, String outputFolder, double[] scaleFactors, String[] methods) {
        // 获取输入文件夹中的所有图像文件
        File inputDir = new File(inputFolder);
        File[] imageFiles = inputDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") ||
                    lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") ||
                    lowerName.endsWith(".bmp");
        });
        if (imageFiles == null || imageFiles.length == 0) {
            System.err.println("输入文件夹中没有找到图像文件: " + inputFolder);
            return;
        }
        // 对每个图像文件应用不同的缩放因子和方法
        for (File imageFile : imageFiles) {
            String fileName = imageFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            for (double scaleFactor : scaleFactors) {
                for (String method : methods) {
                    String outputFileName = String.format("%s_scale%.2f_%s%s",
                            baseName, scaleFactor, method.toLowerCase(), extension);
                    String outputPath = outputFolder + File.separator + outputFileName;

                    scaleImage(imageFile.getAbsolutePath(), outputPath, scaleFactor, method);
                }
            }
        }
    }
    static void main(String[] args) {
        // 创建必要的文件夹
        System.out.println("自定义图像插值缩放程序启动");
        // 定义缩放因子和插值方法
        double[] scaleFactors = {0.25, 0.5, 1.5, 3.0};
        String[] methods = {"BILINEAR", "BICUBIC", "NEAREST"};
        // 批量处理
        batchScaleImages("input", "output", scaleFactors, methods);
        System.out.println("\n程序执行完成！");
        System.out.println("请检查 output 文件夹中的结果图像");
    }
}