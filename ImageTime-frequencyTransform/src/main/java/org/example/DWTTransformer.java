package org.example;

// 小波变换类，实现二维离散小波变换
class DWTTransformer {

    // 小波分解结果结构，用于存储四个子带系数
    public static class WaveletResult {
        public double[][] approx;     // 近似系数（低频子带LL），包含图像的主要能量
        public double[][] horizontal; // 水平细节系数（LH子带），包含水平方向的高频信息
        public double[][] vertical;   // 垂直细节系数（HL子带），包含垂直方向的高频信息
        public double[][] diagonal;   // 对角细节系数（HH子带），包含对角线方向的高频信息

        // 初始化四个子带系数矩阵
        public WaveletResult(double[][] approx, double[][] horizontal,
                             double[][] vertical, double[][] diagonal) {
            this.approx = approx;
            this.horizontal = horizontal;
            this.vertical = vertical;
            this.diagonal = diagonal;
        }
    }

    // Db8小波低通滤波器系数，用于提取近似（低频）信息
    // Db8小波具有8个系数，提供较好的频率局部化特性
    private static final double[] DB8_LOW_PASS = {
            0.23037781330889, 0.71484657055291, 0.63088076792986, -0.02798376941686,
            -0.18703481171909, 0.03084138183556, 0.03288301166689, -0.01059740178507
    };

    // Db8小波高通滤波器系数，用于提取细节（高频）信息
    // 由低通滤波器通过正交镜像关系得到
    private static final double[] DB8_HIGH_PASS = {
            -0.01059740178507, -0.03288301166689, 0.03084138183556, 0.18703481171909,
            -0.02798376941686, -0.63088076792986, 0.71484657055291, -0.23037781330889
    };
    // 一维小波分解方法，将信号分解为近似系数和细节系数
    // 输入：signal - 一维信号数组
    // 输出：二维数组，第一行是近似系数，第二行是细节系数
    private static double[][] wavelet1dDecompose(double[] signal) {
        int N = signal.length;           // 获取信号长度
        int halfN = N / 2;               // 下采样后长度减半
        double[][] result = new double[2][halfN]; // 创建结果数组

        // 遍历每个输出位置，进行卷积和下采样
        for (int i = 0; i < halfN; i++) {
            double approxSum = 0;  // 近似系数累加器
            double detailSum = 0;  // 细节系数累加器

            // 与滤波器系数进行卷积运算
            for (int j = 0; j < DB8_LOW_PASS.length; j++) {
                int index = (2 * i + j) % N;  // 循环索引，处理边界效应
                approxSum += signal[index] * DB8_LOW_PASS[j];   // 低通滤波
                detailSum += signal[index] * DB8_HIGH_PASS[j];  // 高通滤波
            }

            result[0][i] = approxSum;  // 存储近似系数
            result[1][i] = detailSum;  // 存储细节系数
        }

        return result;  // 返回分解结果
    }

    // 一维小波重构方法，从系数重建原始信号
    // 输入：coefficients - 小波系数，包含近似和细节系数
    // 输出：重构后的一维信号
    private static double[] wavelet1dReconstruct(double[][] coefficients) {
        int halfN = coefficients[0].length;  // 获取系数长度
        int N = halfN * 2;                   // 重构后信号长度
        double[] result = new double[N];     // 创建重构结果数组

        // 遍历每个位置，进行上采样和卷积
        for (int i = 0; i < halfN; i++) {
            // 与重构滤波器进行卷积
            for (int j = 0; j < DB8_LOW_PASS.length; j++) {
                int index = (i - j + halfN) % halfN;  // 循环索引处理边界
                // 偶数位置的重构
                result[2 * i] += coefficients[0][index] * DB8_LOW_PASS[j] +
                        coefficients[1][index] * DB8_HIGH_PASS[j];
                // 奇数位置的重构，使用反转的滤波器
                result[2 * i + 1] += coefficients[0][index] * DB8_LOW_PASS[DB8_LOW_PASS.length - 1 - j] +
                        coefficients[1][index] * DB8_HIGH_PASS[DB8_HIGH_PASS.length - 1 - j];
            }
        }

        return result;  // 返回重构信号
    }

