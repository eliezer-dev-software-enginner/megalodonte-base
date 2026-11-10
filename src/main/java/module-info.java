module megalodonte.base {
    requires static java.instrument;
    requires transitive megalodonte.core;
    requires java.desktop;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    requires org.slf4j;
    requires com.google.gson;
    exports megalodonte.application.messaging;
    opens megalodonte.application.messaging to com.google.gson;

    exports megalodonte.application;
    exports megalodonte.application.reload;
    exports megalodonte.base;
    exports megalodonte.base.async;
    exports megalodonte.base.components;
    exports megalodonte.base.route;
    exports megalodonte.base.route.v2;
    exports megalodonte.platform.javafx;
    exports megalodonte.base.v2;
    exports megalodonte.utils;
}
