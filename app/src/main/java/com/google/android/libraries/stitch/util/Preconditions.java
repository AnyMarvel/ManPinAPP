package com.google.android.libraries.stitch.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.Collection;

public final class Preconditions {
    private Preconditions() {
    }

    public static <T extends CharSequence> T checkNotEmpty(T charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            return charSequence;
        }
        throw new IllegalArgumentException();
    }

    public static <T extends CharSequence> T checkNotEmpty(T charSequence, Object errorMessage) {
        if (!TextUtils.isEmpty(charSequence)) {
            return charSequence;
        }
        throw new IllegalArgumentException(String.valueOf(errorMessage));
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T reference) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException();
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T reference, Object errorMessage) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(String.valueOf(errorMessage));
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }

    @Deprecated
    @NonNull
    public static <T extends Iterable<?>> T checkContentsNotNull(@Nullable T iterable) {
        if (!containsOrIsNull(iterable)) {
            return iterable;
        }
        throw new NullPointerException();
    }

    @Deprecated
    @NonNull
    public static <T extends Iterable<?>> T checkContentsNotNull(@Nullable T iterable, Object errorMessage) {
        if (!containsOrIsNull(iterable)) {
            return iterable;
        }
        throw new NullPointerException(String.valueOf(errorMessage));
    }

    @Deprecated
    @NonNull
    public static <T extends Iterable<?>> T checkContentsNotNull(@Nullable T iterable, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!containsOrIsNull(iterable)) {
            return iterable;
        }
        throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }

    private static boolean containsOrIsNull(Iterable<?> iterable) {
        if (iterable == null) {
            return true;
        }
        if (iterable instanceof Collection) {
            try {
                return ((Collection) iterable).contains(null);
            } catch (NullPointerException e) {
                return false;
            }
        }
        for (Object element : iterable) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    public static int checkElementIndex(int index, int size) {
        return checkElementIndex(index, size, "index");
    }

    public static int checkElementIndex(int index, int size, String desc) {
        if (index >= 0 && index < size) {
            return index;
        }
        throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
    }

    private static String badElementIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, Integer.valueOf(index));
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else {
            return format("%s (%s) must be less than size (%s)", desc, Integer.valueOf(index), Integer.valueOf(size));
        }
    }

    public static int checkPositionIndex(int index, int size) {
        return checkPositionIndex(index, size, "index");
    }

    public static int checkPositionIndex(int index, int size, String desc) {
        if (index >= 0 && index <= size) {
            return index;
        }
        throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, Integer.valueOf(index));
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else {
            return format("%s (%s) must not be greater than size (%s)", desc, Integer.valueOf(index), Integer.valueOf(size));
        }
    }

    public static void checkPositionIndexes(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        return format("end index (%s) must not be less than start index (%s)", Integer.valueOf(end), Integer.valueOf(start));
    }

    static String format(String template, Object... args) {
        template = String.valueOf(template);
        StringBuilder builder = new StringBuilder(template.length() + (args.length * 16));
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            int i2 = i + 1;
            builder.append(args[i]);
            templateStart = placeholderStart + 2;
            i = i2;
        }
        builder.append(template.substring(templateStart));
        if (i < args.length) {
            builder.append(" [");
            int i2 = i + 1;
            builder.append(args[i]);
            i = i2;
            while (i < args.length) {
                builder.append(", ");
                i2 = i + 1;
                builder.append(args[i]);
                i = i2;
            }
            builder.append(']');
        }
        return builder.toString();
    }
}
