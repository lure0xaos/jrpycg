module RPyCG {
    requires kotlin.stdlib;
//    requires kotlin.stdlib.jdk7;
//    requires kotlin.stdlib.jdk8;
    requires kotlin.reflect;

    requires java.desktop;
    requires java.prefs;
    requires java.logging;

    exports com.github.lure0xaos.jrpycg;
    exports com.github.lure0xaos.util.log.jul to java.logging;

    exports com.github.lure0xaos.util.pref.file to java.prefs;
    exports com.github.lure0xaos.util.pref.memory to java.prefs;
}
