module megalodonte.base {
    requires java.desktop;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    requires org.slf4j;

    exports megalodonte.application;
    exports megalodonte.base;
    exports megalodonte.base.async;
    exports megalodonte.base.components;
    exports megalodonte.base.route;
    exports megalodonte.base.route.v2;
    exports megalodonte.base.scale;
    exports megalodonte.base.state;
    exports megalodonte.base.theme;
    exports megalodonte.base.v2;
    exports megalodonte.utils;
}