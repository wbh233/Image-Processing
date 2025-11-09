package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;

public class ImageSampling {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // 已知的文件夹路径
        String outputFolder = "image/";
        // 加载图像
        Mat original = Imgcodecs.imread("image/2.jpg");
        if (original.empty()) {
            System.out.println("无法加载图像");
            return;
        }
        System.out.println("原图尺寸: " + original.cols() + "x" + original.rows());

        // 下采样（缩小图像）
        Mat downsampled = downsample(original, 0.5); // 缩小到50%
        Imgcodecs.imwrite(outputFolder + "downsampled.jpg", downsampled);

        // 上采样（放大图像）
        Mat upsampled = upsample(original, 2.0); // 放大到200%
        Imgcodecs.imwrite(outputFolder + "upsampled.jpg", upsampled);

        //  固定尺寸采样
        Mat resized = resizeTo(original, 300, 200); // 调整为300x200
        Imgcodecs.imwrite(outputFolder + "resized.jpg", resized);

        // 间隔为8的采样（新增）
        Mat interval8 = sampleWithInterval(original, 8);
        Imgcodecs.imwrite(outputFolder + "interval_8_sampled.jpg", interval8);

        System.out.println("所有图像已保存到: " + outputFolder);

        // 释放内存
        original.release();
        downsampled.release();
        upsampled.release();
        resized.release();
        interval8.release();
    }
    // 下采样 - 缩小图像
    public static Mat downsample(Mat image, double scale) {
        Size newSize = new Size(image.cols() * scale, image.rows() * scale);
        Mat result = new Mat();
        Imgproc.resize(image, result, newSize, 0, 0, Imgproc.INTER_AREA);
        System.out.println("下采样: " + image.cols() + "x" + image.rows() +
                " → " + result.cols() + "x" + result.rows());
        return result;
    }

    //上采样 - 放大图像
    public static Mat upsample(Mat image, double scale) {
        Size newSize = new Size(image.cols() * scale, image.rows() * scale);
        Mat result = new Mat();
        Imgproc.resize(image, result, newSize, 0, 0, Imgproc.INTER_LINEAR);
        System.out.println("上采样: " + image.cols() + "x" + image.rows() +
                " → " + result.cols() + "x" + result.rows());
        return result;
    }

    // 调整到固定尺寸
    public static Mat resizeTo(Mat image, int width, int height) {
        Size newSize = new Size(width, height);
        Mat result = new Mat();
        Imgproc.resize(image, result, newSize, 0, 0, Imgproc.INTER_LINEAR);
        System.out.println("调整尺寸: " + image.cols() + "x" + image.rows() +
                " → " + result.cols() + "x" + result.rows());
        return result;
    }
    // 间隔采样
    public static Mat sampleWithInterval(Mat image, int interval) {
        // 计算新图像的尺寸
        int newWidth = image.cols() / interval;
        int newHeight = image.rows() / interval;

        System.out.println("间隔采样(间隔=" + interval + "): " +
                image.cols() + "x" + image.rows() + " → " +
                newWidth + "x" + newHeight);
        System.out.println("采样率: 1/" + (interval * interval) + " (" +
                String.format("%.2f", 100.0 / (interval * interval)) + "%)");

        // 创建新图像
        Mat result = new Mat(newHeight, newWidth, image.type());

        // 进行间隔采样：每隔interval个像素取一个
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int origX = x * interval;
                int origY = y * interval;

                // 从原图获取像素值
                double[] pixel = image.get(origY, origX);
                // 设置到新图像
                result.put(y, x, pixel);
            }
        }

        return result;
    }
}