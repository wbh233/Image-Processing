package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;

public class ImageFusion {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // 已知的文件夹路径
        String outputFolder = "OutImage/";
        // 加载两幅图像用于融合
        Mat image1 = Imgcodecs.imread("image/2.jpg");
        Mat image2 = Imgcodecs.imread("image/3.jpg");

        if (image1.empty() || image2.empty()) {
            System.out.println("无法加载图像，请检查图像路径");
            return;
        }
        //图像融合（加权平均）
        System.out.println("\n--- 图像融合 ---");
        processImageFusion(image1, image2, outputFolder);
        //图像叠加效果
        System.out.println("\n--- 图像叠加效果 ---");
        processOverlayEffects(image1, image2, outputFolder);
        //与纯色图像融合
        System.out.println("\n--- 与纯色图像融合 ---");
        processSolidColorFusion(image1, outputFolder);
    }
    //处理图像融合
    private static void processImageFusion(Mat image1, Mat image2, String outputFolder) {
        Mat blend1 = blendImages(image1, image2, 0.7, 0.3);
        Imgcodecs.imwrite(outputFolder + "blend_70_30.jpg", blend1);

        Mat blend2 = blendImages(image1, image2, 0.5, 0.5);
        Imgcodecs.imwrite(outputFolder + "blend_50_50.jpg", blend2);

        Mat blend3 = blendImages(image1, image2, 0.3, 0.7);
        Imgcodecs.imwrite(outputFolder + "blend_30_70.jpg", blend3);

        blend1.release();
        blend2.release();
        blend3.release();
    }
    //处理图像叠加效果
    private static void processOverlayEffects(Mat image1, Mat image2, String outputFolder) {
        Mat overlay1 = overlayImages(image1, image2, 0.3);
        Imgcodecs.imwrite(outputFolder + "overlay_30_percent.jpg", overlay1);

        Mat overlay2 = overlayImages(image1, image2, 0.6);
        Imgcodecs.imwrite(outputFolder + "overlay_60_percent.jpg", overlay2);

        overlay1.release();
        overlay2.release();
    }
    //处理与纯色图像融合
    private static void processSolidColorFusion(Mat image1, String outputFolder) {
        Mat redImage = createSolidColorImage(image1.size(), new Scalar(0, 0, 255));
        Mat blueImage = createSolidColorImage(image1.size(), new Scalar(255, 0, 0));
        Mat blendWithRed = blendImages(image1, redImage, 0.7, 0.3);
        Imgcodecs.imwrite(outputFolder + "blend_with_red.jpg", blendWithRed);
        Mat blendWithBlue = blendImages(image1, blueImage, 0.7, 0.3);
        Imgcodecs.imwrite(outputFolder + "blend_with_blue.jpg", blendWithBlue);
        redImage.release();
        blueImage.release();
        blendWithRed.release();
        blendWithBlue.release();
    }
    //创建纯色图像
    public static Mat createSolidColorImage(Size size, Scalar color) {
        return new Mat(size, org.opencv.core.CvType.CV_8UC3, color);
    }
    //图像融合（加权平均）

    public static Mat blendImages(Mat image1, Mat image2, double alpha, double beta) {
        System.out.println("融合: 权重 " + alpha + ":" + beta);
        Mat image2Resized = new Mat();
        Imgproc.resize(image2, image2Resized, image1.size());
        Mat result = new Mat();
        Core.addWeighted(image1, alpha, image2Resized, beta, 0, result);
        image2Resized.release();
        return result;
    }
    //图像加法
    public static Mat addImages(Mat image1, Mat image2) {
        System.out.println("图像加法");
        Mat image2Resized = new Mat();
        Imgproc.resize(image2, image2Resized, image1.size());
        Mat result = new Mat();
        Core.add(image1, image2Resized, result);
        image2Resized.release();
        return result;
    }
    //图像减法

    public static Mat subtractImages(Mat image1, Mat image2) {
        System.out.println("图像减法");
        Mat image2Resized = new Mat();
        Imgproc.resize(image2, image2Resized, image1.size());
        Mat result = new Mat();
        Core.subtract(image1, image2Resized, result);
        image2Resized.release();
        return result;
    }
     //图像乘法（标量乘法）
    public static Mat multiplyImages(Mat image, double factor) {
        System.out.println("图像乘法: 系数 " + factor);

        Mat result = new Mat();
        Core.multiply(image, new Scalar(factor, factor, factor), result);

        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX);
        result.convertTo(result, org.opencv.core.CvType.CV_8U);

        return result;
    }
    //图像叠加（透明度混合）
    public static Mat overlayImages(Mat background, Mat overlay, double alpha) {
        System.out.println("图像叠加: 透明度 " + alpha);

        Mat overlayResized = new Mat();
        Imgproc.resize(overlay, overlayResized, background.size());

        Mat result = new Mat();
        Core.addWeighted(background, 1.0, overlayResized, alpha, 0, result);

        overlayResized.release();
        return result;
    }
    //调整图像到指定高度
    private static Mat resizeToHeight(Mat image, int height) {
        double scale = (double) height / image.rows();
        int newWidth = (int) (image.cols() * scale);

        Mat result = new Mat();
        Imgproc.resize(image, result, new Size(newWidth, height));
        return result;
    }
    //调整图像到指定宽度

    private static Mat resizeToWidth(Mat image, int width) {
        double scale = (double) width / image.cols();
        int newHeight = (int) (image.rows() * scale);

        Mat result = new Mat();
        Imgproc.resize(image, result, new Size(width, newHeight));
        return result;
    }
}