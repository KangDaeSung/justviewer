package com.kds3393.just.justviewer2.utils;

import android.content.Context;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by android on 2016-09-29.
 */

public class CUtils {
    public static String detectEncoding(File file) throws IOException {
        byte[] buf = new byte[4096];

        FileInputStream fis = new FileInputStream(file);
        UniversalDetector detector = new UniversalDetector(null);

        int nread;
        int total = 0;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            total += nread;
            detector.handleData(buf, 0, nread);
            if (total > 1024*64) {
                break;
            }
        }

        fis.close();

        detector.dataEnd();
        String encoding = detector.getDetectedCharset();

        if (encoding == null) encoding = "EUC-KR";
        return encoding;
    }

    public static int getStatusBarHeight(Context ctx) {
        int result = 0;
        int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
