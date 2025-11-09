package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;

public class ImageProcessing {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // 已知的文件夹路径
        String outputFolder = "OutImage/";
        // 确保文件夹存在
        // 加载彩色图像
        Mat colorImage = Imgcodecs.imread("image/2.jpg");
        if (colorImage.empty()) {
            System.out.println("无法加载图像");
            return;
        }
        // 1. 转换为灰度图像
        System.out.println("\n=== 彩色转灰度 ===");
        Mat grayImage = convertToGrayScale(colorImage);
        // 保存灰度图像到文件夹
        String grayFilename = outputFolder + "gray_converted.jpg";
        boolean graySuccess = Imgcodecs.imwrite(grayFilename, grayImage);
        if (graySuccess) {
            System.out.println("✓ 灰度图像已保存到: " + grayFilename);
            File file = new File(grayFilename);
            if (file.exists()) {
                System.out.println("文件大小: " + file.length() + " 字节");
            }
        } else {
            System.out.println("✗ 灰度图像保存失败");
        }
        // 2. 灰度图像阈值处理
        System.out.println("\n=== 灰度图像阈值处理 ===");
        // 二进制阈值化
        Mat binary = applyBinaryThreshold(grayImage, 127);
        Imgcodecs.imwrite(outputFolder + "binary_threshold.jpg", binary);
        System.out.println("✓ 二进制阈值图像已保存");
        // 大津阈值法
        Mat otsu = applyOtsuThreshold(grayImage);
        Imgcodecs.imwrite(outputFolder + "otsu_threshold.jpg", otsu);
        System.out.println("✓ 大津阈值图像已保存");
        // 3. 灰度线性变换 - 对比度和亮度调整
        System.out.println("\n=== 灰度线性变换 ===");
        // 不同参数的对比度和亮度调整
        Mat linear1 = linearTransform(grayImage, 1.5, 30);   // a=1.5, b=30
        Imgcodecs.imwrite(outputFolder + "contrast_1.5_brightness_30.jpg", linear1);
        System.out.println("✓ 对比度1.5倍 + 亮度30 已保存");
        Mat linear4 = linearTransform(grayImage, 0.5, 80);   // a=0.5, b=80
        Imgcodecs.imwrite(outputFolder + "contrast_0.5_brightness_80.jpg", linear4);
        System.out.println("✓ 对比度0.5倍 + 亮度80 已保存");
        // 4. 伪彩色处理
        System.out.println("\n=== 伪彩色处理 ===");
        // 使用OpenCV内置的伪彩色映射
        Mat pseudoColor5 = applyOpenCVPseudoColor(grayImage, Imgproc.COLORMAP_JET);
        Imgcodecs.imwrite(outputFolder + "pseudo_color_opencv_jet.jpg", pseudoColor5);
        System.out.println("✓ OpenCV Jet伪彩色图像已保存");

        Mat pseudoColor6 = applyOpenCVPseudoColor(grayImage, Imgproc.COLORMAP_HOT);
        Imgcodecs.imwrite(outputFolder + "pseudo_color_opencv_hot.jpg", pseudoColor6);
        System.out.println("✓ OpenCV Hot伪彩色图像已保存");

        System.out.println("\n✓ 所有处理完成！图像已保存到: " + outputFolder);

