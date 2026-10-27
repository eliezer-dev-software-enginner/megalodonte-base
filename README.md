# Megalodonte Base

A lightweight JavaFX framework for building desktop applications with a **React-inspired architecture**, featuring routing, theming, components, and async utilities.

State, theme and scale types come from `megalodonte-core` through this library.
Publish core to Maven Local before publishing base. Bootstrap installs the JavaFX
scale and font adapters. Consumers built against earlier module layouts need recompilation.

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

Use JDK 25. The included Gradle wrapper downloads Gradle 9.2.0. Publish the
matching `megalodonte-core:1.0.0-beta` artifact to Maven Local first; it is a
required API dependency. From this repository root, run:

```bash
./gradlew publishToMavenLocal
```

Add this library to your project:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("megalodonte:megalodonte-base:1.0.0-beta")
}
```

The routing examples also require a compatible `megalodonte-router` dependency,
which is maintained separately. Its coordinates and setup must be taken from
that module; this repository does not declare a router version.

---

## Quick Start

The following routing examples require the separate `megalodonte-router` module
and application screen classes (`HomeScreen`, `SettingsScreen`, `ProductsScreen`,
and `ProductDetails`). Those classes are placeholders to implement in your own
application; the snippets do not run as a standalone project. `AppRouter.build()`
is defined in the example below.

```java
import megalodonte.application.MegalodonteApp;
import my_app.AppRouter;

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
import java.util.Set;
import my_app.screens.HomeScreen;
import my_app.screens.SettingsScreen;
import my_app.screens.ProductsScreen;
import my_app.screens.ProductDetails;

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
import megalodonte.base.route.Route;
import megalodonte.base.route.RouteProps;
import megalodonte.base.route.RouteTable;
import megalodonte.router.v5.Router;
import java.util.Set;

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

See `megalodonte-router`'s README for the full navigation API. The screen context's
`ctx.scope()` is cancelled when that screen is destroyed; running tasks must check
`isCancelled()` or register cleanup with `onCancel()`.

Use `Redirect.to()` to open a URL in the user's browser:

```java
import megalodonte.base.Redirect;

Redirect.to("https://github.com");
```

---

## Theming

Theme contracts and `ThemeManager` are supplied by the transitive
`megalodonte-core` dependency. This library installs JavaFX font and scale
adapters during bootstrap and applies the current theme font to new scenes.
Consult the matching core version for the theme type constructors and API.

---

## Async Operations

```java
import megalodonte.base.UI;
import megalodonte.base.async.Async;

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
screen that kicked it off is gone by the time it finishes. A one-shot fetch must also check
whether its screen is still active before updating the UI. A task that opens a resource
(a socket, a serial port, a listener) also needs cleanup if the screen is destroyed
while that resource is being set up.

`Scope` gives that kind of work a cancellable handle instead:

```java
import megalodonte.base.UI;
import megalodonte.base.async.Scope;

Scope scope = new Scope();

scope.run(() -> {
    var connection = openLongLivedConnection();
    scope.onCancel(connection::close); // fires now if already cancelled, or later when cancel() runs
    if (scope.isCancelled()) return;   // don't start using a connection we just closed

    connection.listen(data -> UI.runOnUi(() -> {
        if (!scope.isCancelled()) state.set(data);
    }));
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
- JavaFX 25.0.1
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
 │   │   ├── components/
 │   │   ├── async/
 │   │   │   ├── Async.java
 │   │   │   └── Scope.java
 │   │   ├── route/
 │   │   │   ├── Route.java
 │   │   │   ├── RouteProps.java
 │   │   │   ├── RouteTable.java
 │   │   │   └── v2/
 │   │   ├── UI.java
 │   │   └── Redirect.java
 │   ├─ platform/javafx/
 │   └─ utils/
 │
 └─ test/java/megalodonte/
```

---

## License

No license file is present in this repository. The licensing terms require
confirmation from the project maintainers.
