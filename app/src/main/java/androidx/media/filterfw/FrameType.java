package androidx.media.filterfw;

public final class FrameType {
    private static final int ACCESS_UNKNOWN = 0;
    public static final int ELEMENT_DONTCARE = 0;
    public static final int ELEMENT_FLOAT32 = 200;
    public static final int ELEMENT_FLOAT64 = 201;
    public static final int ELEMENT_INT16 = 101;
    public static final int ELEMENT_INT32 = 102;
    public static final int ELEMENT_INT64 = 103;
    public static final int ELEMENT_INT8 = 100;
    public static final int ELEMENT_OBJECT = 1;
    public static final int ELEMENT_RGBA8888 = 301;
    public static final int READ_ALLOCATION = 4;
    public static final int READ_CPU = 1;
    public static final int READ_GPU = 2;
    public static final int WRITE_ALLOCATION = 32;
    public static final int WRITE_CPU = 8;
    public static final int WRITE_GPU = 16;
    private static SimpleCache<String, FrameType> mTypeCache = new SimpleCache(64);
    private final int mAccessHints;
    private final Class<?> mClass;
    private final int mDimensions;
    private final int mElementId;

    public static FrameType any() {
        return fetchType(0, -1, 0);
    }

    public static FrameType single() {
        return fetchType(null, 0);
    }

    public static FrameType single(Class<?> clazz) {
        return fetchType(clazz, 0);
    }

    public static FrameType array() {
        return fetchType(null, 1);
    }

    public static FrameType array(Class<?> clazz) {
        return fetchType(clazz, 1);
    }

    public static FrameType buffer1D(int elementType) {
        return fetchType(elementType, 1, 0);
    }

    public static FrameType buffer2D(int elementType) {
        return fetchType(elementType, 2, 0);
    }

    public static FrameType image2D(int elementType, int accessHint) {
        return fetchType(elementType, 2, accessHint);
    }

    public FrameType asSingle() {
        if (this.mElementId == 1) {
            return fetchType(this.mClass, 0);
        }
        throw new RuntimeException("Calling asSingle() on non-object type!");
    }

    public FrameType asArray() {
        if (this.mElementId == 1) {
            return fetchType(this.mClass, 1);
        }
        throw new RuntimeException("Calling asArray() on non-object type!");
    }

    public Class<?> getContentClass() {
        return this.mClass;
    }

    public int getElementId() {
        return this.mElementId;
    }

    public int getElementSize() {
        switch (this.mElementId) {
            case 100:
                return 1;
            case 101:
                return 2;
            case 102:
            case 103:
            case 200:
            case ELEMENT_FLOAT64 /*201*/:
            case ELEMENT_RGBA8888 /*301*/:
                return 4;
            default:
                return 0;
        }
    }

    public int getAccessHints() {
        return this.mAccessHints;
    }

    public int getNumberOfDimensions() {
        return this.mDimensions;
    }

