package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;
public class ImageScaling {
    static {
        // 加载OpenCV本地库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // 图像缩放方法枚举
    public enum ScalingMethod {
        BILINEAR,
        BICUBIC
    }
    /**
     * 图像缩放函数
     * @param inputPath 输入图像路径
     * @param outputPath 输出图像路径
     * @param scaleFactor 缩放因子 (大于1放大，小于1缩小)
     * @param method 缩放方法
     */
    public static void scaleImage(String inputPath, String outputPath, double scaleFactor, ScalingMethod method) {
        // 读取输入图像
        Mat srcImage = Imgcodecs.imread(inputPath);
        if (srcImage.empty()) {
            System.err.println("无法读取图像: " + inputPath);
            return;
        }
        // 计算输出图像尺寸
        int interpolation = getInterpolationMethod(method);
        Size newSize = new Size(
                srcImage.cols() * scaleFactor,
                srcImage.rows() * scaleFactor
        );
        // 创建输出图像矩阵
        Mat dstImage = new Mat();
        // 执行图像缩放
        Imgproc.resize(srcImage, dstImage, newSize, 0, 0, interpolation);

        // 保存输出图像
        boolean success = Imgcodecs.imwrite(outputPath, dstImage);
        if (success) {
            System.out.println("图像缩放成功: " + outputPath +
                    " (缩放因子: " + scaleFactor +
                    ", 方法: " + method + ")");
        } else {
            System.err.println("保存图像失败: " + outputPath);
        }

        // 释放内存
        srcImage.release();
        dstImage.release();
    }
    /**
     * 根据缩放方法获取对应的OpenCV插值方法
     */
    private static int getInterpolationMethod(ScalingMethod method) {
        switch (method) {
            case BILINEAR:// 双线性插值
                return Imgproc.INTER_LINEAR;
            case BICUBIC://三次卷积插值
                return Imgproc.INTER_CUBIC;
            default:
                return Imgproc.INTER_LINEAR;
        }
    }

    /**
     * 批量处理图像缩放
     */
    public static void batchScaleImages(String inputFolder, String outputFolder,
                                        double[] scaleFactors, ScalingMethod[] methods) {

        // 创建输出文件夹
        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 获取输入文件夹中的所有图像文件
        File inputDir = new File(inputFolder);
        File[] imageFiles = inputDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") ||
                    lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") ||
                    lowerName.endsWith(".bmp");
        });
        // 对每个图像文件应用不同的缩放因子和方法
        for (File imageFile : imageFiles) {
            String fileName = imageFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));

            for (double scaleFactor : scaleFactors) {
                for (ScalingMethod method : methods) {
                    String outputFileName = String.format("%s_scale%.2f_%s%s",
                            baseName, scaleFactor, method.toString().toLowerCase(), extension);
                    String outputPath = outputFolder + File.separator + outputFileName;

                    scaleImage(imageFile.getAbsolutePath(), outputPath, scaleFactor, method);
                }
            }
        }
    }
    /**
     * 单个图像缩放示例
     */
    public static void singleImageScalingExample() {
        String inputImage = "image/ewm.jpg";
        String outputFolder = "OutImage/";

        // 定义缩放因子
        double[] scaleFactors = {0.5, 0.75, 1.5, 2.0, 3.0};
        ScalingMethod[] methods = {ScalingMethod.BILINEAR, ScalingMethod.BICUBIC};

        // 批量处理
        batchScaleImages("input", outputFolder, scaleFactors, methods);
    }
    /**
     * 详细示例：展示不同缩放方法的效果
     */
    public static void detailedScalingExample() {
        String inputImage = "image/ewm.jpg";
        String outputFolder = "OutImage/";

        // 创建详细输出文件夹
        new File(outputFolder).mkdirs();
        // 读取原始图像
        Mat originalImage = Imgcodecs.imread(inputImage);
        if (originalImage.empty()) {
            System.err.println("无法读取示例图像: " + inputImage);
            return;
        }
        System.out.println("原始图像尺寸: " + originalImage.cols() + " x " + originalImage.rows());
        // 测试不同的缩放情况
        double[] zoomOutFactors = {0.1, 0.5};  // 缩小
        double[] zoomInFactors = {2.0, 6.0};     // 放大
        // 缩小测试
        System.out.println("\n=== 图像缩小测试 ===");
        for (double factor : zoomOutFactors) {
            for (ScalingMethod method : ScalingMethod.values()) {
                String outputPath = String.format("%s/zoom_out_%.2f_%s.jpg",
                        outputFolder, factor, method.toString().toLowerCase());
                scaleImage(inputImage, outputPath, factor, method);
            }
        }
        // 放大测试
        System.out.println("\n=== 图像放大测试 ===");
        for (double factor : zoomInFactors) {
            for (ScalingMethod method : ScalingMethod.values()) {
                String outputPath = String.format("%s/zoom_in_%.2f_%s.jpg",
                        outputFolder, factor, method.toString().toLowerCase());
                scaleImage(inputImage, outputPath, factor, method);
            }
        }

        originalImage.release();
    }
    public static void main(String[] args) {
        System.out.println("OpenCV图像缩放程序启动");
        System.out.println("OpenCV版本: " + Core.VERSION);
        detailedScalingExample();
    }
}