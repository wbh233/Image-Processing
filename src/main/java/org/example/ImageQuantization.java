package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageQuantization {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        // 创建输出文件夹
        String outputFolder = "OutImage/";

        // 加载图像
        Mat original = Imgcodecs.imread("image/2.jpg");
        if (original.empty()) {
            System.out.println("无法加载图像");
            return;
        }
        System.out.println("原图尺寸: " + original.cols() + "x" + original.rows());

        // 1. 均匀量化 - 减少颜色深度
        Mat uniform2 = uniformQuantization(original, 2);  // 8级量化
        Imgcodecs.imwrite(outputFolder + "uniform_2_levels.jpg", uniform2);
        Mat uniform8 = uniformQuantization(original, 8);  // 8级量化
        Imgcodecs.imwrite(outputFolder + "uniform_8_levels.jpg", uniform8);

        Mat uniform16 = uniformQuantization(original, 16); // 16级量化
        Imgcodecs.imwrite(outputFolder + "uniform_16_levels.jpg", uniform16);

        // 2. K-means聚类量化
        Mat kmeans8 = kmeansQuantization(original, 8);    // 8种颜色
        Imgcodecs.imwrite(outputFolder + "kmeans_8_colors.jpg", kmeans8);

        Mat kmeans16 = kmeansQuantization(original, 16);  // 16种颜色
        Imgcodecs.imwrite(outputFolder + "kmeans_16_colors.jpg", kmeans16);


        // 3. 位深度量化
        Mat bit4 = reduceBitDepth(original, 4);  // 4位量化
        Imgcodecs.imwrite(outputFolder + "4_bit_quantization.jpg", bit4);

        Mat bit2 = reduceBitDepth(original, 2);  // 2位量化
        Imgcodecs.imwrite(outputFolder + "2_bit_quantization.jpg", bit2);

        System.out.println("所有量化图像已保存到: " + outputFolder);

        // 释放内存
        original.release();
        uniform8.release();
        uniform16.release();
        kmeans8.release();
        kmeans16.release();
        bit4.release();
        bit2.release();
    }
    /**
     * 均匀量化 - 减少颜色级别
     */
    public static Mat uniformQuantization(Mat image, int levels) {
        System.out.println("均匀量化: " + levels + " 级");

        Mat result = new Mat();
        image.copyTo(result);

        // 计算量化步长
        int step = 256 / levels;

        // 对每个像素进行量化
        for (int i = 0; i < result.rows(); i++) {
            for (int j = 0; j < result.cols(); j++) {
                double[] pixel = result.get(i, j);
                for (int k = 0; k < pixel.length; k++) {
                    // 量化公式：将像素值映射到最近的量化级别
                    pixel[k] = Math.round(pixel[k] / step) * step;
                    // 确保值在0-255范围内
                    pixel[k] = Math.max(0, Math.min(255, pixel[k]));
                }
                result.put(i, j, pixel);
            }
        }

        return result;
    }

    // K-means聚类量化 - 最常用的量化方法
    public static Mat kmeansQuantization(Mat image, int k) {
        System.out.println("K-means量化: " + k + " 种颜色");

        // 将图像数据转换为适合K-means的格式
        Mat data = new Mat();
        image.reshape(1, image.rows() * image.cols()).convertTo(data, org.opencv.core.CvType.CV_32F);

        // 设置K-means参数
        Mat labels = new Mat();
        Mat centers = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 100, 0.1);

        // 执行K-means聚类
        Core.kmeans(data, k, labels, criteria, 10, Core.KMEANS_PP_CENTERS, centers);

        // 将聚类结果转换回图像
        Mat result = new Mat(image.size(), image.type());
        for (int i = 0; i < image.rows(); i++) {
            for (int j = 0; j < image.cols(); j++) {
                int label = (int) labels.get(i * image.cols() + j, 0)[0];
                double[] center = centers.get(label, 0);
                result.put(i, j, center);
            }
        }

        return result;
    }

    // 减少位深度量化
    public static Mat reduceBitDepth(Mat image, int bits) {
        System.out.println("位深度量化: " + bits + " 位");

        Mat result = new Mat();
        image.copyTo(result);

        // 计算最大值和量化步长
        int maxValue = (1 << bits) - 1;  // 2^bits - 1
        double scale = 255.0 / maxValue;

        for (int i = 0; i < result.rows(); i++) {
            for (int j = 0; j < result.cols(); j++) {
                double[] pixel = result.get(i, j);
                for (int k = 0; k < pixel.length; k++) {
                    // 量化为指定的位深度
                    int quantized = (int) Math.round(pixel[k] / 255.0 * maxValue);
                    pixel[k] = quantized * scale;
                }
                result.put(i, j, pixel);
            }
        }

        return result;
    }
}