    public boolean isSpecified() {
        return this.mElementId != 0 && this.mDimensions >= 0;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FrameType)) {
            return false;
        }
        FrameType type = (FrameType) object;
        if (this.mElementId == type.mElementId && this.mDimensions == type.mDimensions && this.mAccessHints == type.mAccessHints && this.mClass == type.mClass) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((this.mElementId ^ this.mDimensions) ^ this.mAccessHints) ^ this.mClass.hashCode();
    }

    public String toString() {
        String elementToString = elementToString(this.mElementId, this.mClass);
        String result = new StringBuilder(String.valueOf(elementToString).length() + 13).append(elementToString).append("[").append(this.mDimensions).append("]").toString();
        if ((this.mAccessHints & 1) != 0) {
            result = String.valueOf(result).concat("(rcpu)");
        }
        if ((this.mAccessHints & 2) != 0) {
            result = String.valueOf(result).concat("(rgpu)");
        }
        if ((this.mAccessHints & 4) != 0) {
            result = String.valueOf(result).concat("(ralloc)");
        }
        if ((this.mAccessHints & 8) != 0) {
            result = String.valueOf(result).concat("(wcpu)");
        }
        if ((this.mAccessHints & 16) != 0) {
            result = String.valueOf(result).concat("(wgpu)");
        }
        if ((this.mAccessHints & 32) != 0) {
            return String.valueOf(result).concat("(walloc)");
        }
        return result;
    }

    String keyString() {
        return keyValueForType(this.mElementId, this.mDimensions, this.mAccessHints, this.mClass);
    }

    static FrameType tryMerge(FrameType writer, FrameType reader) {
        if (writer.mElementId == 0) {
            return reader;
        }
        if (reader.mElementId == 0) {
            return writer;
        }
        if (writer.mElementId == 1 && reader.mElementId == 1) {
            return tryMergeObjectTypes(writer, reader);
        }
        if (writer.mDimensions <= 0 || writer.mElementId != reader.mElementId) {
            return null;
        }
        return tryMergeBuffers(writer, reader);
    }

    static FrameType tryMergeObjectTypes(FrameType writer, FrameType reader) {
        int dimensions = Math.max(writer.mDimensions, reader.mDimensions);
        Class<?> mergedClass = mergeClasses(writer.mClass, reader.mClass);
        boolean success = mergedClass != null || writer.mClass == null;
        return success ? fetchType(mergedClass, dimensions) : null;
    }

    static FrameType tryMergeBuffers(FrameType writer, FrameType reader) {
        if (writer.mDimensions != reader.mDimensions) {
            return null;
        }
        return fetchType(writer.mElementId, writer.mDimensions, writer.mAccessHints | reader.mAccessHints);
    }

    static FrameType merge(FrameType writer, FrameType reader) {
        FrameType result = tryMerge(writer, reader);
        if (result != null) {
            return result;
        }
        String valueOf = String.valueOf(writer);
        String valueOf2 = String.valueOf(reader);
        throw new RuntimeException(new StringBuilder((String.valueOf(valueOf).length() + 40) + String.valueOf(valueOf2).length()).append("Incompatible types in connection: ").append(valueOf).append(" vs. ").append(valueOf2).append("!").toString());
    }

    private static String keyValueForType(int elemId, int dims, int hints, Class<?> clazz) {
        String name = clazz != null ? clazz.getName() : "0";
        return new StringBuilder(String.valueOf(name).length() + 36).append(elemId).append(":").append(dims).append(":").append(hints).append(":").append(name).toString();
    }

    private static String elementToString(int elemId, Class<?> clazz) {
        switch (elemId) {
            case 0:
                return "*";
            case 1:
                String simpleName = clazz == null ? "*" : clazz.getSimpleName();
                return new StringBuilder(String.valueOf(simpleName).length() + 2).append("<").append(simpleName).append(">").toString();
            case 100:
                return "int8";
            case 101:
                return "int16";
            case 102:
                return "int32";
            case 103:
                return "int64";
            case 200:
                return "float32";
            case ELEMENT_FLOAT64 /*201*/:
                return "float64";
            case ELEMENT_RGBA8888 /*301*/:
                return "rgba8888";
            default:
                return "?";
        }
    }

    private static Class<?> mergeClasses(Class<?> classA, Class<?> classB) {
        if (classA == null) {
            return classB;
        }
        if (classB == null) {
            return classA;
        }
        if (classA.isAssignableFrom(classB)) {
            return classB;
        }
        if (classB.isAssignableFrom(classA)) {
            return classA;
        }
        return null;
    }

    private static FrameType fetchType(int elementId, int dimensions, int accessHints) {
        return fetchType(elementId, dimensions, accessHints, null);
    }

    private static FrameType fetchType(Class<?> clazz, int dimensions) {
        return fetchType(1, dimensions, 0, clazz);
    }

    private static FrameType fetchType(int elementId, int dimensions, int accessHints, Class<?> clazz) {
        String typeKey = keyValueForType(elementId, dimensions, accessHints, clazz);
        FrameType type = (FrameType) mTypeCache.get(typeKey);
        if (type != null) {
            return type;
        }
        type = new FrameType(elementId, dimensions, accessHints, clazz);
        mTypeCache.put(typeKey, type);
        return type;
    }

    private FrameType(int elementId, int dimensions, int accessHints, Class<?> clazz) {
        this.mElementId = elementId;
        this.mDimensions = dimensions;
        this.mClass = clazz;
        this.mAccessHints = accessHints;
    }
}
