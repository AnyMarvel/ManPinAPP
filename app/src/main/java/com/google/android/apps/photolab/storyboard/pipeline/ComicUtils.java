package com.google.android.apps.photolab.storyboard.pipeline;

import java.util.Arrays;
import java.util.Random;

public class ComicUtils {
    public static Random rnd = new Random(System.currentTimeMillis());

    public static void shuffleArray(int[] input) {
        for (int i = 0; i < input.length; i++) {
            int index = rnd.nextInt(i + 1);
            int a = input[index];
            input[index] = input[i];
            input[i] = a;
        }
    }

    public static int[] shuffleArrayOfSize(int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = i;
        }
        shuffleArray(result);
        return result;
    }

    public static int[] getRandomSequence(int size) {
        int[] result = new int[size];
        int bmpCount = ComicIO.getInstance().getStoredFrameCount();
        if (bmpCount < 1) {
            bmpCount = 1;
        }
        int pos = 0;
        while (pos < size) {
            int[] subset = new int[bmpCount];
            for (int i = 0; i < bmpCount; i++) {
                subset[i] = i;
            }
            shuffleArray(subset);
            System.arraycopy(subset, 0, result, pos, Math.min(subset.length, result.length - pos));
            pos += subset.length;
        }
        shuffleArray(result);
        return Arrays.copyOfRange(result, 0, size);
    }
}
