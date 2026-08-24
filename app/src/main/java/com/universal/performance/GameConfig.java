package com.universal.performance;

import java.util.Arrays;
import java.util.List;

public class GameConfig {
    public static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    public static final String NAME_QQ_SPEED = "QQ Speed / QQ飞车";
    public static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";
    public static final String NAME_SPEED_DRIFTERS = "Speed Drifters";

    public static final List<String> SUPPORTED_GAMES = Arrays.asList(
        PKG_QQ_SPEED, PKG_SPEED_DRIFTERS,
        "com.miHoYo.GenshinImpact", "com.mobile.legends",
        "com.dts.freefireth", "com.activision.callofduty.shooter"
    );

    public static GameInfo getGameInfo(String packageName) {
        if (PKG_QQ_SPEED.equals(packageName)) {
            return new GameInfo(NAME_QQ_SPEED, PKG_QQ_SPEED, true, true, 120, true, true);
        } else if (PKG_SPEED_DRIFTERS.equals(packageName)) {
            return new GameInfo(NAME_SPEED_DRIFTERS, PKG_SPEED_DRIFTERS, true, true, 120, true, true);
        }
        return null;
    }

    public static boolean isSupportedGame(String pkg) {
        return SUPPORTED_GAMES.contains(pkg);
    }

    public static class GameInfo {
        public String name, packageName;
        public boolean antiBypassLoading, iosLikeSmooth, forceRefreshRate, optimizeGraphics;
        public int targetRefreshRate;

        public GameInfo(String name, String packageName, boolean antiBypassLoading,
                        boolean iosLikeSmooth, int targetRefreshRate, boolean forceRefreshRate,
                        boolean optimizeGraphics) {
            this.name = name;
            this.packageName = packageName;
            this.antiBypassLoading = antiBypassLoading;
            this.iosLikeSmooth = iosLikeSmooth;
            this.targetRefreshRate = targetRefreshRate;
            this.forceRefreshRate = forceRefreshRate;
            this.optimizeGraphics = optimizeGraphics;
        }
    }
}
