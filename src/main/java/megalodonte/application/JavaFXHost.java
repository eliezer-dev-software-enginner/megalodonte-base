package megalodonte.application;

/**
 * Default shared launcher, used by the {@link MegalodonteApp#run} overloads that
 * don't take a {@code Class<? extends Application>}. Every app relying on this
 * default reports the same WM_CLASS on Linux (see {@link MegalodonteApplication}'s
 * javadoc) — extend {@link MegalodonteApplication} directly with your own subclass
 * instead if you want a taskbar icon distinct from other megalodonte apps.
 */
public final class JavaFXHost extends MegalodonteApplication {
}