        // 释放内存
        colorImage.release();
        grayImage.release();
        binary.release();
        otsu.release();
        linear1.release();
        linear4.release();
        pseudoColor5.release();
        pseudoColor6.release();
    }

    //将彩色图像转换为灰度图像
    public static Mat convertToGrayScale(Mat colorImage) {
        System.out.println("正在转换彩色图像为灰度图像...");
        Mat grayImage = new Mat();
        // 使用OpenCV的彩色转灰度函数
        Imgproc.cvtColor(colorImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        System.out.println("转换完成:");
        System.out.println("灰度图尺寸: " + grayImage.cols() + "x" + grayImage.rows());
        System.out.println("灰度图通道数: " + grayImage.channels());

        return grayImage;
    }
    // 二进制阈值化
     // 大于阈值的设为最大值，其他设为0
    public static Mat applyBinaryThreshold(Mat grayImage, int thresholdValue) {
        System.out.println("二进制阈值化 - 阈值: " + thresholdValue);
        Mat result = new Mat();
        Imgproc.threshold(grayImage, result, thresholdValue, 255, Imgproc.THRESH_BINARY);
        return result;
    }
    //大津阈值法 - 自动确定最佳阈值
    public static Mat applyOtsuThreshold(Mat grayImage) {
        System.out.println("大津阈值法 - 自动确定阈值");
        Mat result = new Mat();
        double threshold = Imgproc.threshold(grayImage, result, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        System.out.println("  自动确定的阈值: " + threshold);
        return result;
    }
    /**
     * 灰度线性变换 - 对比度和亮度同时调整
     * 公式: g(x,y) = a * f(x,y) + b
     * @param grayImage 输入灰度图像
     * @param a 斜率（控制对比度）
     * @param b 截距（控制亮度）
     * @return 变换后的图像
     */
    public static Mat linearTransform(Mat grayImage, double a, double b) {
        System.out.println(String.format("线性变换: 对比度=%.1f, 亮度=%.0f", a, b));
        Mat result = new Mat();
        grayImage.convertTo(result, -1, a, b);  // -1表示输出类型与输入相同
        // 确保像素值在0-255范围内
        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX);
        result.convertTo(result, org.opencv.core.CvType.CV_8U);

        return result;
    }

    //应用Jet伪彩色映射
    public static Mat applyPseudoColorJet(Mat grayImage) {
        System.out.println("应用Jet伪彩色映射");
        Mat result = new Mat(grayImage.size(), org.opencv.core.CvType.CV_8UC3);

        for (int i = 0; i < grayImage.rows(); i++) {
            for (int j = 0; j < grayImage.cols(); j++) {
                double value = grayImage.get(i, j)[0];
                double[] color = getJetColor(value / 255.0);
                result.put(i, j, color[2] * 255, color[1] * 255, color[0] * 255); // BGR格式
            }
        }
        return result;
    }

    //应用Hot伪彩色映射

    public static Mat applyPseudoColorHot(Mat grayImage) {
        System.out.println("应用Hot伪彩色映射");
        Mat result = new Mat(grayImage.size(), org.opencv.core.CvType.CV_8UC3);

        for (int i = 0; i < grayImage.rows(); i++) {
            for (int j = 0; j < grayImage.cols(); j++) {
                double value = grayImage.get(i, j)[0];
                double[] color = getHotColor(value / 255.0);
                result.put(i, j, color[2] * 255, color[1] * 255, color[0] * 255); // BGR格式
            }
        }
        return result;
    }
    //使用OpenCV内置的伪彩色映射
    public static Mat applyOpenCVPseudoColor(Mat grayImage, int colormap) {
        String[] colormapNames = {"AUTUMN", "BONE", "JET", "WINTER", "RAINBOW", "OCEAN",
                "SUMMER", "SPRING", "COOL", "HSV", "HOT", "PINK", "PARULA"};
        System.out.println("应用OpenCV伪彩色映射: " + colormapNames[colormap]);

        Mat result = new Mat();
        Imgproc.applyColorMap(grayImage, result, colormap);
        return result;
    }
    //Jet颜色映射
    private static double[] getJetColor(double value) {
        double[] color = new double[3];
        if (value < 0.125) {
            color[0] = 0;
            color[1] = 0;
            color[2] = 0.5 + 4 * value;
        } else if (value < 0.375) {
            color[0] = 0;
            color[1] = 4 * (value - 0.125);
            color[2] = 1;
        } else if (value < 0.625) {
            color[0] = 4 * (value - 0.375);
            color[1] = 1;
            color[2] = 1 - 4 * (value - 0.375);
        } else if (value < 0.875) {
            color[0] = 1;
            color[1] = 1 - 4 * (value - 0.625);
            color[2] = 0;
        } else {
            color[0] = 1 - 4 * (value - 0.875);
            color[1] = 0;
            color[2] = 0;
        }
        return color;
    }
    // Hot颜色映射
    private static double[] getHotColor(double value) {
        double[] color = new double[3];
        if (value < 0.4) {
            color[0] = value / 0.4;
            color[1] = 0;
            color[2] = 0;
        } else if (value < 0.8) {
            color[0] = 1;
            color[1] = (value - 0.4) / 0.4;
            color[2] = 0;
        } else {
            color[0] = 1;
            color[1] = 1;
            color[2] = (value - 0.8) / 0.2;
        }
        return color;
    }



}