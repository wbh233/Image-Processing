package org.example;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import java.util.Arrays;

public class ImageStatistics {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    //计算图像熵
    public static double calculateEntropy(Mat image) {
        Mat gray = toGrayScale(image);
        Mat hist = calculateHistogram(gray);
        double totalPixels = gray.total();
        double entropy = 0.0;
        for (int i = 0; i < hist.rows(); i++) {
            double count = hist.get(i, 0)[0];
            if (count > 0) {
                double p = count / totalPixels;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }
    //灰度直方图
    public static Mat calculateHistogram(Mat image) {
        Mat gray = toGrayScale(image);
        Mat hist = new Mat();
        Imgproc.calcHist(
                Arrays.asList(gray),
                new MatOfInt(0),
                new Mat(),
                hist,
                new MatOfInt(256),
                new MatOfFloat(0, 256)
        );
        return hist;
    }
    //灰度中值
    public static double calculateMedian(Mat image) {
        Mat gray = toGrayScale(image);
        // 使用排序找中值
        Mat sorted = new Mat();
        gray.reshape(1).copyTo(sorted);
        Core.sort(sorted, sorted, Core.SORT_ASCENDING);
        int totalPixels = sorted.rows();
        if (totalPixels % 2 == 1) {
            return sorted.get(totalPixels / 2, 0)[0];
        } else {
            double val1 = sorted.get(totalPixels / 2 - 1, 0)[0];
            double val2 = sorted.get(totalPixels / 2, 0)[0];
            return (val1 + val2) / 2.0;
        }
    }
    //计算方差
    public static double calculateVariance(Mat image) {
        Mat gray = toGrayScale(image);
        // 使用meanStdDev同时获取均值和标准差
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(gray, mean, stddev);
        double stddevValue = stddev.get(0, 0)[0];
        return stddevValue * stddevValue; // 方差 = 标准差的平方
    }
     //计算平均值
    public static double calculateMean(Mat image) {
        Mat gray = toGrayScale(image);
        return Core.mean(gray).val[0];
    }
    //绘制直方图
    public static void drawHistogram(Mat image, String windowName) {
        Mat gray = toGrayScale(image);
        Mat hist = calculateHistogram(gray);
        // 创建直方图图像
        int histWidth = 512;
        int histHeight = 400;
        int binWidth = (int) Math.round(histWidth / 256.0);
        Mat histImage = new Mat(histHeight, histWidth, CvType.CV_8UC3, new Scalar(0, 0, 0));
        // 归一化直方图到图像高度
        Core.normalize(hist, hist, 0, histImage.rows(), Core.NORM_MINMAX);
        // 绘制直方图
        for (int i = 1; i < 256; i++) {
            Imgproc.line(
                    histImage,
                    new Point(binWidth * (i - 1), histHeight - hist.get(i - 1, 0)[0]),
                    new Point(binWidth * i, histHeight - hist.get(i, 0)[0]),
                    new Scalar(255, 255, 255),
                    2
            );
        }
        // 显示直方图
        HighGui.imshow(windowName, histImage);
    }
    //灰度图
    private static Mat toGrayScale(Mat image) {
        if (image.channels() == 1) return image;
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }
    public static void main(String[] args) {
        // 读取图像
        Mat image = Imgcodecs.imread("image/2.jpg");

        if (image.empty()) {
            System.out.println("无法加载图像");
            return;
        }
        // 绘制直方图
        drawHistogram(image, "灰度直方图");
        // 显示原图
        HighGui.imshow("原图像", image);
        // 等待按键
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
        // 简单使用
        Mat img1 = Imgcodecs.imread("image/2.jpg");
        Mat img2 = Imgcodecs.imread("image/3.jpg");

        double rmsd = ImageDistance.calculateRMSD(img1, img2);
        double supremum = ImageDistance.calculateSupremumDistance(img1, img2);
        double psnr = ImageDistance.calculatePSNR(img1, img2);
        System.out.printf("RMSD: %.2f, Supremum: %.2f, PSNR: %.2f dB\n",
                rmsd, supremum, psnr);
        double entropy = calculateEntropy(image);
        double median = calculateMedian(image);
        double variance = calculateVariance(image);
        double mean = calculateMean(image);
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(toGrayScale(image), new MatOfDouble(), stddev);
        System.out.printf("图像熵: %.4f bits\n", entropy);
        System.out.printf("灰度平均值: %.2f\n", mean);
        System.out.printf("灰度中值: %.2f\n", median);
        System.out.printf("方差: %.2f\n", variance);
        System.out.printf("标准差: %.2f\n", stddev.get(0, 0)[0]);

        // 释放内存
        image.release();

    }
}