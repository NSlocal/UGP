package com.universal.performance;

import java.util.Arrays;
import java.util.List;

public class GameConfig {
    public static final String PKG_QQ_SPEED = "com.tencent.tmgp.speedmobile";
    public static final String PKG_SPEED_DRIFTERS = "com.garena.game.fctw";
    public static final List<String> SUPPORTED = Arrays.asList(PKG_QQ_SPEED, PKG_SPEED_DRIFTERS);
    
    public static boolean isSupported(String pkg) {
        return SUPPORTED.contains(pkg);
    }
}
