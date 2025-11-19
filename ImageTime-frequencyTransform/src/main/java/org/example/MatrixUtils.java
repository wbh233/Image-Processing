package org.example;

// 矩阵工具类：提供矩阵操作相关的工具方法
class MatrixUtils {
    // 复制二维双精度浮点数矩阵：创建输入矩阵的深拷贝,深拷贝就是修改原数据也不会改变拷贝完的
    public static double[][] copyMatrix(double[][] matrix) {
        // 获取原始矩阵的行数和列数
        int rows = matrix.length;        // 矩阵的行数
        int cols = matrix[0].length;     // 矩阵的列数
        // 创建新的二维数组用于存储拷贝结果
        double[][] copy = new double[rows][cols];
        // 逐行复制矩阵数据
        for (int i = 0; i < rows; i++) {
            // 使用System.arraycopy高效复制整行数据
            // 参数说明：源数组, 源起始位置, 目标数组, 目标起始位置, 复制长度
            System.arraycopy(matrix[i], 0, copy[i], 0, cols);
        }
        // 返回复制后的新矩阵
        return copy;
    }
    // 复制二维复数矩阵：创建复数矩阵的深拷贝（包括每个复数的实部和虚部）
    public static Complex[][] copyComplexMatrix(Complex[][] matrix) {
        // 获取原始复数矩阵的行数和列数
        int rows = matrix.length;        // 矩阵的行数
        int cols = matrix[0].length;     // 矩阵的列数
        // 创建新的二维复数数组
        Complex[][] copy = new Complex[rows][cols];
        // 逐个元素复制复数对象
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 为每个位置创建新的Complex对象，复制实部和虚部值
                // 注意：这里创建新对象而不是引用原有对象，确保真正的深拷贝
                copy[i][j] = new Complex(matrix[i][j].real, matrix[i][j].imag);
            }
        }
        // 返回复制后的新复数矩阵
        return copy;
    }

}