/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.guam.museumentry.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.guam.museumentry.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

public class AndroidUtilities {

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();
    private static final String TAG = "AndroidUtilities";
    private static final Object smsLock = new Object();
    private static final String[] projectionPhotos = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION
    };
    private static final String[] projectionVideo = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN
    };
    private static final String[] projectionCommon = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Files.FileColumns.MEDIA_TYPE
    };
    public static int statusBarHeight = 0;
    public static float density = 1;
    public static Point displaySize = new Point();
    public static Integer photoSize = null;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int leftBaseline;
    public static boolean usingHardwareInput;
    public static AlbumEntry allPhotosAlbumEntry;
    public static Random random = new Random();
    public static String NOMEDIA = ".nomedia";
    public static int chatCellHeight = 100;
    //business_card_logo_height
    public static int bclh = 0;
    public static int paddingTop = 0;
    public static int paddingBottom = 0;
    public static int paddingTextBottom = 0;
    public static int contactMarginBottom = 0;
    public static int contactMarginTop = 0;
    public static int chatTextMarginTop = 0;
    public static int chatTextMargin = 0;
    public static int chatTextMarginBottom = 0;
    private static int prevOrientation = -10;
    private static boolean waitingForSms = false;
    private static Boolean isTablet = null;
    private static int adjustOwnerClassGuid = 0;
    private static TextPaint textPaint;

    static {
        density = GlobalApplication.applicationContext.getResources().getDisplayMetrics().density;
        leftBaseline = isTablet() ? 80 : 72;
        checkDisplaySize();
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = GlobalApplication.applicationContext.getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static void checkDisplaySize() {
        try {
            Configuration configuration = GlobalApplication.applicationContext.getResources().getConfiguration();
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) GlobalApplication.applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    if (Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                    Log.e("tmessages", "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
                }
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
        if (displaySize != null) {
            if (displaySize.x > 0) {
                chatCellHeight = (int) ((displaySize.x - dp(52)) * 0.57);
            }
        }
        /*
        keyboardHidden
        public static final int KEYBOARDHIDDEN_NO = 1
        Constant for keyboardHidden, value corresponding to the keysexposed resource qualifier.

        public static final int KEYBOARDHIDDEN_UNDEFINED = 0
        Constant for keyboardHidden: a value indicating that no value has been set.

        public static final int KEYBOARDHIDDEN_YES = 2
        Constant for keyboardHidden, value corresponding to the keyshidden resource qualifier.

        hardKeyboardHidden
        public static final int HARDKEYBOARDHIDDEN_NO = 1
        Constant for hardKeyboardHidden, value corresponding to the physical keyboard being exposed.

        public static final int HARDKEYBOARDHIDDEN_UNDEFINED = 0
        Constant for hardKeyboardHidden: a value indicating that no value has been set.

        public static final int HARDKEYBOARDHIDDEN_YES = 2
        Constant for hardKeyboardHidden, value corresponding to the physical keyboard being hidden.

        keyboard
        public static final int KEYBOARD_12KEY = 3
        Constant for keyboard, value corresponding to the 12key resource qualifier.

        public static final int KEYBOARD_NOKEYS = 1
        Constant for keyboard, value corresponding to the nokeys resource qualifier.

        public static final int KEYBOARD_QWERTY = 2
        Constant for keyboard, value corresponding to the qwerty resource qualifier.
         */
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            GlobalApplication.applicationHandler.post(runnable);
        } else {
            GlobalApplication.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static long makeBroadcastId() {
        return random.nextLong();// 0x0000000100000000L | ((long)id & 0x00000000FFFFFFFFL);
    }

    public static File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = GlobalApplication.applicationContext.getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {
                Log.e("tmessages", e.getMessage());
            }
        }
        try {
            File file = GlobalApplication.applicationContext.getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
        return new File("");
    }

    /***
     * Load Photos and Videos from Gallery and notify it
     *
     * @param guid
     */
    public static void loadGalleryPhotosAlbums(final int guid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<AlbumEntry> albumsSorted = new ArrayList<>();
                final ArrayList<AlbumEntry> videoAlbumsSorted = new ArrayList<>();
                HashMap<Integer, AlbumEntry> albums = new HashMap<>();
                AlbumEntry allPhotosAlbum = null;
                String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
                Integer cameraAlbumId = null;
                Integer cameraAlbumVideoId = null;

                Cursor cursor = null;
                try {
                    if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && GlobalApplication.applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        cursor = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                        if (cursor != null) {
                            int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                            int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                            int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                            int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                            while (cursor.moveToNext()) {
                                int imageId = cursor.getInt(imageIdColumn);
                                int bucketId = cursor.getInt(bucketIdColumn);
                                String bucketName = cursor.getString(bucketNameColumn);
                                String path = cursor.getString(dataColumn);
                                long dateTaken = cursor.getLong(dateColumn);
                                int orientation = cursor.getInt(orientationColumn);

                                if (path == null || path.length() == 0) {
                                    continue;
                                }

                                PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);

                                if (allPhotosAlbum == null) {
                                    allPhotosAlbum = new AlbumEntry(0, "Camera Roll", photoEntry, false);
                                    albumsSorted.add(0, allPhotosAlbum);
                                }
                                if (allPhotosAlbum != null) {
                                    allPhotosAlbum.addPhoto(photoEntry);
                                }

                                AlbumEntry albumEntry = albums.get(bucketId);
                                if (albumEntry == null) {
                                    albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, false);
                                    albums.put(bucketId, albumEntry);
                                    if (cameraAlbumId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                        albumsSorted.add(0, albumEntry);
                                        cameraAlbumId = bucketId;
                                    } else {
                                        albumsSorted.add(albumEntry);
                                    }
                                }

                                albumEntry.addPhoto(photoEntry);
                            }
                        }
                    }
                } catch (Throwable e) {
                    Log.e("tmessages", e.getMessage());
                } finally {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            Log.e("tmessages", e.getMessage());
                        }
                    }
                }

                try {
                    if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && GlobalApplication.applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        albums.clear();
                        AlbumEntry allVideosAlbum = null;
                        cursor = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projectionVideo, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
                        if (cursor != null) {
                            int imageIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                            int bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                            int bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                            int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                            int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);

                            while (cursor.moveToNext()) {
                                int imageId = cursor.getInt(imageIdColumn);
                                int bucketId = cursor.getInt(bucketIdColumn);
                                String bucketName = cursor.getString(bucketNameColumn);
                                String path = cursor.getString(dataColumn);
                                long dateTaken = cursor.getLong(dateColumn);

                                if (path == null || path.length() == 0) {
                                    continue;
                                }

                                PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, 0, true);

                                if (allVideosAlbum == null) {
                                    allVideosAlbum = new AlbumEntry(0, "All Video", photoEntry, true);
                                    videoAlbumsSorted.add(0, allVideosAlbum);
                                }
                                if (allVideosAlbum != null) {
                                    allVideosAlbum.addPhoto(photoEntry);
                                }

                                AlbumEntry albumEntry = albums.get(bucketId);
                                if (albumEntry == null) {
                                    albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, true);
                                    albums.put(bucketId, albumEntry);
                                    if (cameraAlbumVideoId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                        videoAlbumsSorted.add(0, albumEntry);
                                        cameraAlbumVideoId = bucketId;
                                    } else {
                                        videoAlbumsSorted.add(albumEntry);
                                    }
                                }

                                albumEntry.addPhoto(photoEntry);
                            }
                        }
                    }
                } catch (Throwable e) {
                    Log.e("tmessages", e.getMessage());
                } finally {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            Log.e("tmessages", e.getMessage());
                        }
                    }
                }

                final Integer cameraAlbumIdFinal = cameraAlbumId;
                final Integer cameraAlbumVideoIdFinal = cameraAlbumVideoId;
                final AlbumEntry allPhotosAlbumFinal = allPhotosAlbum;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        allPhotosAlbumEntry = allPhotosAlbumFinal;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.albumsDidLoaded, guid, albumsSorted, cameraAlbumIdFinal, videoAlbumsSorted, cameraAlbumVideoIdFinal);
                    }
                });
            }
        }).start();
    }

    /***
     * Load Photos and Videos from Gallery and notify it
     *
     * @param guid
     */
    public static void loadGalleryPhotosAlbumsInSingleCursor(final int guid) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        final ArrayList<AlbumEntry> albumsSorted = new ArrayList<>();
        final ArrayList<AlbumEntry> videoAlbumsSorted = new ArrayList<>();
        HashMap<Integer, AlbumEntry> albums = new HashMap<>();
        AlbumEntry allPhotosAlbum = null;
        AlbumEntry allVideosAlbum = null;
        String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
        Integer cameraAlbumId = null;
        Integer cameraAlbumVideoId = null;

        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 &&
                    GlobalApplication.applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

                Uri queryUri = MediaStore.Files.getContentUri("external");

                CursorLoader cursorLoader = new CursorLoader(
                        GlobalApplication.applicationContext,
                        queryUri,
                        projectionCommon,
                        selection,
                        null, // Selection args (none).
                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
                );
                cursor = cursorLoader.loadInBackground();
