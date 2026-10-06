package megalodonte.platform.javafx;

import javafx.scene.Scene;
import javafx.stage.Screen;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumSet;
import megalodonte.base.scale.ScaleProvider;
import megalodonte.base.theme.ThemeManager;
import megalodonte.base.theme.ThemePlatform;
import megalodonte.contracts.BackendContract;
import megalodonte.contracts.Capability;

public final class JavaFxPlatform {
    private JavaFxPlatform() { }
    public static BackendContract contract() {
        return new BackendContract("javafx", BackendContract.CURRENT_VERSION, EnumSet.of(
            Capability.TEXT, Capability.BUTTON, Capability.CONTAINER, Capability.REACTIVE_STATE, Capability.THEME_TOKENS,
            Capability.SPACING, Capability.INPUT, Capability.CHECKBOX, Capability.SELECT, Capability.DATE_PICKER,
            Capability.IMAGE, Capability.PROGRESS, Capability.ROW, Capability.COLUMN, Capability.STACK, Capability.SCROLL,
            Capability.CARD, Capability.GRID, Capability.TABLE, Capability.MENU, Capability.MODAL,
            Capability.CONDITIONAL_RENDERING, Capability.LIST_RENDERING, Capability.ROUTING, Capability.ASYNC_SCOPE,
            Capability.CUSTOM_FONTS, Capability.DESKTOP_WINDOWS, Capability.JAVAFX_NODE, Capability.JAVAFX_CSS,
            Capability.KEYBOARD_SHORTCUTS));
    }
    public static void initialize() {
        ScaleProvider.setDetector(() -> Screen.getPrimary().getOutputScaleX());
        ThemePlatform.install(JavaFxFontLoader::loadAll, target -> {
            if (!(target instanceof Scene scene)) throw new IllegalArgumentException("JavaFX scene required");
            String family = ThemeManager.theme().typography().fontFamily();
            String css = ".root { -fx-font-family: \"" + family.replace("\"", "\\\"") + "\"; }";
            scene.getStylesheets().add("data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8)));
        });
    }
}
