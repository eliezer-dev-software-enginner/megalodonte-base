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
import megalodonte.router.v4.Router;

public class Main {
    static boolean devMode = true;

    public static void main(String[] args) {
        MegalodonteApp.run(context -> {

            context.javafxStage().setTitle("My App");
            context.javafxStage().setWidth(900);
            context.javafxStage().setHeight(650);

            context.useRouter(AppRouter.build()).start();

            MegalodonteApp.onShutdown(() -> {
                System.out.println("Application closed");
            });
        });
    }
}
```

## Real World Example

### Main.java

```java
import megalodonte.application.MegalodonteApp;
import megalodonte.application.Context;
import megalodonte.router.v4.Router;
import my_app.AppRouter;

public class Main {
    static HotReload hotReload;
    static boolean devMode = true;

    public static void main(String[] args) {
        MegalodonteApp.run(context -> {

            var stage = context.javafxStage();
            stage.setTitle("My App");
            stage.setWidth(900);
            stage.setHeight(650);

            context.useRouter(AppRouter.build()).start();

            if (devMode) {
                hotReload = new HotReload()
                        .sourcePath("src/main/java")
                        .classesPath("build/classes/java/main")
                        .screenClassName("my_app.screens.homescreen.HomeScreen")
                        .reloadContext(context)
                        .useRouter();
                hotReload.start();
            }

            MegalodonteApp.onShutdown(() -> {
                if (hotReload != null) hotReload.stop();
            });
        });
    }
}
```

### AppRouter.java

```java
import megalodonte.router.v4.Router;
import megalodonte.router.v4.RouteProps;
import my_app.screens.HomeScreen;
import my_app.screens.SettingsScreen;
import my_app.screens.ProductsScreen;

public class AppRouter {
    public static Router build() {
        var routes = Set.of(
            new Router.Route("home", ctx -> new HomeScreen(ctx),
                new RouteProps(900, 550, null, false)),

            new Router.Route("settings", ctx -> new SettingsScreen(ctx),
                new RouteProps(900, 550, "Settings", true)),

            new Router.Route("products", ctx -> new ProductsScreen(ctx),
                new RouteProps(900, 550, "Products", true)),

            new Router.Route("product/${id}", ctx -> new ProductDetails(ctx),
                new RouteProps(900, 550, "Product Details", true))
        );

        return new Router(routes, "home");
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

megalodonte.router.v4/
    ├── Router            # Route definitions
    └── RouteProps        # Route properties (size, title, etc)

megalodonte.base/
    ├── theme/            # Theme configuration (ThemeInterface, ThemeColors, ThemeTypography, ThemeSpacing, ThemeBorder)
    ├── components/       # UI components
    ├── async/            # Async utilities
    ├── UI.java           # UI thread helpers
    └── Redirect.java     # Navigation helper
```

---

## Routing

Define routes using `Router.Route` with `RouteProps`:

```java
var routes = Set.of(
    new Router.Route("home", ctx -> new HomeScreen(ctx),
        new RouteProps(900, 550, null, false)),

    new Router.Route("settings", ctx -> new SettingsScreen(ctx),
        new RouteProps(900, 550, "Settings", true)),

    new Router.Route("product/${id}", ctx -> new ProductDetails(ctx),
        new RouteProps(900, 550, "Product Details", true))
);

Router router = new Router(routes, "home");
```

Route parameters are supported via `${param}` syntax.

Use `context.getRouter().navigateTo()` to navigate between screens:

```java
context.getRouter().navigateTo("settings");
context.getRouter().navigateTo("product/123");
```

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
            "#FFFFFF", "#F5F5F5", "#1976D2", "#FF5722",
            "#212121", "#757575", "#E0E0E0"
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
Async.run(() -> {
    // Background task
    return fetchData();
}, result -> {
    // Callback on completion
    UI.runOnUi(() -> {
        // Update UI
    });
});
```

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

In `megalodonte-router` v4, every `ScreenContext` owns one of these automatically, and `Router`
cancels it right before calling `onDestroy()` — see that module's README for `ctx.scope()`.

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
 │   │   ├── UI.java
 │   │   ├── Redirect.java
 │   │   └── Utility.java
 │   └─ utils/
 │
 └─ test/java/megalodonte/
```

---

## License

MIT License
