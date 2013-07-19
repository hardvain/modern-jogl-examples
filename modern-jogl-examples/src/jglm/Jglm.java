/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jglm;

/**
 *
 * @author gbarbieri
 */
public class Jglm {

    public static float mix(float start, float end, float lerp) {
        return (start + lerp * (end - start));
    }

    public static Vec normalize(Vec vec) {

        float length = 0;

        for (int i = 0; i < vec.order; i++) {
            length += vec.toFloatArray()[i] * vec.toFloatArray()[i];
        }

        length = (float) Math.sqrt(length);

        float[] result = new float[vec.order];

        for (int i = 0; i < vec.order; i++) {
            result[i] = vec.toFloatArray()[i] / length;
        }

        return new Vec3(result);

//        float length = ((float) Math.sqrt((vec3[0] * vec3[0]) + (vec3[1] * vec3[1]) + (vec3[2] * vec3[2])));
//
//        return new float[]{vec3[0] / length, vec3[1] / length, vec3[2] / length};
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }

        return value;
    }
}