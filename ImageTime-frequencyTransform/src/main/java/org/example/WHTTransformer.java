package org.example;

// WHT（沃尔什-哈达玛变换）
class WHTTransformer {
    // 1D WHT
    public static double[] wht1d(double[] x) {
        int N = x.length;          // 输入信号长度，必须是2的幂次
        double[] y = x.clone();    // 输出WHT系数
        // 蝶形运算：类似FFT的快速算法
        for (int span = 1; span < N; span *= 2) {
            for (int start = 0; start < N; start += 2 * span) {
                for (int i = 0; i < span; i++) {
                    int idx1 = start + i;        // 当前块的前半部分索引
                    int idx2 = start + i + span; // 当前块的后半部分索引
                    // 蝶形运算核心：加法和减法
                    double a = y[idx1];
                    double b = y[idx2];
                    y[idx1] = a + b;  // 和分量（近似）
                    y[idx2] = a - b;  // 差分量（细节）
                }
            }
        }
//        // 归一化：确保变换的能量守恒
//        double scale = 1.0 / Math.sqrt(N);
//        for (int i = 0; i < N; i++) {
//            y[i] *= scale;
//        }

        return y;
    }

    // 1D 逆WHT
    public static double[] iwht1d(double[] x) {
        int N = x.length;          // 输入WHT系数长度
        double[] y = x.clone();    // 输出重建信号

        // 逆变换需要先取消归一化
        double scale = Math.sqrt(N);
        for (int i = 0; i < N; i++) {
            y[i] *= scale;
        }

        // 逆蝶形运算：与正变换相同但顺序相反
        for (int span = N / 2; span >= 1; span /= 2) {
            for (int start = 0; start < N; start += 2 * span) {
                for (int i = 0; i < span; i++) {
                    int idx1 = start + i;        // 当前块的前半部分索引
                    int idx2 = start + i + span; // 当前块的后半部分索引

                    // 逆蝶形运算核心
                    double a = y[idx1];
                    double b = y[idx2];
                    y[idx1] = (a + b) / 2;  // 恢复原始信号
                    y[idx2] = (a - b) / 2;
                }
            }
        }
        return y;
    }
    // 2D WHT
    public static double[][] wht2d(double[][] input) {
        int rows = input.length;    // 图像行数（高度），必须是2的幂次
        int cols = input[0].length; // 图像列数（宽度），必须是2的幂次
        double[][] result = new double[rows][cols]; // 输出2D WHT系数
        double[][] temp = new double[rows][cols];   // 临时存储矩阵

        // 第一步：先对每行做一维WHT（行变换）
        for (int i = 0; i < rows; i++) {
            temp[i] = wht1d(input[i]);  // 对第i行进行WHT
        }

        // 第二步：转置矩阵，便于对列进行操作
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = temp[i][j];  // 行列互换
            }
        }

        // 第三步：对转置后的每行做WHT（相当于对原矩阵的列做WHT）
        for (int j = 0; j < cols; j++) {
            transposed[j] = wht1d(transposed[j]);  // 对第j列进行WHT
        }

        // 第四步：转置回来，恢复原始方向
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = transposed[j][i];  // 行列再次互换，恢复原顺序
            }
        }

        return result;
    }

    // 2D 逆WHT
    public static double[][] iwht2d(double[][] input) {
        int rows = input.length;    // WHT系数矩阵行数
        int cols = input[0].length; // WHT系数矩阵列数
        double[][] result = new double[rows][cols]; // 输出重建图像
        double[][] temp = new double[rows][cols];   // 临时存储矩阵

        // 第一步：转置矩阵，便于对列进行操作
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = input[i][j];  // 行列互换
            }
        }

        // 第二步：对转置后的每行做逆WHT（相当于对原WHT系数的列做逆WHT）
        for (int j = 0; j < cols; j++) {
            transposed[j] = iwht1d(transposed[j]);  // 对第j列进行逆WHT
        }

        // 第三步：转置回来
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                temp[i][j] = transposed[j][i];  // 行列再次互换，恢复原顺序
            }
        }

        // 第四步：对每行做逆WHT
        for (int i = 0; i < rows; i++) {
            result[i] = iwht1d(temp[i]);  // 对第i行进行逆WHT
        }

        return result;
    }
    // 使用直接逆变换重构图像
    public static double[][] reconstructByInverseTransform(double[][] whtCoefficients) {
        // 直接使用逆WHT变换回到空间域
        return iwht2d(whtCoefficients);
    }
}