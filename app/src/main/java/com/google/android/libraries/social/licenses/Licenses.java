package com.google.android.libraries.social.licenses;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.libraries.stitch.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.mp.android.apps.R;
public final class Licenses {
    private static final String LICENSE_FILENAME = "third_party_licenses";
    private static final String LICENSE_METADATA_FILENAME = "third_party_license_metadata";
    private static final String RES_RAW = "res/raw/";
    private static final String TAG = "Licenses";

    public static ArrayList<License> getLicenses(Context context) {
        return getLicenseListFromMetadata(getTextFromResource(context.getApplicationContext(), LICENSE_METADATA_FILENAME, 0, -1), "");
    }

    public static ArrayList<License> getPluginLicenses(Context context, String jarPath) {
        String metadata = getTextFromJar("res/raw/third_party_license_metadata", jarPath, 0, -1);
        if (metadata != null) {
            return getLicenseListFromMetadata(metadata, jarPath);
        }
        return new ArrayList();
    }

    private static ArrayList<License> getLicenseListFromMetadata(String metadata, String filePath) {
        String[] entries = metadata.split("\n");
        ArrayList<License> licenses = new ArrayList(entries.length);
        for (String entry : entries) {
            int delimiter = entry.indexOf(32);
            String[] licenseLocation = entry.substring(0, delimiter).split(":");
            boolean z = licenseLocation.length == 2 && delimiter > 0;
            String str = "Invalid license meta-data line:\n";
            String valueOf = String.valueOf(entry);
            Preconditions.checkState(z, valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            licenses.add(License.create(entry.substring(delimiter + 1), Long.parseLong(licenseLocation[0]), Integer.parseInt(licenseLocation[1]), filePath));
        }
        Collections.sort(licenses);
        return licenses;
    }

    public static String getLicenseText(Context context, License license) {
        long offset = license.getLicenseOffset();
        int length = license.getLicenseLength();
        String licensePath = license.getPath();
        if (licensePath.isEmpty()) {
            return getTextFromResource(context, LICENSE_FILENAME, offset, length);
        }
        String text = getTextFromJar("res/raw/third_party_licenses", licensePath, offset, length);
        if (text != null) {
            return text;
        }
        throw new RuntimeException(new StringBuilder(String.valueOf(licensePath).length() + 46).append(licensePath).append(" does not contain ").append(RES_RAW).append(LICENSE_FILENAME).toString());
    }

    private static String getTextFromResource(Context context, String filename, long offset, int length) {
        Resources resources = context.getApplicationContext().getResources();
        return getTextFromInputStream(resources.openRawResource(resources.getIdentifier(filename, "raw", resources.getResourcePackageName(R.id.dummy_placeholder))), offset, length);
    }

    private static String getTextFromJar(String filePath, String jarPath, long offset, int length) {
        IOException e;
        Throwable th;
        JarFile jarFile = null;
        String str = null;
        try {
            JarFile jarFile2 = new JarFile(jarPath);
            try {

                JarEntry jarEntry = jarFile2.getJarEntry(filePath);
                if (jarEntry == null) {
                    str = null;
                    if (jarFile2 != null) {
                        try {
                            jarFile2.close();
                        } catch (IOException e2) {
                        }
                    }
                } else {
                    str = getTextFromInputStream(jarFile2.getInputStream(jarEntry), offset, length);
                    if (jarFile2 != null) {
                        try {
                            jarFile2.close();
                        } catch (IOException e3) {
                        }
                    }
                }
                return str;
            } catch (IOException e4) {
                e = e4;
                jarFile = jarFile2;
                try {
                    throw new RuntimeException("Failed to read license text.", e);
                } catch (Throwable th2) {
                    th = th2;
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (IOException e5) {
                        }
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                jarFile = jarFile2;
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        } catch (IOException e6) {
            e = e6;
            throw new RuntimeException("Failed to read license text.", e);
        }
        return str;
    }

    private static String getTextFromInputStream(InputStream stream, long offset, int length) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream textArray = new ByteArrayOutputStream();
        try {
            stream.skip(offset);
            int bytesRemaining = length > 0 ? length : Integer.MAX_VALUE;
            while (bytesRemaining > 0) {
                int bytes = stream.read(buffer, 0, Math.min(bytesRemaining, buffer.length));
                if (bytes == -1) {
                    break;
                }
                textArray.write(buffer, 0, bytes);
                bytesRemaining -= bytes;
            }
            stream.close();
            try {
                return textArray.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Unsupported encoding UTF8. This should always be supported.", e);
            }
        } catch (IOException e2) {
            throw new RuntimeException("Failed to read license or metadata text.", e2);
        }
    }
}
