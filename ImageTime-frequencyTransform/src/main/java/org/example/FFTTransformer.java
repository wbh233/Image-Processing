package org.example;

// FFT实现
class FFTTransformer {
    // 1D FFT
    public static Complex[] fft1d(Complex[] x) {
        int n = x.length;
        if (n <= 1) return x;//如果数组长度为1就返回
        // 奇偶分离，将序列分成偶索引和奇索引
        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[i * 2];//偶数索引元素
            odd[i] = x[i * 2 + 1];//奇数索引元素
        }
        // 递归，奇偶分别fft
        even = fft1d(even);
        odd = fft1d(odd);
        // 合并，将两部分的结果合并为完整的FFT结果
        Complex[] result = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            // 计算旋转因子，e^(-i*2πk/n)
            Complex t = Complex.fromPolar(1.0, -2 * Math.PI * k / n).multiply(odd[k]);
            // 蝶形运算，合并偶数和奇数部分的结果
            result[k] = even[k].add(t);// 前半部分结果
            result[k + n / 2] = even[k].subtract(t);// 后半部分结果
        }
        return result;
    }
    // 1D 逆FFT
    public static Complex[] ifft1d(Complex[] x) {
        int n = x.length;
        // 取共轭，对输入序列的每个元素取共轭复数
        for (int i = 0; i < n; i++) {
            x[i] = x[i].conjugate();
        }
        // 正向FFT，对共轭后的序列进行正向FFT
        Complex[] result = fft1d(x);
        // 再取共轭并缩放，再次取共轭并除以长度n完成逆变换，逆变换需要除以n
        for (int i = 0; i < n; i++) {
            result[i] = result[i].conjugate(); // 取共轭
            result[i].real /= n;               // 缩放实部
            result[i].imag /= n;               // 缩放虚部
        }
        return result;
    }
    // 2D FFT
    public static Complex[][] fft2d(double[][] input) {
        int rows = input.length;//行数，图像高度
        int cols = input[0].length;//列数，图像宽度
        Complex[][] complexMat = new Complex[rows][cols];
        // 将实数矩阵转换为复数矩阵（虚部为0）
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                complexMat[i][j] = new Complex(input[i][j], 0);
            }
        }
        // 对每行做FFT，逐行进行一维FFT
        for (int i = 0; i < rows; i++) {
            complexMat[i] = fft1d(complexMat[i]);
        }
        // 转置，将矩阵转置以便对列进行操作
        Complex[][] transposed = new Complex[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = complexMat[i][j];
            }
        }
        // 对每列做FFT，对转置后的行（即原矩阵的列）进行FFT
        for (int j = 0; j < cols; j++) {
            transposed[j] = fft1d(transposed[j]);
        }
        // 转置回来，将矩阵转置回原始方向
        Complex[][] result = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = transposed[j][i];
            }
        }
        return result;
    }
    // 2D 逆FFT
    public static double[][] ifft2d(Complex[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        // 复制输入矩阵以避免修改原始数据
        Complex[][] complexMat = MatrixUtils.copyComplexMatrix(input);
        // 对每行做IFFT，逐行进行一维逆FFT
        for (int i = 0; i < rows; i++) {
            complexMat[i] = ifft1d(complexMat[i]);
        }
        // 转置，将矩阵转置以便对列进行操作
        Complex[][] transposed = new Complex[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = complexMat[i][j];
            }
        }
        // 对每列做IFFT，对转置后的行（即原矩阵的列）进行逆FFT
        for (int j = 0; j < cols; j++) {
            transposed[j] = ifft1d(transposed[j]);
        }
        // 转置回来并保存实部，转置回原始方向并提取实部作为结果
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = transposed[j][i].real; // 只取实部（虚部应为0）
            }
        }
        return result;
    }
    // 高通滤波，滤除低频成分，保留高频成分（边缘、细节）
    public static void highPassFilter(Complex[][] freq, double cutoff) {
        int centerX = freq[0].length / 2; // 频谱中心点X坐标
        int centerY = freq.length / 2;    // 频谱中心点Y坐标
        // 遍历频谱中的所有频率分量
        for (int i = 0; i < freq.length; i++) {
            for (int j = 0; j < freq[0].length; j++) {
                // 计算当前频率分量到频谱中心的距离
                double dist = Math.sqrt(Math.pow(i - centerY, 2) + Math.pow(j - centerX, 2));
                // 如果距离小于截止频率（低频区域），则将该分量置零
                if (dist < cutoff) {
                    freq[i][j].real = 0; // 实部置零
                    freq[i][j].imag = 0; // 虚部置零
                }
            }
        }
    }
    // 低通滤波，滤除高频成分，保留低频成分（平滑区域）
    public static void lowPassFilter(Complex[][] freq, double cutoff) {
        int centerX = freq[0].length / 2; // 频谱中心点X坐标
        int centerY = freq.length / 2;    // 频谱中心点Y坐标
        // 遍历频谱中的所有频率分量
        for (int i = 0; i < freq.length; i++) {
            for (int j = 0; j < freq[0].length; j++) {
                // 计算当前频率分量到频谱中心的距离
                double dist = Math.sqrt(Math.pow(i - centerY, 2) + Math.pow(j - centerX, 2));
                // 如果距离大于截止频率（高频区域），则将该分量置零
                if (dist > cutoff) {
                    freq[i][j].real = 0; // 实部置零
                    freq[i][j].imag = 0; // 虚部置零
                }
            }
        }
    }
    // 计算幅度谱，计算复数矩阵中各元素的幅度值
    public static double[][] computeMagnitudeSpectrum(Complex[][] fftResult) {
        int rows = fftResult.length;
        int cols = fftResult[0].length;
        double[][] magnitude = new double[rows][cols];
        // 遍历所有频率分量，计算每个复数的幅度
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                magnitude[i][j] = fftResult[i][j].magnitude(); // 计算幅度，sqrt(real² + imag²)
            }
        }
        return magnitude;
    }
    // 计算相位谱，计算复数矩阵中各元素的相位角
    public static double[][] computePhaseSpectrum(Complex[][] fftResult) {
        int rows = fftResult.length;
        int cols = fftResult[0].length;
        double[][] phase = new double[rows][cols];
        // 遍历所有频率分量，计算每个复数的相位
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 计算相位角，范围 [-π, π]
                double phaseAngle = Math.atan2(fftResult[i][j].imag, fftResult[i][j].real);
                // 归一化到 [0, 1] 范围以便显示
                phase[i][j] = (phaseAngle + Math.PI) / (2 * Math.PI);
            }
        }
        return phase;
    }
    // 使用直接逆变换重构图像，通过逆FFT从频域恢复到空域
    public static double[][] reconstructByInverseTransform(Complex[][] fftCoefficients) {
        // 直接使用逆FFT变换回到空间域，因为逆FFT可以直接将频率域复数系数变到空间域图像
        return ifft2d(fftCoefficients);
    }
     //从幅度谱和相位谱重建图像，通过幅度和相位信息重建原始图像
    public static double[][] reconstructFromSpectrum(double[][] magnitude, double[][] phase) {
        int rows = magnitude.length;
        int cols = magnitude[0].length;

        Complex[][] complexResult = new Complex[rows][cols];
        // 从幅度和相位信息重建复数矩阵
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 将归一化的相位转换回原始范围 [-π, π]
                double phaseAngle = phase[i][j] * 2 * Math.PI - Math.PI;
                // 从极坐标形式重建复数，real = magnitude * cos(phase), imag = magnitude * sin(phase)
                double real = magnitude[i][j] * Math.cos(phaseAngle);
                double imag = magnitude[i][j] * Math.sin(phaseAngle);
                complexResult[i][j] = new Complex(real, imag);
            }
        }
        // 执行逆FFT，从频域转换回空域
        return ifft2d(complexResult);
    }
}