package org.example;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ImageDistance {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
     //计算均方根距离 (Root Mean Square Distance)
     //RMSD = sqrt(mean((I1 - I2)²))
    public static double calculateRMSD(Mat image1, Mat image2) {
        // 确保图像尺寸相同
        if (image1.size().width != image2.size().width ||
                image1.size().height != image2.size().height) {
            throw new IllegalArgumentException("图像尺寸必须相同");
        }
        // 转换为相同类型（灰度）
        Mat gray1 = toGrayScale(image1);
        Mat gray2 = toGrayScale(image2);
        // 计算差值平方
        Mat diff = new Mat();
        Core.absdiff(gray1, gray2, diff);
        Mat diffSquared = new Mat();
        Core.multiply(diff, diff, diffSquared);
        // 计算均值并开方
        Scalar mean = Core.mean(diffSquared);
        return Math.sqrt(mean.val[0]);
    }
    // 计算上确界距离 (Supremum Distance / Chebyshev Distance)
    //D_inf = max(|I1 - I2|)
    public static double calculateSupremumDistance(Mat image1, Mat image2) {
        if (image1.size().width != image2.size().width ||
                image1.size().height != image2.size().height) {
            throw new IllegalArgumentException("图像尺寸必须相同");
        }

        Mat gray1 = toGrayScale(image1);
        Mat gray2 = toGrayScale(image2);

        Mat diff = new Mat();
        Core.absdiff(gray1, gray2, diff);

        Core.MinMaxLocResult minMax = Core.minMaxLoc(diff);
        return minMax.maxVal; // 最大绝对差值
    }
     //计算曼哈顿距离 (L1距离)
     //D1 = sum(|I1 - I2|)
    public static double calculateManhattanDistance(Mat image1, Mat image2) {
        if (image1.size().width != image2.size().width ||
                image1.size().height != image2.size().height) {
            throw new IllegalArgumentException("图像尺寸必须相同");
        }

        Mat gray1 = toGrayScale(image1);
        Mat gray2 = toGrayScale(image2);

        Mat diff = new Mat();
        Core.absdiff(gray1, gray2, diff);

        Scalar sum = Core.sumElems(diff);
        return sum.val[0];
    }
    //计算欧几里得距离 (L2距离)
    //D2 = sqrt(sum((I1 - I2)²))
    public static double calculateEuclideanDistance(Mat image1, Mat image2) {
        if (image1.size().width != image2.size().width ||
                image1.size().height != image2.size().height) {
            throw new IllegalArgumentException("图像尺寸必须相同");
        }
        Mat gray1 = toGrayScale(image1);
        Mat gray2 = toGrayScale(image2);
        // 计算差值平方和
        Mat diff = new Mat();
        Core.absdiff(gray1, gray2, diff);
        Mat diffSquared = new Mat();
        Core.multiply(diff, diff, diffSquared);

        Scalar sum = Core.sumElems(diffSquared);
        return Math.sqrt(sum.val[0]);
    }
    //计算直方图相交距离
    public static double calculateHistogramIntersection(Mat image1, Mat image2) {
        Mat hist1 = calculateHistogram(image1);
        Mat hist2 = calculateHistogram(image2);
        // 归一化直方图
        Core.normalize(hist1, hist1, 1, 0, Core.NORM_L1);
        Core.normalize(hist2, hist2, 1, 0, Core.NORM_L1);
        // 计算交集：sum(min(hist1, hist2))
        Mat intersection = new Mat();
        Core.min(hist1, hist2, intersection);
        Scalar sum = Core.sumElems(intersection);
        return sum.val[0]; // 值越接近1表示越相似
    }
     //计算巴氏距离 (Bhattacharyya Distance)
    public static double calculateBhattacharyyaDistance(Mat image1, Mat image2) {
        Mat hist1 = calculateHistogram(image1);
        Mat hist2 = calculateHistogram(image2);
        // 归一化直方图
        Core.normalize(hist1, hist1, 1, 0, Core.NORM_L1);
        Core.normalize(hist2, hist2, 1, 0, Core.NORM_L1);
        // 计算巴氏系数
        Mat bc = new Mat();
        Core.sqrt(hist1, hist1); // hist1 = sqrt(hist1)
        Core.sqrt(hist2, hist2); // hist2 = sqrt(hist2)
        Core.multiply(hist1, hist2, bc);
        Scalar sum = Core.sumElems(bc);
        double bhattacharyyaCoeff = sum.val[0];
        // 巴氏距离
        return Math.sqrt(1 - bhattacharyyaCoeff);
    }
     // 计算PSNR (峰值信噪比)
    public static double calculatePSNR(Mat image1, Mat image2) {
        double mse = calculateMSE(image1, image2);
        if (mse == 0) {
            return Double.POSITIVE_INFINITY; // 完全相同图像
        }
        double maxPixelValue = 255.0;
        return 10.0 * Math.log10((maxPixelValue * maxPixelValue) / mse);
    }
     //计算MSE (均方误差)
    public static double calculateMSE(Mat image1, Mat image2) {
        if (image1.size().width != image2.size().width ||
                image1.size().height != image2.size().height) {
            throw new IllegalArgumentException("图像尺寸必须相同");
        }
        Mat gray1 = toGrayScale(image1);
        Mat gray2 = toGrayScale(image2);
        Mat diff = new Mat();
        Core.absdiff(gray1, gray2, diff);
        Mat diffSquared = new Mat();
        Core.multiply(diff, diff, diffSquared);
        Scalar mean = Core.mean(diffSquared);
        return mean.val[0];
    }
     //计算直方图
    private static Mat calculateHistogram(Mat image) {
        Mat gray = toGrayScale(image);
        Mat hist = new Mat();
        Imgproc.calcHist(
                java.util.Arrays.asList(gray),
                new MatOfInt(0),
                new Mat(),
                hist,
                new MatOfInt(256),
                new MatOfFloat(0, 256)
        );

        return hist;
    }
    //转换为灰度图
    private static Mat toGrayScale(Mat image) {
        if (image.channels() == 1) return image;
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }
}