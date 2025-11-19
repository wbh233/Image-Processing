package org.example;

// 复数类: 用于FFT计算中的复数运算
class Complex {
    // 实部：复数的实数部分,代表余弦成分的幅度
    public double real;
    // 虚部：复数的虚数部分,代表正弦成分的幅度
    public double imag;

    // 构造函数：创建复数对象
    public Complex(double real, double imag) {
        this.real = real; // 初始化实部
        this.imag = imag; // 初始化虚部
    }

    // 复数加法：两个复数相加
    public Complex add(Complex other) {
        // 返回新复数：实部相加，虚部相加
        return new Complex(this.real + other.real, this.imag + other.imag);
    }

    // 复数减法：两个复数相减
    public Complex subtract(Complex other) {
        // 返回新复数：实部相减，虚部相减
        return new Complex(this.real - other.real, this.imag - other.imag);
    }

    // 复数乘法：两个复数相乘
    public Complex multiply(Complex other) {
        // 复数乘法公式：(a+bi)(c+di) = (ac-bd) + (ad+bc)i
        return new Complex(
                this.real * other.real - this.imag * other.imag, // 实部：ac - bd
                this.real * other.imag + this.imag * other.real  // 虚部：ad + bc
        );
    }

    // 共轭复数：实部相同，虚部相反
    public Complex conjugate() {
        // 返回新复数：实部不变，虚部取反
        return new Complex(real, -imag);
    }
    // 计算幅度（模）：复数的绝对值
    public double magnitude() {
        // 幅度公式：|a+bi| = √(a² + b²)
        return Math.sqrt(real * real + imag * imag);
    }
    // 从极坐标创建复数：通过幅度和相位角创建复数
    public static Complex fromPolar(double magnitude, double phase) {
        // 极坐标转直角坐标公式：real = magnitude * cos(phase), imag = magnitude * sin(phase)
        return new Complex(magnitude * Math.cos(phase), magnitude * Math.sin(phase));
    }
}