    // 二维小波分解方法，将图像分解为四个子带
    // 输入：image - 二维图像矩阵
    // 输出：WaveletResult对象，包含四个子带系数
    public static WaveletResult waveletDecompose(double[][] image) {
        int rows = image.length;        // 图像行数
        int cols = image[0].length;     // 图像列数
        int halfRows = rows / 2;        // 子带行数（下采样）
        int halfCols = cols / 2;        // 子带列数（下采样）

        // 创建临时矩阵存储中间结果
        double[][] temp = new double[rows][cols];
        // 创建四个子带矩阵
        double[][] approx = new double[halfRows][halfCols];     // 近似子带（低频）
        double[][] horizontal = new double[halfRows][halfCols]; // 水平细节子带
        double[][] vertical = new double[halfRows][halfCols];   // 垂直细节子带
        double[][] diagonal = new double[halfRows][halfCols];   // 对角细节子带

        // 对每一行进行一维小波分解
        for (int i = 0; i < rows; i++) {
            double[][] rowCoeffs = wavelet1dDecompose(image[i]);  // 对当前行分解
            // 将分解结果存入临时矩阵
            for (int j = 0; j < halfCols; j++) {
                temp[i][j] = rowCoeffs[0][j];           // 左半部分存储近似系数
                temp[i][j + halfCols] = rowCoeffs[1][j]; // 右半部分存储细节系数
            }
        }
        // 对每一列进行一维小波分解
        for (int j = 0; j < cols; j++) {
            // 提取当前列数据
            double[] column = new double[rows];
            for (int i = 0; i < rows; i++) {
                column[i] = temp[i][j];
            }
            // 对当前列进行小波分解
            double[][] colCoeffs = wavelet1dDecompose(column);

            // 根据列位置分配到不同的子带
            for (int i = 0; i < halfRows; i++) {
                if (j < halfCols) {
                    // 左半列：近似系数和垂直细节
                    approx[i][j] = colCoeffs[0][i];     // 左上：近似子带（LL）
                    vertical[i][j] = colCoeffs[1][i];   // 左下：垂直细节子带（HL）
                } else {
                    // 右半列：水平细节和对角细节
                    horizontal[i][j - halfCols] = colCoeffs[0][i];  // 右上：水平细节子带（LH）
                    diagonal[i][j - halfCols] = colCoeffs[1][i];    // 右下：对角细节子带（HH）
                }
            }
        }
        // 返回包含四个子带的分解结果
        return new WaveletResult(approx, horizontal, vertical, diagonal);
    }

    // 二维小波重构方法，从四个子带重建原始图像
    // 输入：result - 包含四个子带系数的WaveletResult对象
    // 输出：重构后的二维图像矩阵
    public static double[][] waveletReconstruct(WaveletResult result) {
        int halfRows = result.approx.length;     // 子带行数
        int halfCols = result.approx[0].length;  // 子带列数
        int rows = halfRows * 2;                 // 重构图像行数
        int cols = halfCols * 2;                 // 重构图像列数

        // 创建临时矩阵存储列重构结果
        double[][] temp = new double[rows][cols];

        // 第一步：列重构 - 对每一列进行一维小波重构
        for (int j = 0; j < cols; j++) {
            double[] approxCol = new double[halfRows];  // 当前列的近似系数
            double[] detailCol = new double[halfRows];  // 当前列的细节系数

            // 根据列位置从不同子带提取系数
            for (int i = 0; i < halfRows; i++) {
                if (j < halfCols) {
                    // 左半列：从近似和垂直子带提取
                    approxCol[i] = result.approx[i][j];     // 近似系数
                    detailCol[i] = result.vertical[i][j];   // 细节系数（垂直方向）
                } else {
                    // 右半列：从水平和对角子带提取
                    approxCol[i] = result.horizontal[i][j - halfCols];  // 近似系数（水平方向）
                    detailCol[i] = result.diagonal[i][j - halfCols];    // 细节系数（对角方向）
                }
            }

            // 对当前列进行一维小波重构
            double[][] colCoeffs = {approxCol, detailCol};
            double[] reconstructedCol = wavelet1dReconstruct(colCoeffs);

            // 将重构的列存入临时矩阵
            for (int i = 0; i < rows; i++) {
                temp[i][j] = reconstructedCol[i];
            }
        }

        // 第二步：行重构 - 对每一行进行一维小波重构
        double[][] finalResult = new double[rows][cols];  // 最终重构结果
        for (int i = 0; i < rows; i++) {
            double[] approxRow = new double[halfCols];  // 当前行的近似系数
            double[] detailRow = new double[halfCols];  // 当前行的细节系数

            // 从临时矩阵提取当前行的系数
            for (int j = 0; j < halfCols; j++) {
                approxRow[j] = temp[i][j];           // 左半部分：近似系数
                detailRow[j] = temp[i][j + halfCols]; // 右半部分：细节系数
            }

            // 对当前行进行一维小波重构
            double[][] rowCoeffs = {approxRow, detailRow};
            double[] reconstructedRow = wavelet1dReconstruct(rowCoeffs);

            // 将重构的行存入最终结果
            for (int j = 0; j < cols; j++) {
                finalResult[i][j] = reconstructedRow[j];
            }
        }

        return finalResult;  // 返回完全重构的图像
    }
}