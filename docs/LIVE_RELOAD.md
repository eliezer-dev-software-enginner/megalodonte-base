# Desktop live reload

Run a native desktop application with the updated Megalodonte CLI:

```sh
megalodonte dev --project .
# When all dependencies are already cached:
megalodonte dev --project . --offline
```

Requires JDK 25, a graphical session, the Gradle `application` plugin, and matching
updated `megalodonte-core` and `megalodonte-base` artifacts. Applications using
`megalodonte-reactivity` also need its updated artifact for subscription cleanup.
Publish libraries to Maven Local in that order. Existing applications do not need
changes to `main` or their render methods when they use `Context.useView` or
`ScreenManager` routes. Install/update the CLI separately as described in its README.
This is a development feature: a regular `gradlew run` or packaged application does
not enable the agent or the file watcher.

## What the isolated world does

The project-to-world messaging model, token/file/class granularities, resources
and `.env` policy are specified in [COMPILATION_WORLD.md](COMPILATION_WORLD.md).
JSON describes a content-addressed project snapshot and subsequent deltas; it is
not a serialized UI or application object graph.

### Persistent window host: design evolution

Keeping the same native windows open is a user requirement even when ordinary
class redefinition fails. Automatic process restart does not satisfy it.

The initial alternative considered a permanent host owning JavaFX, the Stage/Scene registry
and a stable framework bridge, with application code loaded as replaceable
generations. Compatible changes keep the existing redefine path. Structural
changes prepare a new generation in an isolated class loader, reconstruct the
registered screens and transfer explicitly supported state through a neutral,
versioned data contract. Existing application object instances cannot simply be
cast to classes from the new generation. Framework APIs crossing that boundary
must remain in the host loader.

Prepare every affected window before swapping any roots on the FX thread. If
preparation or state conversion fails, keep the old generation and all its
windows active. A removed screen with no replacement requires an explicit
mapping or rejection, never silently closing its window. After a successful
swap, detach old callbacks and stop owned jobs/listeners; do not rerun arbitrary
startup side effects. This needs explicit lifecycle ownership and does not make
network/database effects transactional.

Window identity can be preserved by this design, but arbitrary application
state cannot be promised. Changes to the host, JavaFX or native dependencies,
and a crashed JVM, remain outside this fallback. A separate worker process
could isolate application crashes only with an additional declarative UI/event
protocol; JavaFX Node objects cannot be transferred between processes.

The class-loader alternative above was not implemented. The chosen follow-up is
a separate worker-process prototype with internal JSON messaging, now available
through an explicit Java API and opt-in CLI mode. See [native messaging status](NATIVE_MESSAGING.md).
Existing application screens do not automatically support generation replacement.

### Current implementation

1. The CLI watches source/resource directories and Gradle configuration, caches file
   hashes, and waits for a quiet interval (`--debounce`, default 0.6 seconds). It
   records an initial JSON project manifest and later JSON deltas with Java token
   and symbol metadata. Contents use a local hash-addressed blob store.
2. Gradle compiles mirrored sources/resources to a private temporary build directory using its incremental
   compiler and daemon. It does not run `clean`, tests, packaging or application
   startup on every edit. The project's normal `build/` is not used.
3. A successful generation freezes class/resource output. The running JVM reads a
   separate active copy and frozen dependency JARs. Failed builds and results whose
   source changed during compilation do not reach the running application.
4. A startup `-javaagent` compares class bytes and uses the JVM's
   `Instrumentation.redefineClasses` in one batch. Only changed classes are sent.
   Classes are loaded without initialization before replacement to retain their
   previous definitions for rollback; application class loaders are not replaced.
5. On the JavaFX thread, every framework-managed screen prepares a new root using
   its existing screen instance. The framework keeps an explicit weak registry of
   primary and spawned stages; refresh does not depend on JavaFX's global window
   enumeration. Roots are committed only after every render succeeds. Stages and
   Scenes remain, retaining window size, position and title.

The command prints `APPLIED`, `REJECTED`, or `RESTART_REQUIRED`, plus class and
window counts on success. Ctrl+C stops the development child and removes the
temporary session. A hung UI cannot acknowledge a refresh: the CLI reports that
it is waiting instead of timing out and later applying an abandoned transaction.
There are no network listeners or runtime agent attachment requirements.