//                        cursor = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(),
//                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, null,
//                                null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                if (cursor != null) {
                    String[] columns = cursor.getColumnNames();
                    for (int i = 0; i < columns.length; i++) {
                        Log.d(TAG, "run: Columns Name" + columns[i]);
                    }
                    int imageIdColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    int dateColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                    int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                    int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
//
                    while (cursor.moveToNext()) {
                        int imageId = cursor.getInt(imageIdColumn);
                        int bucketId = cursor.getInt(bucketIdColumn);
                        String bucketName = cursor.getString(bucketNameColumn);
                        String path = cursor.getString(dataColumn);
                        long dateTaken = cursor.getLong(dateColumn);
                        int orientation = cursor.getInt(orientationColumn);
                        int mimeType = cursor.getInt(mimeTypeColumn);

                        if (path == null || path.length() == 0) {
                            continue;
                        }
                        PhotoEntry photoEntry = null;
                        if (mimeType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                            photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);
                            if (allPhotosAlbum == null) {
                                allPhotosAlbum = new AlbumEntry(0, "Camera Roll", photoEntry, false);
                                albumsSorted.add(0, allPhotosAlbum);
                            }
                            allPhotosAlbum.addPhoto(photoEntry);
                        } else if (mimeType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                            photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, 0, true);
                            if (allVideosAlbum == null) {
                                allVideosAlbum = new AlbumEntry(1, "All Video", photoEntry, true);
                                albumsSorted.add(0, allVideosAlbum);
                            }
                            allVideosAlbum.addPhoto(photoEntry);
                        }

                        AlbumEntry albumEntry = albums.get(bucketId);
                        if (albumEntry == null) {
                            if (mimeType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, false);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumId == null && path.startsWith(cameraFolder)) {
                                    albumsSorted.add(albumEntry);
                                    cameraAlbumId = bucketId;
                                } else {
                                    albumsSorted.add(albumEntry);
                                }
                            } else if (mimeType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, true);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumVideoId == null && path.startsWith(cameraFolder)) {
                                    albumsSorted.add(albumEntry);
                                    cameraAlbumVideoId = bucketId;
                                } else {
                                    albumsSorted.add(albumEntry);
                                }
                            }
                        }
                        if (albumEntry != null) {
                            albumEntry.addPhoto(photoEntry);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("tmessages", e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("tmessages", e.getMessage());
                }
            }
        }
        final Integer cameraAlbumIdFinal = cameraAlbumId;
        final Integer cameraAlbumVideoIdFinal = cameraAlbumVideoId;
        final AlbumEntry allPhotosAlbumFinal = allPhotosAlbum;
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                allPhotosAlbumEntry = allPhotosAlbumFinal;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.albumsDidLoaded, guid, albumsSorted, cameraAlbumIdFinal, videoAlbumsSorted, cameraAlbumVideoIdFinal);
            }
        });
    }

    public static PhotoEntry getUriFromPath(final String originalpah) {
        final PhotoEntry[] photoEntry = {null};

        Integer cameraAlbumId = null;
        Integer cameraAlbumVideoId = null;
        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && GlobalApplication.applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                cursor = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, MediaStore.Images.Media.DATA + "=?", new String[]{originalpah}, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                if (cursor != null) {
                    int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                    while (cursor.moveToNext()) {
                        int imageId = cursor.getInt(imageIdColumn);
                        int bucketId = cursor.getInt(bucketIdColumn);
                        String bucketName = cursor.getString(bucketNameColumn);
                        String path = cursor.getString(dataColumn);
                        long dateTaken = cursor.getLong(dateColumn);
                        int orientation = cursor.getInt(orientationColumn);
                        if (path == null || path.length() == 0) {
                            continue;
                        }
                        photoEntry[0] = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("tmessages", e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("tmessages", e.getMessage());
                }
            }
        }
        return photoEntry[0];
    }

    public static AlbumEntry getAlbumEntryFromPath(final String originalpah) {
        final AlbumEntry[] photoEntry = {null};
        Integer cameraAlbumId = null;
        Integer cameraAlbumVideoId = null;
        Cursor cursor = null;
        Cursor cursor1 = null;
        try {
            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && GlobalApplication.applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                cursor = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, MediaStore.Images.Media.DATA + "=?", new String[]{originalpah}, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                if (cursor != null) {
                    int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                    while (cursor.moveToNext()) {
                        int imageId = cursor.getInt(imageIdColumn);
                        int bucketId = cursor.getInt(bucketIdColumn);
                        String bucketName = cursor.getString(bucketNameColumn);
                        String path = cursor.getString(dataColumn);
                        long dateTaken = cursor.getLong(dateColumn);
                        int orientation = cursor.getInt(orientationColumn);
                        if (path == null || path.length() == 0) {
                            continue;
                        }
                        PhotoEntry photoEntry1 = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);
                        photoEntry[0] = new AlbumEntry(bucketId, bucketName, photoEntry1, false);
                        break;
                    }
                }
                if (photoEntry[0] != null) {
                    cursor1 = MediaStore.Images.Media.query(GlobalApplication.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[]{photoEntry[0].bucketName}, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                    if (cursor1 != null) {
                        int imageIdColumn = cursor1.getColumnIndex(MediaStore.Images.Media._ID);
                        int bucketIdColumn = cursor1.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                        int bucketNameColumn = cursor1.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor1.getColumnIndex(MediaStore.Images.Media.DATA);
                        int dateColumn = cursor1.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                        int orientationColumn = cursor1.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                        while (cursor1.moveToNext()) {
                            int imageId = cursor1.getInt(imageIdColumn);
                            int bucketId = cursor1.getInt(bucketIdColumn);
                            String bucketName = cursor1.getString(bucketNameColumn);
                            String path = cursor1.getString(dataColumn);
                            long dateTaken = cursor1.getLong(dateColumn);
                            int orientation = cursor1.getInt(orientationColumn);
                            if (path == null || path.length() == 0) {
                                continue;
                            }
                            PhotoEntry photoEntry1 = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);
                            photoEntry[0].addPhoto(photoEntry1);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("tmessages", e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("tmessages", e.getMessage());
                }
            }
            if (cursor1 != null) {
                try {
                    cursor1.close();
                } catch (Exception e) {
                    Log.e("tmessages", e.getMessage());
                }
            }
        }
        return photoEntry[0];
    }

    public static int getCurrentActionBarHeight() {
        if (AndroidUtilities.isTablet()) {
            return AndroidUtilities.dp(64);
        } else if (GlobalApplication.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return AndroidUtilities.dp(48);
        } else {
            return AndroidUtilities.dp(56);
        }

    }

    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream source = null;
        FileOutputStream destination = null;
        try {
            source = new FileInputStream(sourceFile);
            destination = new FileOutputStream(destFile);
            destination.getChannel().transferFrom(source.getChannel(), 0, source.getChannel().size());
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
            return false;
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return true;
    }

    public static String escapeBracket(String s) {
        return s.replaceAll("\\[", "").replaceAll("\\]", "");
    }

    public static int getPhotoSize() {
        if (photoSize == null) {
            if (Build.VERSION.SDK_INT >= 16) {
                photoSize = 1280;
            } else {
                photoSize = 800;
            }
        }
        return photoSize;
    }

    public static void addMediaToGallery(String fromPath) {
        if (fromPath == null) {
            return;
        }
        File f = new File(fromPath);
        Uri contentUri = Uri.fromFile(f);
        addMediaToGallery(contentUri);
    }

    public static void addMediaToGallery(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            GlobalApplication.applicationContext.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        GlobalApplication.applicationHandler.removeCallbacks(runnable);
    }

    @SuppressLint("NewApi")
    public static String getPath(final Uri uri) {
        try {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(GlobalApplication.applicationContext, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(GlobalApplication.applicationContext, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    switch (type) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(GlobalApplication.applicationContext, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(GlobalApplication.applicationContext, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGalleryDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String MD5(String md5) {
        if (md5 == null) {
            return null;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            Log.e("tmessages", e.getMessage());
        }
        return null;
    }

    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21 || view.getHeight() == AndroidUtilities.displaySize.y || view.getHeight() == AndroidUtilities.displaySize.y - statusBarHeight) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
        return 0;
    }

    public static float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 700;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    public static int getRandomBW(int min, int max) {
        return random.nextInt(max + 1 - min) + min;
    }

    public static int givemeTotalDownload(String path) {
        int no_of_images = 0;
        try {
            int no_of_comma = path.replaceAll("[^,]", "").length();
            if (path.contains("<>")) {
                no_of_images = 0;
            } else if (no_of_comma == 1) {
                no_of_images = Integer.parseInt(path.substring(path.lastIndexOf(",") + 1));
            } else {
                Log.wtf(TAG, "givemeTotalDownload: " + no_of_comma + ":" + no_of_images + ":" + path);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return no_of_images;
    }

    public static String givemeTracker(String path) {
        String no_of_images = path;
        int no_of_comma = path.replaceAll("[^,]", "").length();
        if (no_of_comma == 1) {
            no_of_images = path.substring(0, path.indexOf(","));
        } else {
            Log.wtf(TAG, "givemeTotalDownload: " + no_of_comma + ":" + no_of_images + ":" + path);
        }
        if (!TextUtils.isEmpty(no_of_images) && no_of_images.endsWith("_thumb.zip")) {
            no_of_images = no_of_images.replace("_thumb.zip", "");
        }
        if (!TextUtils.isEmpty(no_of_images) && no_of_images.endsWith("_thumb.mp4.zip")) {
            no_of_images = no_of_images.replace("_thumb.mp4.zip", "");
        }
        Log.i(TAG, "givemeTracker: Tracker is :" + no_of_images);
        return no_of_images;
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        try {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
        }
    }

    public static File getProfileDir() {
        File rootFile = getUserProfileDir();
        File root = new File(rootFile, "I'M XAM Profile");
        if (!root.exists())
            root.mkdirs();
        try {
            File nomediaCheck = new File(root, NOMEDIA);
            if (!nomediaCheck.exists()) {
                nomediaCheck.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return root;
    }

    public static File getUserProfileDir() {
        File customCacheDirectory = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + "CACHE_DIR");
        if (!customCacheDirectory.exists()) {
            boolean isCreated = customCacheDirectory.mkdirs();
            Log.d(TAG, "::" + customCacheDirectory.getAbsolutePath() + "isCreated:" + isCreated);
        }
        return customCacheDirectory;
    }

    public static int calculateBorderOf(String tg) {
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xff000000);
            int pixelSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, AndroidUtilities.displayMetrics);
            textPaint.setTextSize(pixelSize);
            textPaint.linkColor = 0xff316f9f;
            textPaint.setTypeface(Typeface.MONOSPACE);
        }
        Log.d(TAG, "calculateBorderOf() called with: tg = [" + tg + "]");
        Rect rect = new Rect();
        textPaint.getTextBounds(tg, 0, tg.length(), rect);
        Log.d(TAG, "calculateBorderOf: Height of :" + tg + ":IS:" + rect.height() + ":Rect is :" + rect.toString());
        int heightToReturn = rect.height();
//        if (tg.equals("a") && rect.bottom > 0) {
//            heightToReturn = heightToReturn + rect.bottom;
//            Log.d(TAG, "calculateBorderOf: We are increasing height :" + heightToReturn);
//        }
        return heightToReturn;
    }

    public static void setRectToRect(Matrix matrix, RectF src, RectF dst, int rotation, Matrix.ScaleToFit align) {
        float tx, sx;
        float ty, sy;
        if (rotation == 90 || rotation == 270) {
            sx = dst.height() / src.width();
            sy = dst.width() / src.height();
        } else {
            sx = dst.width() / src.width();
            sy = dst.height() / src.height();
        }
        if (align != Matrix.ScaleToFit.FILL) {
            if (sx > sy) {
                sx = sy;
            } else {
                sy = sx;
            }
        }
        tx = -src.left * sx;
        ty = -src.top * sy;

        matrix.setTranslate(dst.left, dst.top);
        if (rotation == 90) {
            matrix.preRotate(90);
            matrix.preTranslate(0, -dst.width());
        } else if (rotation == 180) {
            matrix.preRotate(180);
            matrix.preTranslate(-dst.width(), -dst.height());
        } else if (rotation == 270) {
            matrix.preRotate(270);
            matrix.preTranslate(-dst.height(), 0);
        }

        matrix.preScale(sx, sy);
        matrix.preTranslate(tx, ty);
    }

    public static boolean isInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) GlobalApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<>();
        public HashMap<Integer, PhotoEntry> photosByIds = new HashMap<>();
        public boolean isVideo;

        public AlbumEntry(int bucketId, String bucketName, PhotoEntry coverPhoto, boolean isVideo) {
            this.bucketId = bucketId;
            this.bucketName = bucketName;
            this.coverPhoto = coverPhoto;
            this.isVideo = isVideo;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            photos.add(photoEntry);
            photosByIds.put(photoEntry.imageId, photoEntry);
        }
    }

    public static class PhotoEntry implements Serializable {
        public int bucketId;
        public int imageId;
        public long dateTaken;
        public String path;
        public int orientation;
        public String thumbPath;
        public String imagePath;
        public boolean isVideo;
        public CharSequence caption;

        public PhotoEntry(int bucketId, int imageId, long dateTaken, String path, int orientation, boolean isVideo) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
            this.isVideo = isVideo;
        }
    }

    public static class SearchImage {
        public String id;
        public String imageUrl;
        public String thumbUrl;
        public String localUrl;
        public int width;
        public int height;
        public int size;
        public int type;
        public int date;
        public String thumbPath;
        public String imagePath;
        public CharSequence caption;
    }


}
