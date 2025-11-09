package org.example;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.nio.file.Files;
import java.nio.file.Paths;
public class ImageLoaderAndConverter {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        System.out.println("=== 图像加载和转换工具 ===\n");
        // 1. 加载BMP图片
        Mat bmpImage = loadBMP("image/bmp.bmp");
        // 转换BMP为JPEG
        if (!bmpImage.empty()) {
            convertToJPEG(bmpImage, "output_bmp.jpg", "BMP");
            bmpImage.release();
        }
        Mat bmpImage1 = Imgcodecs.imread("output_bmp.jpg");
        String outputFolder = "OutImage/";
        if (!bmpImage1.empty()) {
            // 存储到文件夹中
            convertToJPEG(bmpImage1, outputFolder + "output_bmp.jpg", "JPG");
            bmpImage1.release();
        }
        // 2. 加载PGM图片
        Mat pgmImage = loadPGM("image/pgm.pgm");
        // 转换PGM为JPEG
        if (!pgmImage.empty()) {
            convertToJPEG(pgmImage, "output_pgm.jpg", "PGM");
            pgmImage.release();
        }
        Mat pgmImage1 = Imgcodecs.imread("output_pgm.jpg");
        if (!pgmImage1.empty()) {
            // 存储到文件夹中
            convertToJPEG(pgmImage1, outputFolder + "output_pgm.jpg", "JPG");
            pgmImage1.release();
        }
        // 3. 加载RAW图片
        Mat rawImage = loadRAW("image/raw.raw", 640, 480);
        // 转换RAW为JPEG
        if (!rawImage.empty()) {
            convertToJPEG(rawImage, "output_raw.jpg", "RAW");
            rawImage.release();
        }
        Mat rawImage1 = Imgcodecs.imread("output_raw.jpg");
        if (!rawImage1.empty()) {
            // 存储到文件夹中
            convertToJPEG(rawImage1, outputFolder + "output_raw.jpg", "JPG");
            rawImage1.release();
        }
        Mat jpgImage = loadRAW("image/raw.raw", 640, 480);
    }
    //加载BMP图片
    public static Mat loadBMP(String filename) {
        System.out.println("正在加载BMP图片: " + filename);
        Mat image = Imgcodecs.imread(filename);
        if (image.empty()) {
            System.out.println("✗ BMP图片加载失败: " + filename);
        } else {
            System.out.println("✓ BMP图片加载成功");;
        }
        return image;
    }
    //加载PGM图片
    public static Mat loadPGM(String filename) {
        System.out.println("正在加载PGM图片: " + filename);
        Mat image = Imgcodecs.imread(filename);
        if (image.empty()) {
            System.out.println("✗ PGM图片加载失败: " + filename);
        } else {
            System.out.println("✓ PGM图片加载成功");;
        }
        return image;
    }
     // 加载RAW图片
    public static Mat loadRAW(String filename, int width, int height) {
        try {
            byte[] rawData = Files.readAllBytes(Paths.get(filename));
            Mat image = new Mat(height, width, CvType.CV_8UC1);
            image.put(0, 0, rawData);

            if (image.empty()) {
                System.out.println("✗ RAW图片加载失败");
            } else {
                System.out.println("✓ RAW图片加载成功");
            }
            return image;
        } catch (Exception e) {
            System.out.println("✗ RAW图片加载失败: " + e.getMessage());
            return new Mat();
        }
    }
    public static void convertToJPEG(Mat image, String outputFilename, String format) {
        System.out.println("正在将" + format + "转换为JPEG: " + outputFilename);
        boolean success = Imgcodecs.imwrite(outputFilename, image);
        if (success) {
            System.out.println("✓ " + format + "转换JPEG成功");
        } else {
            System.out.println("✗ " + format + "转换JPEG失败");
        }
    }
}