The “world” is an artifact staging area, **not a security sandbox or a second JVM
whose memory can be copied into the client**. Build scripts and application code
still have their normal permissions and side effects.

The compiler response carries status and artifact metadata, not the entire project
again. `.env` contents never enter the mirror/protocol; a changed environment file
requires restart. Resources are mirrored as binary blobs and processed normally.

## State and failure recovery

Screen fields, ViewModels and `State` objects survive compatible redefinition.
`onMount`, constructors, static initializers and `onDestroy` are not rerun during
refresh. Keep persistent UI values in fields or a ViewModel. Cursor position,
unbound text, focus, scroll offsets and other control-local state are not copied
from the discarded JavaFX nodes.

Subscriptions created synchronously during framework-managed `render()` are
captured by `SubscriptionScope`: old render subscriptions close after a successful
commit; candidate subscriptions close if rendering fails. This supports core
`State`/mapped state and updated reactivity `ComputedState`/`ListState` registrations.
Subscriptions created later by asynchronous work, custom state implementations,
timers and external resources still require their own lifecycle cleanup. Do not
create persistent ViewModel subscriptions inside `render()`.

If a candidate fails, previous bytecode and runtime files are restored and old
roots remain. If rollback itself fails, the supervisor stops the session rather
than accepting further generations. `render()` must construct a fresh, unattached
root and avoid domain side effects. Rollback cannot undo database writes, network
requests or arbitrary state mutations performed by application code. Other
threads are not transactionally paused: already-active methods may finish using
old bytecode. This mechanism is not a transaction over the whole application.

## Supported changes and explicit restart

| Change | Behavior |
| --- | --- |
| Method bodies with unchanged class structure | Redefined in the existing JVM; all registered visible windows rerender |
| Resources read again during render | Updated in the active copy before rerender; cached images/fonts/static data may still need restart |
| Compile error or failed render | Existing version kept; failed render rolls back bytecode/resources |
| Added/deleted class files | Restart required, including anonymous-class changes |
| New/removed fields or methods, changed signatures/inheritance | JVM may reject; restart required on the standard JVM |
| Added, removed, upgraded or reordered external dependencies | Resolved in the shadow build, then restart required; never appended silently to the live class loader |
| Constructor, static initializer, bootstrap or route-table setup changes | Existing objects are not reinitialized; restart to observe initialization changes |

Adding a lambda can add a synthetic method or class, so even a small source edit
can require restart. Compilation granularity is Gradle's dependency analysis,
not arbitrary Java statements. Changed JARs at the same path are detected by
content when preparing another generation.

By default an incompatible change leaves the current process untouched. Stop and
start `megalodonte dev` to adopt it. To explicitly permit restarts:

```sh
megalodonte dev --project . --restart-on-incompatible
```

This option loses in-memory state, recreates windows from application startup and
uses process termination (not the application's window-close handler). Persist
work before opting in. There is no automatic arbitrary-object migration.

Current launch support is single-project, non-modular native desktop applications
using the standard Gradle `application` launch configuration. JavaFX modules use
the module path; application classes use the system class loader. Custom module
paths, custom class loaders, multi-project output and arbitrary task-specific
`run.doFirst` logic are not supported. JSONC/script renderer cache invalidation is
not integrated; use its own renderer facilities instead of assuming this feature
refreshes cached declarative documents. Custom windows outside `Context` and
`ScreenManager` require framework integration before they participate.

## Validation and JVM references

`./gradlew test liveReloadTest` validates normal contracts and an actual agent JVM
with two JavaFX route windows: changed render bytecode, retained state/Scene,
single mount, subscription cleanup, failed-render rollback, incompatible fields
and a subsequent successful reload. `liveReloadTest` requires a display and fails
explicitly if that environment is missing; it takes no screenshots.

The CLI provides `PYTHONPATH=src python tests/integration_live_reload.py` for a
disposable generated consumer, incremental shadow builds, compile-error recovery
and dependency-change rejection. It does not test unrelated user applications.

The limits follow the [JDK 25 Instrumentation contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.instrument/java/lang/instrument/Instrumentation.html#redefineClasses(java.lang.instrument.ClassDefinition...))
and [JVMTI RedefineClasses](https://docs.oracle.com/en/java/javase/25/docs/specs/jvmti.html#RedefineClasses).
