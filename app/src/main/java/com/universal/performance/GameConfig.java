package com.universal.performance;

import java.util.Arrays;
import java.util.List;

public class GameConfig {
    public static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    public static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";
    public static final List<String> SUPPORTED_GAMES = Arrays.asList(
        PKG_QQ_SPEED, PKG_SPEED_DRIFTERS, "com.miHoYo.GenshinImpact",
        "com.mobile.legends", "com.dts.freefireth", "com.activision.callofduty.shooter"
    );

    public static GameInfo getGameInfo(String pkg) {
        if (PKG_QQ_SPEED.equals(pkg) || PKG_SPEED_DRIFTERS.equals(pkg)) {
            return new GameInfo(true, true, 120, true, true);
        }
        return null;
    }

    public static boolean isSupportedGame(String pkg) {
        return SUPPORTED_GAMES.contains(pkg);
    }

    public static class GameInfo {
        public boolean antiBypass, iosSmooth, forceRefresh, optimizeGfx;
        public int targetRate;
        public GameInfo(boolean a, boolean b, int c, boolean d, boolean e) {
            antiBypass = a; iosSmooth = b; targetRate = c; forceRefresh = d; optimizeGfx = e;
        }
    }
}
