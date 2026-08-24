package com.universal.performance;

import java.util.Arrays;
import java.util.List;

public class GameConfig {
    // 🎮 GAME 1: QQ飞车 (China)
    public static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    public static final String NAME_QQ_SPEED = "QQ Speed / QQ飞车";
    public static final String VER_QQ_SPEED = "1.58.0.50557";
    
    // 🎮 GAME 2: Speed Drifters (Global/Taiwan)
    public static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";
    public static final String NAME_SPEED_DRIFTERS = "Speed Drifters";
    public static final String VER_SPEED_DRIFTERS = "1.54.0.26207";

    // Supported Game List
    public static final List<String> SUPPORTED_GAMES = Arrays.asList(
        PKG_QQ_SPEED,
        PKG_SPEED_DRIFTERS,
        "com.tencent.tmgp.speedmobile",
        "com.garena.game.fctw",
        "com.miHoYo.GenshinImpact",
        "com.tencent.tmgp.pubgmhd",
        "com.mobile.legends",
        "com.dts.freefireth",
        "com.activision.callofduty.shooter"
    );

    // Refresh Rate Targets
    public static final int REFRESH_RATE_DEFAULT = 60;
    public static final int REFRESH_RATE_90 = 90;
    public static final int REFRESH_RATE_120 = 120;

    // Game-Specific Optimizations
    public static GameInfo getGameInfo(String packageName) {
        if (PKG_QQ_SPEED.equals(packageName)) {
            return new GameInfo(
                NAME_QQ_SPEED,
                PKG_QQ_SPEED,
                VER_QQ_SPEED,
                true,  // Anti-Bypass Loading
                true,  // iOS-like Smooth
                REFRESH_RATE_120,
                true,  // Force High Refresh
                true   // Graphic Optimization
            );
        } else if (PKG_SPEED_DRIFTERS.equals(packageName)) {
            return new GameInfo(
                NAME_SPEED_DRIFTERS,
                PKG_SPEED_DRIFTERS,
                VER_SPEED_DRIFTERS,
                true,  // Anti-Bypass Loading
                true,  // iOS-like Smooth
                REFRESH_RATE_120,
                true,
                true
            );
        }
        return null;
    }

    public static boolean isSupportedGame(String pkg) {
        return SUPPORTED_GAMES.contains(pkg);
    }

    public static class GameInfo {
        public String name;
        public String packageName;
        public String version;
        public boolean antiBypassLoading;
        public boolean iosLikeSmooth;
        public int targetRefreshRate;
        public boolean forceRefreshRate;
        public boolean optimizeGraphics;

        public GameInfo(String name, String packageName, String version,
                        boolean antiBypassLoading, boolean iosLikeSmooth,
                        int targetRefreshRate, boolean forceRefreshRate,
                        boolean optimizeGraphics) {
            this.name = name;
            this.packageName = packageName;
            this.version = version;
            this.antiBypassLoading = antiBypassLoading;
            this.iosLikeSmooth = iosLikeSmooth;
            this.targetRefreshRate = targetRefreshRate;
            this.forceRefreshRate = forceRefreshRate;
            this.optimizeGraphics = optimizeGraphics;
        }
    }
}
