package com.google.android.apps.photolab.storyboard.pipeline;

public class AnimationUtils {
    public static float easeInBackOrg(float t) {
        return (float) (((double) ((t * t) * t)) - (((double) t) * Math.sin(((double) t) * Math.PI)));
    }

    public static float easeInBack(float t) {
        return (float) (((double) ((t * t) * t)) - (((double) (t * t)) * Math.sin(((double) t) * Math.PI)));
    }

    public static float easeOutBack(float t) {
        t = 1.0f - t;
        return (float) (1.0d - (((double) ((t * t) * t)) - (((double) t) * Math.sin(((double) t) * Math.PI))));
    }

    public static float easeInOutBack(float t) {
        if (t < 0.5f) {
            float f = 2.0f * t;
            return ((float) (((double) ((f * f) * f)) - (((double) f) * Math.sin(((double) f) * Math.PI)))) * 0.5f;
        }
        double f = 1.0f - ((2.0f * t) - 1.0f);
        return (float) (((1.0d - (((double) ((f * f) * f)) - (((double) f) * Math.sin(((double) f) * Math.PI)))) * 0.5d) + 0.5d);
    }

    public static float easeInElastic(float t) {
        return (float) (Math.sin(20.420352248333657d * ((double) t)) * Math.pow(2.0d, (double) (10.0f * (t - 1.0f))));
    }

    public static float easeOutElastic(float t) {
        return (float) ((Math.sin(-20.420352248333657d * ((double) (1.0f + t))) * Math.pow(2.0d, (double) (-10.0f * t))) + 1.0d);
    }
}
