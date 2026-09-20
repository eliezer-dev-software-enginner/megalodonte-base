# Megalodonte Base

A lightweight JavaFX framework for building desktop applications with a **React-inspired architecture**, featuring routing, theming, components, and async utilities.

---

## Features

- **Routing System** - Simple navigation between screens
- **Theme System** - Customizable colors, typography, spacing, borders, and radius
- **Component Architecture** - React-inspired component model
- **Async Utilities** - Easy async operations
- **Scope** - Lifecycle-bound cancellation for async work
- **Bootstrap Lifecycle** - Clean application startup

---

## Installation (Maven Local)

```bash
./gradlew publishToMavenLocal
```

Add to your project:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("megalodonte:megalodonte-base:1.0.0-beta")
}
```

---

## Quick Start

```java
import megalodonte.application.MegalodonteApp;
import megalodonte.application.Context;
import megalodonte.router.v5.Router;

public class Main {

    public static void main(String[] args) {
        MegalodonteApp.run(context -> {

            context.javafxStage().setTitle("My App");
            context.javafxStage().setWidth(900);
            context.javafxStage().setHeight(650);

            context.useRouter(AppRouter.build()).start();
        });
    }
}
```

## Real World Example

### Main.java

```java
import megalodonte.application.MegalodonteApp;
import megalodonte.application.MegalodonteApplication;
import megalodonte.application.Context;
import megalodonte.router.v5.Router;
import my_app.AppRouter;

public class Main {
    public static class AppHost extends MegalodonteApplication {}

    public static void main(String[] args) {
        MegalodonteApp.run(AppHost.class, args, Main::start, Main::onEvent);
    }

    private static void start(Context context) {
        var stage = context.javafxStage();
        stage.setTitle("My App");
        stage.setWidth(900);
        stage.setHeight(650);

        context.useRouter(AppRouter.build()).start();
    }

    // MegalodonteApp.Event only has CloseRequest today - fired when the user
    // closes the window, so it's where you release anything the app opened
    // outside a screen's own lifecycle (DB connections, background processes).
    private static void onEvent(MegalodonteApp.Event event) {
        if (event == MegalodonteApp.Event.CloseRequest) {
            // close DB sessions, kill child processes, etc.
        }
    }
}
```

### AppRouter.java

```java
import megalodonte.base.route.Route;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouteTable;
import megalodonte.router.v5.Router;
import my_app.screens.HomeScreen;
import my_app.screens.SettingsScreen;
import my_app.screens.ProductsScreen;

public class AppRouter {
    public static Router build() {
        var routes = Set.of(
            new Route("home", ctx -> new HomeScreen(ctx),
                new RouteProps(900, 550, null, false)),

            new Route("settings", ctx -> new SettingsScreen(ctx),
                new RouteProps(900, 550, "Settings", true)),

            new Route("products", ctx -> new ProductsScreen(ctx),
                new RouteProps(900, 550, "Products", true)),

            new Route("product/${id}", ctx -> new ProductDetails(ctx),
                new RouteProps(900, 550, "Product Details", true))
        );

        return new Router(new RouteTable(routes, "home"));
    }
}
```

---

## Architecture

```
megalodonte.application/
    ├── MegalodonteApp    # Application entry point
    ├── Context           # Application context
    └── Bootstrap         # Initialization bootstrap

megalodonte.router.v5/ (separate module: megalodonte-router, navigation-only)
    └── Router            # navigateOnStage, navigateAndCloseOthers, mainStage

megalodonte.base/
    ├── theme/            # Theme configuration (ThemeInterface, ThemeColors, ThemeTypography, ThemeSpacing, ThemeBorder)
    ├── components/       # UI components
    ├── async/            # Async.Run + Scope (lifecycle-bound cancellation)
    ├── route/            # Route, RouteProps, RouteTable, ScreenManager, ScreenFactory + v2/ScreenContextBase — route storage, screen lifecycle and spawnWindow
    ├── UI.java           # UI thread helpers
    └── Redirect.java     # Open a URL in the system browser
```

---

## Routing

`megalodonte-base` stores the routes in a `RouteTable` and owns screen lifecycle and window
spawning; the router (v5) only navigates. Routes are defined with `Route` and `RouteProps`:

```java
var routes = Set.of(
    new Route("home", ctx -> new HomeScreen(ctx),
        new RouteProps(900, 550, null, false)),

    new Route("settings", ctx -> new SettingsScreen(ctx),
        new RouteProps(900, 550, "Settings", true)),

    new Route("product/${id}", ctx -> new ProductDetails(ctx),
        new RouteProps(900, 550, "Product Details", true))
);

