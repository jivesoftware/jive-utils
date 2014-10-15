package com.jivesoftware.os.jive.utils.health.api;

/**
 *
 * @author jonathan.colt
 */
public class HealthCheckUtil {

    public static void main(String[] args) {
        System.out.println("--");
        System.out.println(zeroToOne(0, 10, 5));
        System.out.println(zeroToOne(10, 0, 5));
        System.out.println("--");

        System.out.println(zeroToOne(0, 10, 8));
        System.out.println(zeroToOne(10, 0, 8));
        System.out.println("--");

        System.out.println(zeroToOne(0, 10, 11));
        System.out.println(zeroToOne(10, 0, 11));
        System.out.println("--");

        System.out.println(zeroToOne(0, 10, -2));
        System.out.println(zeroToOne(10, 0, -2));
    }

    public static double zeroToOne(long _min, long _max, long _value) {
        if (_max == _min) {
            if (_value == _min) {
                return 0;
            }
            if (_value > _max) {
                return Double.MAX_VALUE;
            }
            return -Double.MAX_VALUE;
        }
        return (double) (_value - _min) / (double) (_max - _min);
    }

    public static double zeroToOne(double _min, double _max, double _value) {
        if (_max == _min) {
            if (_value == _min) {
                return 0;
            }
            if (_value > _max) {
                return Double.MAX_VALUE;
            }
            return -Double.MAX_VALUE;
        }
        return (double) (_value - _min) / (double) (_max - _min);
    }
}
