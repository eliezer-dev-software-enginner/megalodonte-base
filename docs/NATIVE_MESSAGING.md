# Native development host over messaging

## Diagnosis and decision

The current reload agent edits compatible method bodies in one JVM. Restarting
that JVM for structural changes destroys its native windows. Keeping a screenshot
or reopening a window does not meet the requirement of retaining the same Stage.

Use a persistent JavaFX host and replaceable application worker processes. Java
remains the authoring language. JSON is an internal wire protocol, not JSONC view
authoring and not serialized Java objects. The host owns windows, controls and
input state; workers own application behavior. Separate processes also allow
worker dependencies and class structure to change without restarting the host.
Host/JavaFX/native-library upgrades still require a host restart.

## Delivery and validation policy

User instruction for this implementation: develop and document cohesive increments,
make local commits, and run tests only at the end of the implementation stage.
Intermediate commits are explicitly unvalidated. This is a task-scoped batching
policy, not permission to omit final validation or a universal testing rule.
No screenshots and no repeated tests of unrelated applications.

## First implementation stage

Build an experimental typed Java messaging API, bounded/versioned JSON transport,
a JavaFX host retaining Stage/Scene identities, a replaceable subprocess worker,
and a two-window integration test. Native controls start with rows, columns,
text, buttons and text inputs. Application code must use this explicit prototype
API; existing component constructors are not transparently intercepted.

Protocol: host sends HELLO with generation and a snapshot of input values; worker
sends SNAPSHOT with all windows; host sends EVENT with component/window identity;
worker sends subsequent SNAPSHOT updates. Every direction has increasing sequence
numbers and every message carries protocol version and generation. Stdout is
reserved for protocol; worker logging uses stderr. Messages have a size limit.

During replacement, input in existing windows is disabled briefly. Build the entire
candidate UI before changing any current root. Keep old windows and worker until
the candidate is accepted. Reject missing existing windows, unsupported controls,
invalid identities, malformed messages and stale generations. Restore interaction
if preparation fails. Only after committing the candidate retire the old worker.

Input values are carried across generations by stable window/component IDs.
Arbitrary business state, in-flight operations, scroll/focus, effects and resources
are NOT automatically migrated. Worker termination does not undo database/network
effects. Initial workers must avoid effects until they become active; production integration still needs business-state checkpointing and background-effect quiescence; the prototype already gates activation and waits for UI-event acknowledgments.

## Roadmap

- [x] Implement and finally validate protocol, host, worker and two-window test.
- [ ] Integrate native component factories and event callbacks with stable identities.
- [ ] Add business-state checkpoint/migration, effect quiescence and activation.
- [ ] Preserve focus, cursor, scroll, selection, owner/modality and custom windows.
- [ ] Add component coverage, themes, images/assets and capability negotiation.
- [ ] Integrate an opt-in CLI mode, compilation and generation supervision.
- [ ] Test real applications before claiming transparent support for Minastex.

## Progress

- Diagnosis recorded and committed before implementation.
- Protocol, typed Java API, subprocess host and integration fixtures written.
  Intermediate source commit: not yet compiled or tested, per the batching plan.
- The standalone launcher accepts a worker executable and arguments; its prototype
  session ends when any window is closed. Existing application entry points are
  not converted automatically. The host API supports replacing a worker later.

## Experimental API

`NativeUi` provides Java records and builders. `NativeWorker.Application.prepare`
returns a complete snapshot; `onEvent` returns another snapshot or null for ACK.
`activated` is called only after the host accepts the initial snapshot. Application
logs go to stderr. Workers must not print ordinary logs to stdout.

```java
import megalodonte.application.messaging.NativeWorker;
import megalodonte.application.messaging.NativeUi.*;
import static megalodonte.application.messaging.NativeUi.*;
import java.util.List;

class WorkerMain {
    static void main() throws Exception {
        NativeWorker.run(new NativeWorker.Application() {
            int clicks;
            public Snapshot prepare(State state) { return screen(); }
            public Snapshot onEvent(Event event) { clicks++; return screen(); }
            private Snapshot screen() {
                return new Snapshot(List.of(new Window("main", "Demo", 400, 240,
                    column("root", text("count", "Clicks: " + clicks),
                        button("increment", "Increment")))));
            }
        });
    }
}
```

A host integration calls `NativeWindowHost.replaceWorker(command)` on the FX
thread and observes its future. It retains all existing Stage and Scene instances
when accepting another worker. `readyForReplacement()` reports whether all sent
UI events were acknowledged; replacement is rejected while events are pending.
Initial snapshots cannot silently remove open windows. A disconnected active
worker leaves the last UI visible but disabled; a subsequent replacement can
recover it. Candidate failure leaves the previous worker active.

The first renderer reconstructs roots, so it does not preserve control instances,
focus, cursor or scroll. It preserves user-edited text through stable IDs. This is
not a remote wrapper around every existing Megalodonte component. There is no CLI
watcher integration yet. This stage does not claim compatibility with Minastex.

Final validation commands: `./gradlew test nativeMessagingTest publishToMavenLocal`.
`nativeMessagingTest` requires a real display and launches worker JVMs; it checks
two Stage/Scene identities, typed input, button routing, invalid candidates,
missing windows, malformed protocol, timeout, worker death and recovery.


### Final validation of the first stage

Passed 10 unit tests and the display-dependent integration test (no skipped tests),
covering two unchanged Stage/Scene identities across worker replacement, invalid
candidates, missing windows, malformed messages, timeout and recovery after worker
exit. `publishToMavenLocal` succeeded. A temporary standalone consumer compiled
the documented Java worker against the published JAR and handled HELLO/ACTIVATE/
EVENT successfully. No screenshots or unrelated application tests were run.

The first attempt was blocked by an obsolete Gradle daemon's JDK spawn helper;
validation succeeded with `--no-daemon`. This was an environment issue, not a
passing test. Next stage: opt-in CLI compilation and host supervision, with its
tests likewise deferred until the stage is implemented.

## CLI supervision stage (implementation written; validation pending)

The opt-in CLI command is:

```sh
megalodonte dev --native-host --worker-main your.package.WorkerMain
```

This entry class must implement the NativeWorker protocol above; passing an
existing ordinary application Main does not convert it. The CLI compiles app
classes in the temporary world, freezes each worker generation and its JARs,
and sends REPLACE to a permanent NativeHostSupervisor JVM. It never automatically
restarts the host. Stage/Scene identity diagnostics accompany accepted responses.

Worker-only dependencies can change between generations. Host dependencies
(base/core/Gson/SLF4J/JavaFX), Java executable and JVM arguments are pinned; a
change is rejected with the existing windows retained. Worker cwd, environment
and application arguments are forwarded explicitly. Stdout is protocol in both
channels; logs remain on stderr. Closing a window ends this prototype session.

Old generation files remain until session shutdown to avoid deleting bytecode
while a retired worker is still exiting; long sessions consume additional disk
space. Ctrl+C closes the host, which terminates workers through normal cleanup
and a shutdown hook. Native host crash/forced OS kill is outside window retention.

The new integration fixture edits a disposable Java worker, adds/removes a class
and adds a field, checks worker PID replacement and identical host/Stage/Scene,
then checks compile errors, invalid candidates and recovery. Tests for this stage
are deferred to its final validation, following the user-requested cadence.
