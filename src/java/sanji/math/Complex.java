/**
 * @(#)Complex.java, 2012-10-30. 

 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */

package sanji.math;

/**
 *  Data type for complex numbers, "immutable"
 *  a            = 5.0 + 6.0i
 *  b            = -3.0 + 4.0i
 *  Re(a)        = 5.0
 *  Im(a)        = 6.0
 *  b + a        = 2.0 + 10.0i
 *  a - b        = 8.0 + 2.0i
 *  a * b        = -39.0 + 2.0i
 *  b * a        = -39.0 + 2.0i
 *  a / b        = 0.36 - 1.52i
 *  (a / b) * b  = 5.0 + 6.0i
 *  conj(a)      = 5.0 - 6.0i
 *  |a|          = 7.810249675906654
 *  tan(a)       = -6.685231390246571E-6 + 1.0000103108981198i
 *
 * @author leo
 */
public class Complex {
    /** real part */
    private final double re;
    /** imaginary part */
    private final double im;

    public static final Complex ZERO = new Complex(0, 0);
    /**
     * constructor, create a new object with the given real and imaginary parts
     * @param real
     * @param imag
     */
    public Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    /**
     * return a string representation of the invoking Complex object
     */
    @Override
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    /**
     * return abs/modulus/magnitude
     * @return
     */
    public double abs() {
        // Math.sqrt(re*re + im*im)
        return Math.hypot(re, im);
    }

    /**
     * returns the angle/phase/argument
     * @return
     */
    public double phase() {
        // between -pi and pi
        return Math.atan2(im, re);
    }

    /**
     * return a new Complex object whose value is (this + b)
     * @param b
     * @return
     */
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    /**
     * return a new Complex object whose value is (this - b)
     * @param b
     * @return
     */
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    /**
     * return a new Complex object whose value is (this * b)
     * @param b
     * @return
     */
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    /**
     * scalar multiplication
     * return a new object whose value is (this * alpha)
     * @param alpha
     * @return
     */
    public Complex times(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    /**
     * return a new Complex object whose value is the conjugate of this
     * @return
     */
    public Complex conjugate() {
        return new Complex(re, -im);
    }

    /**
     * return a new Complex object whose value is the reciprocal of this
     * @return
     */
    public Complex reciprocal() {
        double scale = re * re + im * im;
        return new Complex(re / scale, -im / scale);
    }

    /**
     * return the real part
     * @return
     */
    public double re() {
        return re;
    }

    /**
     * return the imaginary part
     * @return
     */
    public double im() {
        return im;
    }

    /**
     * return a / b
     * @param b
     * @return
     */
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    /**
     * return a new Complex object whose value is the complex exponential of this
     * @return
     */
    public Complex exp() {
        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    /**
     * return a new Complex object whose value is the complex sine of this
     * @return
     */
    public Complex sin() {
        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    /**
     * return a new Complex object whose value is the complex cosine of this
     * @return
     */
    public Complex cos() {
        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    /**
     * return a new Complex object whose value is the complex tangent of this
     * @return
     */
    public Complex tan() {
        return sin().divides(cos());
    }

    /**
     * a static version of plus
     * @param a
     * @param b
     * @return
     */
    public static Complex plus(Complex a, Complex b) {
        return new Complex(a.re + b.re, a.im + b.im);
    }

    /**
     * sample client for testing
     * @param args
     */
    public static void main(String[] args) {
    }

}
