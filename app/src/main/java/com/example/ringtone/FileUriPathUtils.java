package com.example.ringtone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Android之uri、file、path相互转化
 */
public final class FileUriPathUtils {
    private FileUriPathUtils() {
        throw new UnsupportedOperationException("FileUriPathUtils  can't instantiate me...");
    }

    public final String getUriToPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }

    /**
     * uri转file: * * @param uri
     */
    public final File getUriToFile(Uri uri) {
        File file = null;
        try {
            file = new File(new URI(uri.toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * file转uri: * * @param file
     */
    public final URI getFileToUri(File file) {
        URI uri = file.toURI();
        return uri;
    }

    /**
     * file转path: * * @param file
     */
    public final String getFileToPath(File file) {
        String path = file.getPath();
        return path;
    }

    /**
     * path转uri: * * @param path
     */
    public final Uri getPathToUri(String path) {
        Uri uri = Uri.parse(path);
        return uri;
    }

    /**
     * path转file: * * @param path
     */
    public final File getPathToFile(String path) {
        File file = new File(path);
        return file;
    }

}