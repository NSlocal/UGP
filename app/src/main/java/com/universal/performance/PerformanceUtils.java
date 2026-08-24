package com.universal.performance;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class PerformanceUtils {

    public static long getTotalRAM(Context ctx) {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            return stat.getBlockCountLong() * blockSize;
        } catch (Exception e) {
            return 0;
        }
    }

    public static long getAvailableRAM(Context ctx) {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getChipset() {
        String hardware = Build.HARDWARE.toLowerCase();
        if (hardware.contains("qcom") || hardware.contains("msm") || hardware.contains("sm"))
            return "Qualcomm Snapdragon";
        if (hardware.contains("mt") || hardware.contains("mediatek"))
            return "MediaTek";
        if (hardware.contains("apple"))
            return "Apple A18 Pro";
        if (hardware.contains("exynos"))
            return "Samsung Exynos";
        return "Unknown / Other";
    }

    public static boolean isArm64() {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi.equals("arm64-v8a")) return true;
        }
        return false;
    }

    public static int getRecommendedRefreshRate() {
        // 120Hz for flagship, 60Hz fallback
        String model = Build.MODEL.toLowerCase();
        if (model.contains("pro") || model.contains("ultra") || model.contains("max"))
            return 120;
        return 60;
    }
}