RouteTable table = new RouteTable(routes, "home");
Router router = new Router(table);
```

Route parameters are supported via `${param}` syntax.

Screens receive a `ScreenContextInterface ctx` (from `megalodonte.base.route.v2`) in their
constructor — use it to navigate and spawn:

```java
ctx.navigate("settings");
ctx.navigate("product/123");
ctx.spawnWindow("settings");                 // open in a new window
ctx.spawnWindow("product/123", error -> {}); // with an error handler
```

See `megalodonte-router`'s README for the full navigation API (spawning windows, closing them,
reading route parameters, and automatic cancellation of in-flight work via `ctx.scope()`).

Use `Redirect.to()` to open a URL in the user's browser:

```java
Redirect.to("https://github.com");
```

---

## Theming

Create a custom theme by implementing `ThemeInterface`:

```java
public class MyTheme implements ThemeInterface {
    @Override
    public ThemeColors colors() {
        return new ThemeColors(
            "#FFFFFF", "#F5F5F5", "#1976D2", "#FF5722",  // background, surface, primary, secondary
            "#212121", "#757575", "#E0E0E0", "#9E9E9E",  // textPrimary, textSecondary, border, placeholder
            "#BBDEFB", "#90CAF9", "#EEEEEE"               // selection, focusRing, hover
        );
    }

    @Override
    public ThemeTypography typography() {
        return new ThemeTypography(24, 18, 14, 12);
    }

    @Override
    public ThemeSpacing spacing() {
        return new ThemeSpacing(4, 8, 16, 24, 32);
    }

    @Override
    public ThemeBorder border() {
        return new ThemeBorder(1, 4, 8, 16);
    }
}
```

Apply the theme via ThemeManager (from megalodonte-theme):

```java
ThemeManager.setTheme(new MyTheme());
```

---

## Async Operations

```java
Async.Run(() -> {
    var data = fetchData(); // runs on a virtual thread

    UI.runOnUi(() -> {
        // back on the JavaFX Application Thread — update state/UI here
    });
});
```

`Async.Run` is fire-and-forget: unhandled exceptions go to `ErrorReporter`, there's no result
callback and no handle to cancel it. For anything that opens a resource needing symmetric
teardown when a screen is destroyed, use `Scope` instead — see below.

---

## Scope (lifecycle-bound cancellation)

`Async.Run()` is fire-and-forget: it has no idea who started it, so nothing stops it if the
screen that kicked it off is gone by the time it finishes. That's fine for a one-shot "fetch and
update the UI" call, but it's a real bug if the task opens something that needs to be closed
(a socket, a serial port, a listener) — if the screen is destroyed while the task is still
setting that resource up, nothing is left to close it.

`Scope` gives that kind of work a cancellable handle instead:

```java
Scope scope = new Scope();

scope.run(() -> {
    var connection = openLongLivedConnection();
    scope.onCancel(connection::close); // fires now if already cancelled, or later when cancel() runs
    if (scope.isCancelled()) return;   // don't start using a connection we just closed

    connection.listen(data -> UI.runOnUi(() -> state.set(data)));
});

// later, when the owner (screen/ViewModel) goes away:
scope.cancel();
```

- `run(RunnableThrowing)` — only executes the task if the scope hasn't been cancelled yet.
- `onCancel(Runnable)` — registers cleanup for when `cancel()` runs; fires immediately if the
  scope is already cancelled.
- `cancel()` / `isCancelled()` — cheap, synchronous, idempotent.

Every `ScreenContextBase` (in `megalodonte-base`) owns one of these automatically, and
`ScreenManager` cancels it right before calling `onDestroy()` — see `megalodonte-router`'s
README for `ctx.scope()`.

---

## Technologies

- Java 25
- JavaFX 17
- JUnit 5
- Mockito
- TestFX
- Gradle

---

## Project Structure

```
src/
 ├─ main/java/megalodonte/
 │   ├─ application/
 │   │   ├── MegalodonteApp.java
 │   │   ├── MegalodonteApplication.java
 │   │   ├── Context.java
 │   │   ├── Bootstrap.java
 │   │   └── JavaFXHost.java
 │   ├─ base/
 │   │   ├── theme/
 │   │   │   ├── ThemeInterface.java
 │   │   │   ├── ThemeColors.java
 │   │   │   ├── ThemeTypography.java
 │   │   │   ├── ThemeSpacing.java
 │   │   │   └── ThemeBorder.java
 │   │   ├── components/
 │   │   ├── async/
 │   │   │   ├── Async.java
 │   │   │   └── Scope.java
 │   │   ├── UI.java
 │   │   └── Redirect.java
 │   └─ utils/
 │
 └─ test/java/megalodonte/
```

---

## License

MIT License
