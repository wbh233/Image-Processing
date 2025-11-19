package org.example;

// DCT
class DCTTransformer {
    // 1D DCT
    public static double[] dct1d(double[] x) {
        int N = x.length;          // 输入信号长度
        double[] y = new double[N]; // 输出DCT系数
        // 遍历每个频率分量k
        for (int k = 0; k < N; k++) {
            double sum = 0;
            // 遍历每个时域样本n，计算余弦加权和
            for (int n = 0; n < N; n++) {
                // DCT核心公式：cos(πk(2n+1)/(2N))
                sum += x[n] * Math.cos(Math.PI * k * (2 * n + 1) / (2 * N));
            }
            // 归一化系数：DC分量(k=0)与其他分量不同
            double ck = (k == 0) ? Math.sqrt(1.0 / N) : Math.sqrt(2.0 / N);
            y[k] = ck * sum;  // 得到第k个DCT系数
        }
        return y;
    }
    // 1D 逆DCT
    public static double[] idct1d(double[] x) {
        int N = x.length;          // 输入DCT系数长度
        double[] y = new double[N]; // 输出重建信号
        // 遍历每个时域位置n
        for (int n = 0; n < N; n++) {
            double sum = 0;
            // 遍历每个频率分量k，重建时域信号
            for (int k = 0; k < N; k++) {
                // 使用相同的归一化系数
                double ck = (k == 0) ? Math.sqrt(1.0 / N) : Math.sqrt(2.0 / N);
                // 逆DCT核心公式：与正变换相同的余弦基函数
                sum += ck * x[k] * Math.cos(Math.PI * k * (2 * n + 1) / (2 * N));
            }
            y[n] = sum;  // 得到重建后的时域样本
        }
        return y;
    }
    // 2D DCT
    public static double[][] dct2d(double[][] input) {
        int rows = input.length;    // 图像行数（高度）
        int cols = input[0].length; // 图像列数（宽度）
        double[][] result = new double[rows][cols]; // 输出2D DCT系数
        double[][] temp = new double[rows][cols];   // 临时存储矩阵
        // 第一步：先对每行做一维DCT（行变换）
        for (int i = 0; i < rows; i++) {
            temp[i] = dct1d(input[i]);  // 对第i行进行DCT
        }
        // 第二步：转置矩阵，便于对列进行操作
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = temp[i][j];  // 行列互换
            }
        }
        // 第三步：对转置后的每行做DCT
        for (int j = 0; j < cols; j++) {
            transposed[j] = dct1d(transposed[j]);  // 对第j列进行DCT
        }
        // 第四步：转置回来，恢复原始方向
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = transposed[j][i];  // 行列再次互换，恢复原顺序
            }
        }
        return result;
    }
    // 2D 逆DCT
    public static double[][] idct2d(double[][] input) {
        int rows = input.length;    // DCT系数矩阵行数
        int cols = input[0].length; // DCT系数矩阵列数
        double[][] result = new double[rows][cols]; // 输出重建图像
        double[][] temp = new double[rows][cols];   // 临时存储矩阵
        // 第一步：转置矩阵，便于对列进行操作
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = input[i][j];  // 行列互换
            }
        }
        // 第二步：对转置后的每行做逆DCT（相当于对原DCT系数的列做逆DCT）
        for (int j = 0; j < cols; j++) {
            transposed[j] = idct1d(transposed[j]);  // 对第j列进行逆DCT
        }
        // 第三步：转置回来
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                temp[i][j] = transposed[j][i];  // 行列再次互换，恢复原顺序
            }
        }
        // 第四步：对每行做逆DCT
        for (int i = 0; i < rows; i++) {
            result[i] = idct1d(temp[i]);  // 对第i行进行逆DCT
        }
        return result;
    }
    // DCT高通滤波：滤除低频成分，保留高频成分（边缘、细节）,左上角是低频，向右下角频率逐渐增加
    public static void highPassFilter(double[][] dct, double cutoffRatio) {
        // 根据比例计算截止频率的索引位置
        int cutoff = (int) (Math.min(dct.length, dct[0].length) * cutoffRatio);
        // 遍历所有DCT系数
        for (int i = 0; i < dct.length; i++) {
            for (int j = 0; j < dct[0].length; j++) {
                // 如果位于左上角低频区域（i<cutoff且j<cutoff），则置零
                if (i < cutoff && j < cutoff) {
                    dct[i][j] = 0;  // 滤除低频成分
                }
            }
        }
    }
    // DCT低通滤波：滤除高频成分，保留低频成分（平滑区域）
    public static void lowPassFilter(double[][] dct, double cutoffRatio) {
        // 根据比例计算截止频率的索引位置
        int cutoff = (int) (Math.min(dct.length, dct[0].length) * cutoffRatio);

        // 遍历所有DCT系数
        for (int i = 0; i < dct.length; i++) {
            for (int j = 0; j < dct[0].length; j++) {
                // 如果位于右下角高频区域（i>=cutoff或j>=cutoff），则置零
                if (i >= cutoff || j >= cutoff) {
                    dct[i][j] = 0;  // 滤除高频成分
                }
            }
        }
    }
    // 计算DCT幅度谱（由于DCT只有实部，幅度就是绝对值）
    public static double[][] computeMagnitudeSpectrum(double[][] dctResult) {
        int rows = dctResult.length;
        int cols = dctResult[0].length;
        double[][] magnitude = new double[rows][cols];
        // 遍历所有DCT系数，计算幅度（绝对值）
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                magnitude[i][j] = Math.abs(dctResult[i][j]);  // 幅度 = |DCT系数|,取绝对值
            }
        }
        return magnitude;
    }
    // 计算DCT相位谱（DCT只有实部，相位只有0或π）
    public static double[][] computePhaseSpectrum(double[][] dctResult) {
        int rows = dctResult.length;
        int cols = dctResult[0].length;
        double[][] phase = new double[rows][cols];
        // 遍历所有DCT系数，根据正负号确定相位
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // DCT系数为正值时相位为0，负值时相位为π
                if (dctResult[i][j] >= 0) {
                    phase[i][j] = 0.0;  // 对应相位0（归一化到0.0）
                } else {
                    phase[i][j] = 1.0;  // 对应相位π（归一化到1.0）
                }
            }
        }
        return phase;
    }
    // 从幅度谱和相位谱重建图像
    public static double[][] reconstructFromSpectrum(double[][] magnitude, double[][] phase) {
        int rows = magnitude.length;
        int cols = magnitude[0].length;
        double[][] dctResult = new double[rows][cols];
        // 根据幅度和相位信息重建DCT系数
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 根据相位信息恢复符号：相位>0.5表示负数，否则为正数
                if (phase[i][j] > 0.5) {  // 相位接近π，系数为负
                    dctResult[i][j] = -magnitude[i][j];
                } else {  // 相位接近0，系数为正
                    dctResult[i][j] = magnitude[i][j];
                }
            }
        }
        // 执行逆DCT，从频域转换回空域
        return idct2d(dctResult);
    }
    // 使用直接逆变换重构图像
    public static double[][] reconstructByInverseTransform(double[][] dctCoefficients) {
        // 直接使用逆DCT变换回到空间域
        return idct2d(dctCoefficients);
    }
}