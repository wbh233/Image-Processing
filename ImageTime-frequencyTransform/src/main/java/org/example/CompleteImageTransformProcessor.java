package org.example;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.*;

//opencv函数实现
public class CompleteImageTransformProcessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // 已知的输入图片路径和输出目录
    private static final String INPUT_IMAGE_PATH = "src/image/image.jpg";
    private static final String OUTPUT_DIRECTORY = "src/outImage/";

    // FFT变换类
    public static class FFTTransformer {
        public static Mat transform(Mat src) {
            Mat gray = convertToGray(src);
            Mat floatMat = new Mat();
            gray.convertTo(floatMat, CvType.CV_32F);
            Mat padded = optimizeSize(floatMat);
            Mat zeros = Mat.zeros(padded.size(), CvType.CV_32F);
            Mat complexI = new Mat();
            Core.merge(Arrays.asList(padded, zeros), complexI);
            Core.dft(complexI, complexI);
            return complexI;
        }
        public static Mat inverseTransform(Mat complexI) {
            Mat inverse = new Mat();
            Core.dft(complexI, inverse, Core.DFT_INVERSE | Core.DFT_REAL_OUTPUT);
            Core.normalize(inverse, inverse, 0, 255, Core.NORM_MINMAX);
            Mat result = new Mat();
            inverse.convertTo(result, CvType.CV_8U);
            return result;
        }
        public static Mat getMagnitudeSpectrum(Mat complexI) {
            List<Mat> planes = new ArrayList<>();
            Core.split(complexI, planes);
            Mat magnitude = new Mat();
            Core.magnitude(planes.get(0), planes.get(1), magnitude);
            Mat mag = magnitude.clone();
            Core.add(Mat.ones(mag.size(), mag.type()), mag, mag);
            Core.log(mag, mag);
            shiftSpectrum(mag);
            Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX);
            mag.convertTo(mag, CvType.CV_8U);
            return mag;
        }

        public static Mat compress(Mat src, double thresholdRatio) {
            Mat complexI = transform(src);
            List<Mat> planes = new ArrayList<>();
            Core.split(complexI, planes);

            Mat real = planes.get(0);
            Mat imag = planes.get(1);

            double maxReal = Core.minMaxLoc(real).maxVal;
            double maxImag = Core.minMaxLoc(imag).maxVal;
            double thresholdReal = maxReal * thresholdRatio;
            double thresholdImag = maxImag * thresholdRatio;

            int zeroCount = 0;
            int totalCount = real.rows() * real.cols() * 2;

            for (int i = 0; i < real.rows(); i++) {
                for (int j = 0; j < real.cols(); j++) {
                    if (Math.abs(real.get(i, j)[0]) < thresholdReal) {
                        real.put(i, j, 0);
                        zeroCount++;
                    }
                    if (Math.abs(imag.get(i, j)[0]) < thresholdImag) {
                        imag.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }

            Mat compressedComplex = new Mat();
            Core.merge(Arrays.asList(real, imag), compressedComplex);
            System.out.println("FFT压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");

            return inverseTransform(compressedComplex);
        }

        // FFT低通滤波
        public static Mat lowPassFilter(Mat src, double cutoffRatio) {
            Mat complexI = transform(src);
            List<Mat> planes = new ArrayList<>();
            Core.split(complexI, planes);

            Mat real = planes.get(0);
            Mat imag = planes.get(1);

            int centerX = real.cols() / 2;
            int centerY = real.rows() / 2;
            int radius = (int) (Math.min(centerX, centerY) * cutoffRatio);

            System.out.println("FFT低通滤波 - 截止半径: " + radius + " (比率: " + cutoffRatio + ")");

            // 创建低通滤波器（保留低频，去除高频）
            for (int i = 0; i < real.rows(); i++) {
                for (int j = 0; j < real.cols(); j++) {
                    double dist = Math.sqrt(Math.pow(i - centerY, 2) + Math.pow(j - centerX, 2));
                    if (dist > radius) {
                        real.put(i, j, 0);
                        imag.put(i, j, 0);
                    }
                }
            }

            Mat filteredComplex = new Mat();
            Core.merge(Arrays.asList(real, imag), filteredComplex);
            return inverseTransform(filteredComplex);
        }

        // FFT高通滤波
        public static Mat highPassFilter(Mat src, double cutoffRatio) {
            Mat complexI = transform(src);
            List<Mat> planes = new ArrayList<>();
            Core.split(complexI, planes);

            Mat real = planes.get(0);
            Mat imag = planes.get(1);

            int centerX = real.cols() / 2;
            int centerY = real.rows() / 2;
            int radius = (int) (Math.min(centerX, centerY) * cutoffRatio);

            System.out.println("FFT高通滤波 - 截止半径: " + radius + " (比率: " + cutoffRatio + ")");

            // 创建高通滤波器（保留高频，去除低频）
            for (int i = 0; i < real.rows(); i++) {
                for (int j = 0; j < real.cols(); j++) {
                    double dist = Math.sqrt(Math.pow(i - centerY, 2) + Math.pow(j - centerX, 2));
                    if (dist <= radius) {
                        real.put(i, j, 0);
                        imag.put(i, j, 0);
                    }
                }
            }

            Mat filteredComplex = new Mat();
            Core.merge(Arrays.asList(real, imag), filteredComplex);
            return inverseTransform(filteredComplex);
        }

        private static void shiftSpectrum(Mat mag) {
            int cx = mag.cols() / 2;
            int cy = mag.rows() / 2;
            Mat q0 = new Mat(mag, new org.opencv.core.Rect(0, 0, cx, cy));
            Mat q1 = new Mat(mag, new org.opencv.core.Rect(cx, 0, cx, cy));
            Mat q2 = new Mat(mag, new org.opencv.core.Rect(0, cy, cx, cy));
            Mat q3 = new Mat(mag, new org.opencv.core.Rect(cx, cy, cx, cy));
            Mat tmp = new Mat();
            q0.copyTo(tmp);
            q3.copyTo(q0);
            tmp.copyTo(q3);
            q1.copyTo(tmp);
            q2.copyTo(q1);
            tmp.copyTo(q2);
        }
    }

    // DCT变换类
    public static class DCTTransformer {
        public static Mat transform(Mat src) {
            Mat gray = convertToGray(src);
            Mat floatMat = new Mat();
            gray.convertTo(floatMat, CvType.CV_32F);
            Mat dct = new Mat();
            Core.dct(floatMat, dct);
            return dct;
        }
        public static Mat inverseTransform(Mat dct) {
            Mat idct = new Mat();
            Core.dct(dct, idct, Core.DCT_INVERSE);

            Mat result = new Mat();
            idct.convertTo(result, CvType.CV_8U);
            return result;
        }
        public static Mat visualize(Mat dct) {
            Mat mag = dct.clone();
            Core.log(mag, mag);
            Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX);
            mag.convertTo(mag, CvType.CV_8U);
            return mag;
        }
        public static Mat compressByRegion(Mat src, double ratio) {
            Mat dct = transform(src);
            Mat compressed = dct.clone();
            int keepRows = (int)(dct.rows() * ratio);
            int keepCols = (int)(dct.cols() * ratio);
            int zeroCount = 0;
            int totalCount = dct.rows() * dct.cols();
            for (int i = 0; i < dct.rows(); i++) {
                for (int j = 0; j < dct.cols(); j++) {
                    if (i >= keepRows || j >= keepCols) {
                        compressed.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }
            System.out.println("DCT区域压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");
            return inverseTransform(compressed);
        }
        public static Mat compressByThreshold(Mat src, double thresholdRatio) {
            Mat dct = transform(src);
            Mat compressed = dct.clone();
            double maxVal = Core.minMaxLoc(dct).maxVal;
            double threshold = maxVal * thresholdRatio;
            int zeroCount = 0;
            int totalCount = dct.rows() * dct.cols();
            for (int i = 0; i < dct.rows(); i++) {
                for (int j = 0; j < dct.cols(); j++) {
                    if (Math.abs(dct.get(i, j)[0]) < threshold) {
                        compressed.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }
            System.out.println("DCT阈值压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");
            return inverseTransform(compressed);
        }

        // DCT低通滤波
        public static Mat lowPassFilter(Mat src, double cutoffRatio) {
            Mat dct = transform(src);
            Mat filtered = dct.clone();

            int cutoffRow = (int) (dct.rows() * cutoffRatio);
            int cutoffCol = (int) (dct.cols() * cutoffRatio);

            System.out.println("DCT低通滤波 - 保留区域: " + cutoffRow + "x" + cutoffCol);

            int zeroCount = 0;
            // 低通滤波：保留左上角低频系数
            for (int i = 0; i < dct.rows(); i++) {
                for (int j = 0; j < dct.cols(); j++) {
                    if (i >= cutoffRow || j >= cutoffCol) {
                        filtered.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }

            System.out.println("DCT低通滤波 - 置零系数: " + zeroCount);
            return inverseTransform(filtered);
        }

        // DCT高通滤波
        public static Mat highPassFilter(Mat src, double cutoffRatio) {
            Mat dct = transform(src);
            Mat filtered = dct.clone();

            int cutoffRow = (int) (dct.rows() * cutoffRatio);
            int cutoffCol = (int) (dct.cols() * cutoffRatio);

            System.out.println("DCT高通滤波 - 去除区域: " + cutoffRow + "x" + cutoffCol);

            int zeroCount = 0;
            // 高通滤波：保留右下角高频系数
            for (int i = 0; i < cutoffRow; i++) {
                for (int j = 0; j < cutoffCol; j++) {
                    filtered.put(i, j, 0);
                    zeroCount++;
                }
            }

            System.out.println("DCT高通滤波 - 置零系数: " + zeroCount);
            return inverseTransform(filtered);
        }
    }

    // 沃尔什-哈达玛变换类
    public static class WHTTransformer {
        public static Mat transform(Mat src) {
            Mat gray = convertToGray(src);
            int size = getPowerOfTwoSize(Math.max(gray.rows(), gray.cols()));
            Mat resized = new Mat();
            Imgproc.resize(gray, resized, new Size(size, size));
            Mat floatMat = new Mat();
            resized.convertTo(floatMat, CvType.CV_32F);
            return walshHadamardTransform(floatMat);
        }
        public static Mat inverseTransform(Mat wht) {
            Mat iwht = walshHadamardTransform(wht);
            Core.normalize(iwht, iwht, 0, 255, Core.NORM_MINMAX);
            Mat result = new Mat();
            iwht.convertTo(result, CvType.CV_8U);
            return result;
        }
        public static Mat visualize(Mat wht) {
            Mat viz = wht.clone();
            Core.normalize(viz, viz, 0, 255, Core.NORM_MINMAX);
            viz.convertTo(viz, CvType.CV_8U);
            return viz;
        }
        public static Mat compressByThreshold(Mat src, double thresholdRatio) {
            Mat wht = transform(src);
            Mat compressed = wht.clone();
            double maxVal = Core.minMaxLoc(wht).maxVal;
            double threshold = maxVal * thresholdRatio;
            int zeroCount = 0;
            int totalCount = wht.rows() * wht.cols();
            for (int i = 0; i < wht.rows(); i++) {
                for (int j = 0; j < wht.cols(); j++) {
                    double val = wht.get(i, j)[0];
                    if (Math.abs(val) < threshold) {
                        compressed.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }
            System.out.println("WHT阈值压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");
            return inverseTransform(compressed);
        }
        public static Mat compressByTopCoefficients(Mat src, int topK) {
            Mat wht = transform(src);
            Mat compressed = Mat.zeros(wht.size(), wht.type());

            List<Coefficient> coefficients = new ArrayList<>();
            for (int i = 0; i < wht.rows(); i++) {
                for (int j = 0; j < wht.cols(); j++) {
                    double value = Math.abs(wht.get(i, j)[0]);
                    coefficients.add(new Coefficient(i, j, value, wht.get(i, j)[0]));
                }
            }
            coefficients.sort((a, b) -> Double.compare(b.absValue, a.absValue));
            int keepCount = Math.min(topK, coefficients.size());
            for (int i = 0; i < keepCount; i++) {
                Coefficient coeff = coefficients.get(i);
                compressed.put(coeff.row, coeff.col, coeff.originalValue);
            }
            System.out.println("WHT前" + keepCount + "系数压缩率: " +
                    String.format("%.2f", (1 - (double)keepCount/coefficients.size()) * 100) + "%");
            return inverseTransform(compressed);
        }
        public static Mat compressByRegion(Mat src, double regionRatio) {
            Mat wht = transform(src);
            Mat compressed = wht.clone();
            int keepSize = (int)(wht.rows() * regionRatio);
            int zeroCount = 0;
            int totalCount = wht.rows() * wht.cols();
            for (int i = keepSize; i < wht.rows(); i++) {
                for (int j = 0; j < wht.cols(); j++) {
                    compressed.put(i, j, 0);
                    zeroCount++;
                }
            }
            for (int i = 0; i < wht.rows(); i++) {
                for (int j = keepSize; j < wht.cols(); j++) {
                    compressed.put(i, j, 0);
                    zeroCount++;
                }
            }
            System.out.println("WHT区域压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");
            return inverseTransform(compressed);
        }
        private static Mat walshHadamardTransform(Mat input) {
            int n = input.rows();
            Mat output = input.clone();
            whtRecursive(output, n);
            Core.divide(output, Scalar.all(Math.sqrt(n)), output);
            return output;
        }
        private static void whtRecursive(Mat mat, int size) {
            if (size <= 1) return;
            int half = size / 2;
            whtRecursive(mat, half);
            for (int i = 0; i < half; i++) {
                for (int j = 0; j < mat.cols(); j++) {
                    double a = mat.get(i, j)[0];
                    double b = mat.get(i + half, j)[0];
                    mat.put(i, j, a + b);
                    mat.put(i + half, j, a - b);
                }
            }
        }
        private static int getPowerOfTwoSize(int size) {
            int power = 1;
            while (power < size) power <<= 1;
            return power;
        }
    }

    // 小波变换类
    public static class WaveletTransformer {
        public static Mat transform(Mat src) {
            Mat gray = convertToGray(src);
            Mat floatMat = new Mat();
            gray.convertTo(floatMat, CvType.CV_32F);
            return haarWaveletTransform(floatMat);
        }
        public static Mat inverseTransform(Mat wavelet) {
            Mat reconstructed = inverseHaarWavelet(wavelet);
            Core.normalize(reconstructed, reconstructed, 0, 255, Core.NORM_MINMAX);
            Mat result = new Mat();
            reconstructed.convertTo(result, CvType.CV_8U);
            return result;
        }
        public static Mat visualize(Mat wavelet) {
            Mat viz = wavelet.clone();
            Core.normalize(viz, viz, 0, 255, Core.NORM_MINMAX);
            viz.convertTo(viz, CvType.CV_8U);
            return viz;
        }
        public static Mat compressByThreshold(Mat src, double thresholdRatio) {
            Mat wavelet = transform(src);
            Mat compressed = wavelet.clone();
            double maxVal = Core.minMaxLoc(wavelet).maxVal;
            double threshold = maxVal * thresholdRatio;
            int zeroCount = 0;
            int totalCount = wavelet.rows() * wavelet.cols();
            for (int i = 0; i < wavelet.rows(); i++) {
                for (int j = 0; j < wavelet.cols(); j++) {
                    if (Math.abs(wavelet.get(i, j)[0]) < threshold) {
                        compressed.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }
            System.out.println("小波阈值压缩率: " + String.format("%.2f", (double)zeroCount/totalCount*100) + "%");
            return inverseTransform(compressed);
        }

        //小波低通滤波
        public static Mat lowPassFilter(Mat src, double energyRatio) {
            Mat wavelet = transform(src);
            Mat filtered = wavelet.clone();

            // 计算总能量
            double totalEnergy = 0;
            List<Double> coefficients = new ArrayList<>();
            for (int i = 0; i < wavelet.rows(); i++) {
                for (int j = 0; j < wavelet.cols(); j++) {
                    double val = wavelet.get(i, j)[0];
                    totalEnergy += val * val;
                    coefficients.add(Math.abs(val));
                }
            }

            // 排序系数
            coefficients.sort(Collections.reverseOrder());

            // 计算保留的能量阈值
            double targetEnergy = totalEnergy * energyRatio;
            double currentEnergy = 0;
            double threshold = 0;

            for (double coeff : coefficients) {
                currentEnergy += coeff * coeff;
                if (currentEnergy >= targetEnergy) {
                    threshold = coeff;
                    break;
                }
            }
            // 应用阈值滤波（低通）
            int zeroCount = 0;
            for (int i = 0; i < wavelet.rows(); i++) {
                for (int j = 0; j < wavelet.cols(); j++) {
                    if (Math.abs(wavelet.get(i, j)[0]) < threshold) {
                        filtered.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }

            System.out.println("小波低通滤波 - 保留能量: " + String.format("%.2f", energyRatio*100) + "%, 置零系数: " + zeroCount);
            return inverseTransform(filtered);
        }

        // 小波高通滤波
        public static Mat highPassFilter(Mat src, double energyRatio) {
            Mat wavelet = transform(src);
            Mat filtered = wavelet.clone();

            // 计算总能量
            double totalEnergy = 0;
            List<Double> coefficients = new ArrayList<>();
            for (int i = 0; i < wavelet.rows(); i++) {
                for (int j = 0; j < wavelet.cols(); j++) {
                    double val = wavelet.get(i, j)[0];
                    totalEnergy += val * val;
                    coefficients.add(Math.abs(val));
                }
            }

            // 排序系数
            coefficients.sort(Collections.reverseOrder());

            // 计算去除的能量阈值（保留高频）
            double targetEnergy = totalEnergy * (1 - energyRatio);
            double currentEnergy = 0;
            double threshold = 0;

            for (double coeff : coefficients) {
                currentEnergy += coeff * coeff;
                if (currentEnergy >= targetEnergy) {
                    threshold = coeff;
                    break;
                }
            }

            // 应用阈值滤波（高通）
            int zeroCount = 0;
            for (int i = 0; i < wavelet.rows(); i++) {
                for (int j = 0; j < wavelet.cols(); j++) {
                    if (Math.abs(wavelet.get(i, j)[0]) > threshold) {
                        filtered.put(i, j, 0);
                        zeroCount++;
                    }
                }
            }

            System.out.println("小波高通滤波 - 保留高频能量: " + String.format("%.2f", energyRatio*100) + "%, 置零系数: " + zeroCount);
            return inverseTransform(filtered);
        }

        // 多级小波变换
        public static Mat multiLevelTransform(Mat src, int levels) {
            // 确保输入是单通道灰度图
            Mat gray = convertToGray(src);
            Mat current = new Mat();
            gray.convertTo(current, CvType.CV_32F);

            // 记录每一级的尺寸
            List<Size> levelSizes = new ArrayList<>();
            levelSizes.add(new Size(current.cols(), current.rows()));

            // 多级分解
            for (int level = 0; level < levels; level++) {
                int rows = current.rows();
                int cols = current.cols();

                // 检查是否可以继续分解
                if (rows < 4 || cols < 4) {
                    System.out.println("第 " + (level+1) + " 级分解停止，图像尺寸太小: " + cols + "x" + rows);
                    break;
                }

                // 执行一级小波变换
                current = haarWaveletTransform(current);

                // 记录新的尺寸
                levelSizes.add(new Size(cols, rows));

                System.out.println("第 " + (level+1) + " 级小波分解完成，尺寸: " + cols + "x" + rows);
            }

            // 归一化显示
            Core.normalize(current, current, 0, 255, Core.NORM_MINMAX);
            Mat result = new Mat();
            current.convertTo(result, CvType.CV_8U);

            return result;
        }

        // 小波变换方法
        private static Mat haarWaveletTransform(Mat input) {
            int rows = input.rows();
            int cols = input.cols();
            Mat output = input.clone();

            // 确保是单通道
            if (output.channels() != 1) {
                Mat temp = new Mat();
                Imgproc.cvtColor(output, temp, Imgproc.COLOR_BGR2GRAY);
                output = temp;
                output.convertTo(output, CvType.CV_32F);
            }

            // 临时矩阵用于存储中间结果
            Mat tempRow = new Mat(rows, cols, CvType.CV_32F);

            // 行变换
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols / 2; j++) {
                    double a = output.get(i, 2 * j)[0];
                    double b = output.get(i, 2 * j + 1)[0];

                    double avg = (a + b) / Math.sqrt(2);
                    double diff = (a - b) / Math.sqrt(2);

                    tempRow.put(i, j, avg);
                    tempRow.put(i, j + cols / 2, diff);
                }
            }

            // 列变换
            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows / 2; i++) {
                    double a = tempRow.get(2 * i, j)[0];
                    double b = tempRow.get(2 * i + 1, j)[0];

                    double avg = (a + b) / Math.sqrt(2);
                    double diff = (a - b) / Math.sqrt(2);

                    output.put(i, j, avg);
                    output.put(i + rows / 2, j, diff);
                }
            }

            return output;
        }

        private static Mat inverseHaarWavelet(Mat wavelet) {
            int rows = wavelet.rows();
            int cols = wavelet.cols();
            Mat output = wavelet.clone();

            // 确保是单通道
            if (output.channels() != 1) {
                Mat temp = new Mat();
                Imgproc.cvtColor(output, temp, Imgproc.COLOR_BGR2GRAY);
                output = temp;
                output.convertTo(output, CvType.CV_32F);
            }

            // 临时矩阵用于存储中间结果
            Mat tempCol = new Mat(rows, cols, CvType.CV_32F);

            // 逆列变换
            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows / 2; i++) {
                    double avg = output.get(i, j)[0];
                    double diff = output.get(i + rows / 2, j)[0];

                    double a = (avg + diff) / Math.sqrt(2);
                    double b = (avg - diff) / Math.sqrt(2);

                    tempCol.put(2 * i, j, a);
                    tempCol.put(2 * i + 1, j, b);
                }
            }

            // 逆行变换
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols / 2; j++) {
                    double avg = tempCol.get(i, j)[0];
                    double diff = tempCol.get(i, j + cols / 2)[0];

                    double a = (avg + diff) / Math.sqrt(2);
                    double b = (avg - diff) / Math.sqrt(2);

                    output.put(i, 2 * j, a);
                    output.put(i, 2 * j + 1, b);
                }
            }

            return output;
        }
    }

    // 系数信息类
    private static class Coefficient {
        int row, col;
        double absValue, originalValue;
        Coefficient(int row, int col, double absValue, double originalValue) {
            this.row = row;
            this.col = col;
            this.absValue = absValue;
            this.originalValue = originalValue;
        }
    }

    // 工具方法
    private static Mat convertToGray(Mat src) {
        Mat gray = new Mat();
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            gray = src.clone();
        }
        return gray;
    }
    private static Mat optimizeSize(Mat src) {
        int m = Core.getOptimalDFTSize(src.rows());
        int n = Core.getOptimalDFTSize(src.cols());
        Mat padded = new Mat();
        Core.copyMakeBorder(src, padded, 0, m - src.rows(), 0, n - src.cols(),
                Core.BORDER_CONSTANT, Scalar.all(0));
        return padded;
    }

    // 主处理函数
    public static void main(String[] args) {
        System.out.println("开始图像变换处理...");
        System.out.println("输入图片: " + INPUT_IMAGE_PATH);
        System.out.println("输出目录: " + OUTPUT_DIRECTORY);

        // 检查输出目录是否存在，如果不存在则创建
        java.io.File outputDir = new java.io.File(OUTPUT_DIRECTORY);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            System.out.println("创建输出目录: " + OUTPUT_DIRECTORY);
        }

        // 读取输入图片
        Mat originalImage = Imgcodecs.imread(INPUT_IMAGE_PATH);
        if (originalImage.empty()) {
            System.out.println("错误: 无法读取输入图片: " + INPUT_IMAGE_PATH);
            return;
        }
        System.out.println("图片读取成功，尺寸: " + originalImage.cols() + "x" + originalImage.rows());

        try {
            // 1. FFT处理（包含滤波）
            processFFT(originalImage);
            // 2. DCT处理（包含滤波）
            processDCT(originalImage);
            // 3. WHT处理
            processWHT(originalImage);
            // 4. 小波处理（包含滤波）
            processWavelet(originalImage);
            // 5. 保存原图
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "original.jpg", originalImage);

            System.out.println("\n所有处理完成！结果保存在: " + OUTPUT_DIRECTORY);
            System.out.println("总计生成约30个文件，包含13个滤波图像");

        } catch (Exception e) {
            System.out.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processFFT(Mat image) {
        System.out.println("\n=== FFT变换处理 ===");
        Mat fftComplex = FFTTransformer.transform(image);
        Mat fftMagnitude = FFTTransformer.getMagnitudeSpectrum(fftComplex);
        Mat fftReconstructed = FFTTransformer.inverseTransform(fftComplex);
        Mat fftCompressed10 = FFTTransformer.compress(image, 0.1);
        Mat fftCompressed5 = FFTTransformer.compress(image, 0.05);

        //FFT滤波处理
        Mat fftLowPass30 = FFTTransformer.lowPassFilter(image, 0.3);
        Mat fftLowPass50 = FFTTransformer.lowPassFilter(image, 0.5);
        Mat fftHighPass10 = FFTTransformer.highPassFilter(image, 0.1);
        Mat fftHighPass20 = FFTTransformer.highPassFilter(image, 0.2);

        // 保存所有图像
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_magnitude.jpg", fftMagnitude);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_reconstructed.jpg", fftReconstructed);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_compressed_10.jpg", fftCompressed10);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_compressed_5.jpg", fftCompressed5);

        // 保存滤波图像
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_lowpass_30.jpg", fftLowPass30);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_lowpass_50.jpg", fftLowPass50);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_highpass_10.jpg", fftHighPass10);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "fft_highpass_20.jpg", fftHighPass20);

        System.out.println("FFT处理完成 ");
    }

    private static void processDCT(Mat image) {
        System.out.println("\n=== DCT变换处理 ===");
        Mat dct = DCTTransformer.transform(image);
        Mat dctVisualized = DCTTransformer.visualize(dct);
        Mat dctReconstructed = DCTTransformer.inverseTransform(dct);
        Mat dctRegion50 = DCTTransformer.compressByRegion(image, 0.5);
        Mat dctRegion25 = DCTTransformer.compressByRegion(image, 0.25);
        Mat dctThreshold10 = DCTTransformer.compressByThreshold(image, 0.1);
        Mat dctThreshold5 = DCTTransformer.compressByThreshold(image, 0.05);

        // 新增：DCT滤波处理
        Mat dctLowPass30 = DCTTransformer.lowPassFilter(image, 0.3);
        Mat dctLowPass50 = DCTTransformer.lowPassFilter(image, 0.5);
        Mat dctHighPass30 = DCTTransformer.highPassFilter(image, 0.3);
        Mat dctHighPass50 = DCTTransformer.highPassFilter(image, 0.5);

        // 保存所有图像
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_transform.jpg", dctVisualized);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_reconstructed.jpg", dctReconstructed);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_region_50.jpg", dctRegion50);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_region_25.jpg", dctRegion25);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_threshold_10.jpg", dctThreshold10);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_threshold_5.jpg", dctThreshold5);

        // 保存滤波图像
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_lowpass_30.jpg", dctLowPass30);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_lowpass_50.jpg", dctLowPass50);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_highpass_30.jpg", dctHighPass30);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "dct_highpass_50.jpg", dctHighPass50);

        System.out.println("DCT处理完成");
    }

    private static void processWHT(Mat image) {
        System.out.println("\n=== WHT变换处理 ===");
        Mat wht = WHTTransformer.transform(image);
        Mat whtVisualized = WHTTransformer.visualize(wht);
        Mat whtReconstructed = WHTTransformer.inverseTransform(wht);
        Mat whtThreshold10 = WHTTransformer.compressByThreshold(image, 0.1);
        Mat whtThreshold5 = WHTTransformer.compressByThreshold(image, 0.05);
        Mat whtTop100 = WHTTransformer.compressByTopCoefficients(image, 100);
        Mat whtTop50 = WHTTransformer.compressByTopCoefficients(image, 50);
        Mat whtRegion50 = WHTTransformer.compressByRegion(image, 0.5);
        Mat whtRegion25 = WHTTransformer.compressByRegion(image, 0.25);

        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wht_transform.jpg", whtVisualized);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wht_reconstructed.jpg", whtReconstructed);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wht_threshold_10.jpg", whtThreshold10);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wht_top_100.jpg", whtTop100);
        Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wht_region_50.jpg", whtRegion50);

        System.out.println("WHT处理完成");
    }

    private static void processWavelet(Mat image) {
        System.out.println("\n=== 小波变换处理 ===");
        try {
            Mat wavelet = WaveletTransformer.transform(image);
            Mat waveletVisualized = WaveletTransformer.visualize(wavelet);
            Mat waveletReconstructed = WaveletTransformer.inverseTransform(wavelet);
            Mat waveletThreshold10 = WaveletTransformer.compressByThreshold(image, 0.1);
            Mat waveletThreshold5 = WaveletTransformer.compressByThreshold(image, 0.05);
            Mat multiWavelet3 = WaveletTransformer.multiLevelTransform(image, 3);
            Mat multiWavelet2 = WaveletTransformer.multiLevelTransform(image, 2);

            // 新增：小波滤波处理
            Mat waveletLowPass80 = WaveletTransformer.lowPassFilter(image, 0.8);
            Mat waveletLowPass90 = WaveletTransformer.lowPassFilter(image, 0.9);
            Mat waveletHighPass20 = WaveletTransformer.highPassFilter(image, 0.2);
            Mat waveletHighPass30 = WaveletTransformer.highPassFilter(image, 0.3);

            // 保存所有图像
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_transform.jpg", waveletVisualized);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_reconstructed.jpg", waveletReconstructed);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_threshold_10.jpg", waveletThreshold10);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_threshold_5.jpg", waveletThreshold5);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "multilevel_wavelet_3.jpg", multiWavelet3);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "multilevel_wavelet_2.jpg", multiWavelet2);

            // 保存滤波图像
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_lowpass_80.jpg", waveletLowPass80);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_lowpass_90.jpg", waveletLowPass90);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_highpass_20.jpg", waveletHighPass20);
            Imgcodecs.imwrite(OUTPUT_DIRECTORY + "wavelet_highpass_30.jpg", waveletHighPass30);

            System.out.println("小波处理完成");
        } catch (Exception e) {
            System.out.println("小波处理出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}