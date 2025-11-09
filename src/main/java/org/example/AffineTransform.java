package org.example;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;

public class AffineTransform {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // 旋转变换
    public static void rotateImage(Mat src, String outputPath, double angle) {
        // 获取图像中心
        Point center = new Point(src.cols() / 2.0, src.rows() / 2.0);
        // 计算旋转矩阵
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        // 执行旋转
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, rotationMatrix, src.size());
        // 保存结果
        Imgcodecs.imwrite(outputPath, dst);
        System.out.println("旋转变换完成: " + outputPath);
    }
    // 缩放变换
    public static void scaleImage(Mat src, String outputPath, double scaleX, double scaleY) {
        // 创建缩放矩阵
        Mat scaleMatrix = new Mat(2, 3, CvType.CV_64FC1);
        scaleMatrix.put(0, 0, scaleX, 0, 0);
        scaleMatrix.put(1, 0, 0, scaleY, 0);
        // 计算新尺寸
        Size newSize = new Size(src.cols() * scaleX, src.rows() * scaleY);
        // 执行缩放
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, scaleMatrix, newSize);
        // 保存结果
        Imgcodecs.imwrite(outputPath, dst);
        System.out.println("缩放变换完成: " + outputPath);
    }
    // 错切变换（剪切变换）
    public static void shearImage(Mat src, String outputPath, double shearX, double shearY) {
        // 创建错切矩阵
        Mat shearMatrix = new Mat(2, 3, CvType.CV_64FC1);
        shearMatrix.put(0, 0, 1, shearX, 0);
        shearMatrix.put(1, 0, shearY, 1, 0);
        // 计算新尺寸以适应错切后的图像
        int newWidth = (int) (src.cols() + Math.abs(shearX) * src.rows());
        int newHeight = (int) (src.rows() + Math.abs(shearY) * src.cols());
        Size newSize = new Size(newWidth, newHeight);
        // 调整平移分量以使图像居中显示
        if (shearX > 0) {
            shearMatrix.put(0, 2, shearX * src.rows() / 2);
        }
        if (shearY > 0) {
            shearMatrix.put(1, 2, shearY * src.cols() / 2);
        }
        // 执行错切
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, shearMatrix, newSize);
        // 保存结果
        Imgcodecs.imwrite(outputPath, dst);
        System.out.println("错切变换完成: " + outputPath);
    }
    // 平移变换
    public static void translateImage(Mat src, String outputPath, double translateX, double translateY) {
        // 创建平移矩阵
        Mat translationMatrix = new Mat(2, 3, CvType.CV_64FC1);
        translationMatrix.put(0, 0, 1, 0, translateX);
        translationMatrix.put(1, 0, 0, 1, translateY);
        // 计算新尺寸
        Size newSize = new Size(src.cols() + (int)Math.abs(translateX),
                src.rows() + (int)Math.abs(translateY));
        // 执行平移
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, translationMatrix, newSize);
        // 保存结果
        Imgcodecs.imwrite(outputPath, dst);
        System.out.println("平移变换完成: " + outputPath);
    }
    // 使用三点法进行仿射变换（更通用的方法）
    public static void affineTransformByPoints(Mat src, String outputPath) {
        // 定义原始图像中的三个点（三角形）
        MatOfPoint2f srcPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(src.cols() - 1, 0),
                new Point(0, src.rows() - 1)
        );
        // 定义变换后的三个点
        MatOfPoint2f dstPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(src.cols() - 1, 30),  // 第二个点向下移动
                new Point(50, src.rows() - 1)   // 第三个点向右移动
        );
        // 计算仿射变换矩阵
        Mat affineMatrix = Imgproc.getAffineTransform(srcPoints, dstPoints);
        // 执行变换
        Mat dst = new Mat();
        Imgproc.warpAffine(src, dst, affineMatrix, src.size());
        // 保存结果
        Imgcodecs.imwrite(outputPath, dst);
        System.out.println("三点法仿射变换完成: " + outputPath);
    }

    public static void main(String[] args) {
        // 输入和输出路径
        String inputImagePath = "image/ewm.jpg";  // 替换为你的输入图片路径
        String outputFolder = "OutImage/";      // 替换为你的输出文件夹路径
        // 读取输入图像
        Mat src = Imgcodecs.imread(inputImagePath);
        try {
            // 旋转变换 - 旋转45度
            rotateImage(src, outputFolder + "rotated_30.jpg", 30);
            // 旋转变换 - 旋转-30度
            rotateImage(src, outputFolder + "rotated_-20.jpg", -20);
            // 缩放变换 - 放大1.5倍
            scaleImage(src, outputFolder + "scaled_1.5x.jpg", 1.5, 1.5);
            // 错切变换 - X方向错切
            shearImage(src, outputFolder + "shear_x.jpg", 0.3, 0);
            // 错切变换 - Y方向错切
            shearImage(src, outputFolder + "shear_y.jpg", 0, 0.3);
            // 平移变换 - 向右下角平移
            translateImage(src, outputFolder + "translated.jpg", 50, 50);
            // 三点法仿射变换
            affineTransformByPoints(src, outputFolder + "affine_by_points.jpg");
        } catch (Exception e) {
            System.out.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 释放内存
            src.release();
        }
    }
}