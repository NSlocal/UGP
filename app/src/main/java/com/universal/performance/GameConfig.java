package com.universal.performance;

import java.util.Arrays;
import java.util.List;

public class GameConfig {
    public static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    public static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";
    
    public static final List<String> SUPPORTED_GAMES = Arrays.asList(
        PKG_QQ_SPEED, PKG_SPEED_DRIFTERS,
        "com.mobile.legends", "com.dts.freefireth", 
        "com.activision.callofduty.shooter", "com.miHoYo.GenshinImpact"
    );

    public static GameInfo getGameInfo(String pkg) {
        if (PKG_QQ_SPEED.equals(pkg)) {
            return new GameInfo(true, true, 120, true, true, "QQ Speed v1.58.0");
        } else if (PKG_SPEED_DRIFTERS.equals(pkg)) {
            return new GameInfo(true, true, 120, true, true, "Speed Drifters v1.54.0");
        }
        return null;
    }

    public static boolean isSupportedGame(String pkg) {
        return SUPPORTED_GAMES.contains(pkg);
    }

    public static class GameInfo {
        public boolean antiBypass, iosSmooth, forceRefresh, optimizeGfx;
        public int targetRate;
        public String name;
        
        public GameInfo(boolean a, boolean b, int c, boolean d, boolean e, String n) {
            antiBypass = a; iosSmooth = b; targetRate = c; 
            forceRefresh = d; optimizeGfx = e; name = n;
        }
    }
}
