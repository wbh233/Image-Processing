package org.example;

import java.io.IOException;

// 主类
public class ImageTransforms {
    public static void main(String[] args) {
        // 定义输出文件夹路径
        String outputDir = "src/output/";

        try {
            // 创建输出目录
            java.io.File outputFolder = new java.io.File(outputDir);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
                System.out.println("创建输出目录: " + outputDir);
            }
            // 读取图像
            double[][] originalImage = ImageProcessor.loadImage("src/image/image.jpg");
            // 调整大小为2的幂次
            System.out.println("调整图像大小");
            double[][] image = ImageProcessor.resizeToPowerOfTwo(originalImage);
            System.out.println("图像大小调整为: " + image.length + " x " + image[0].length);
            //FFT 变换和频谱分析
            System.out.println("执行FFT变换和频谱分析");
            Complex[][] fftResult = FFTTransformer.fft2d(image);
            // 计算并保存幅度谱
            double[][] magnitude = FFTTransformer.computeMagnitudeSpectrum(fftResult);
            ImageProcessor.saveImage(magnitude, outputDir + "fft_magnitude_spectrum.jpg");
            // 计算并保存相位谱
            double[][] phase = FFTTransformer.computePhaseSpectrum(fftResult);
            ImageProcessor.saveImage(phase, outputDir + "fft_phase_spectrum.jpg");
            // 使用直接逆变换重构图像
            double[][] reconstructedByInverse1 = FFTTransformer.reconstructByInverseTransform(fftResult);
            ImageProcessor.saveImage(reconstructedByInverse1, outputDir + "fft_inverse_reconstructed.jpg");
            // 测试从频谱重建图像
            double[][] reconstructedFromSpectrum = FFTTransformer.reconstructFromSpectrum(magnitude, phase);
            ImageProcessor.saveImage(reconstructedFromSpectrum, outputDir + "fft_reconstructed.jpg");
            // FFT高通滤波
            Complex[][] fftHighPass = MatrixUtils.copyComplexMatrix(fftResult);
            FFTTransformer.highPassFilter(fftHighPass, 50);
            double[][] fftHighResult = FFTTransformer.ifft2d(fftHighPass);
            ImageProcessor.saveImage(fftHighResult, outputDir + "fft_highpass.jpg");
            // FFT低通滤波
            Complex[][] fftLowPass = MatrixUtils.copyComplexMatrix(fftResult);
            FFTTransformer.lowPassFilter(fftLowPass, 30);
            double[][] fftLowResult = FFTTransformer.ifft2d(fftLowPass);
            ImageProcessor.saveImage(fftLowResult, outputDir + "fft_lowpass.jpg");
            //DCT 变换
            System.out.println("执行DCT变换");
            double[][] dctResult = DCTTransformer.dct2d(image);
            // 计算并保存DCT幅度谱
            double[][] dctMagnitude = DCTTransformer.computeMagnitudeSpectrum(dctResult);
            ImageProcessor.saveImage(dctResult, outputDir + "dct_magnitude_spectrum.jpg");
            // 计算并保存DCT相位谱
            double[][] dctPhase = DCTTransformer.computePhaseSpectrum(dctResult);
            ImageProcessor.saveImage(dctPhase, outputDir + "dct_phase_spectrum.jpg");
            // 测试从DCT频谱重建图像
            double[][] reconstructedFromDCT = DCTTransformer.reconstructFromSpectrum(dctMagnitude, dctPhase);
            ImageProcessor.saveImage(reconstructedFromDCT, outputDir + "dct_reconstructed.jpg");
            // 逆变换重构
            double[][] reconstructedByInverse = DCTTransformer.reconstructByInverseTransform(dctResult);
            ImageProcessor.saveImage(reconstructedByInverse, outputDir + "dct_inverse_reconstructed.jpg");
            // DCT高通滤波
            double[][] dctHighPass = MatrixUtils.copyMatrix(dctResult);
            DCTTransformer.highPassFilter(dctHighPass, 0.3);
            double[][] dctHighResult = DCTTransformer.idct2d(dctHighPass);
            ImageProcessor.saveImage(dctHighResult, outputDir + "dct_highpass.jpg");
            // DCT低通滤波
            double[][] dctLowPass = MatrixUtils.copyMatrix(dctResult);
            DCTTransformer.lowPassFilter(dctLowPass, 0.3);
            double[][] dctLowResult = DCTTransformer.idct2d(dctLowPass);
            ImageProcessor.saveImage(dctLowResult, outputDir + "dct_lowpass.jpg");

            // WHT 变换
            System.out.println("执行WHT变换...");
            double[][] whtResult = WHTTransformer.wht2d(image);
            // WHT变换结果
            ImageProcessor.saveImage(whtResult, outputDir + "wht_transform.jpg");
            // 使用直接逆变换重构图像
            double[][] whtReconstructedByInverse = WHTTransformer.reconstructByInverseTransform(whtResult);
            ImageProcessor.saveImage(whtReconstructedByInverse, outputDir + "wht_inverse_reconstructed.jpg");
            //小波变换
            System.out.println("执行小波变换...");
            DWTTransformer.WaveletResult waveletResult = DWTTransformer.waveletDecompose(image);
            //保存子带
            ImageProcessor.saveImage(waveletResult.approx, outputDir + "wavelet_approx.jpg");
            ImageProcessor.saveImage(waveletResult.horizontal, outputDir + "wavelet_horizontal.jpg");
            ImageProcessor.saveImage(waveletResult.vertical, outputDir + "wavelet_vertical.jpg");
            ImageProcessor.saveImage(waveletResult.diagonal, outputDir + "wavelet_diagonal.jpg");
            //小波重构
            System.out.println("执行小波重构...");
            double[][] waveletReconstructed = DWTTransformer.waveletReconstruct(waveletResult);
            ImageProcessor.saveImage(waveletReconstructed, outputDir + "wavelet_reconstructed.jpg");
            //保存原始图像用于对比
            ImageProcessor.saveImage(image, outputDir + "original_resized.jpg");
        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}