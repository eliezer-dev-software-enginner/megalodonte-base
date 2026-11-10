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
effects. Initial workers must avoid effects until they become active; future
production integration needs an explicit activation and quiescence contract.

## Roadmap

- [ ] Implement and finally validate protocol, host, worker and two-window test.
- [ ] Integrate native component factories and event callbacks with stable identities.
- [ ] Add business-state checkpoint/migration, effect quiescence and activation.
- [ ] Preserve focus, cursor, scroll, selection, owner/modality and custom windows.
- [ ] Add component coverage, themes, images/assets and capability negotiation.
- [ ] Integrate an opt-in CLI mode, compilation and generation supervision.
- [ ] Test real applications before claiming transparent support for Minastex.

## Progress

- Diagnosis recorded; implementation starting. No tests run for this stage yet.
