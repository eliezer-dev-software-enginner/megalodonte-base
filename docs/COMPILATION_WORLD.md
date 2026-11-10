# Incremental compilation world

## Meaning of the messaging proposal

The JSON boundary belongs between the real project and an isolated compilation
world. It is not a serialization of a Minastex screen, JavaFX nodes, or business
objects. Application code remains normal Java and can differ completely between
Megalodonte projects.

The first request maps the project with paths, content hashes, Java lexical token
hashes, declared types and imports. Later requests contain only added, changed
and deleted entries. File contents live in a local content-addressed blob store;
JSON references their hashes. This avoids Base64 inflation for images, fonts and
other resources and prevents `.env` values from entering protocol logs.

The response should **not** send the whole application back. It contains status,
diagnostics, affected outputs and hashes for compiled classes/resources. The
runtime consumes the frozen output generation directly. Sending the full project
in both directions would duplicate I/O without improving activation safety.

## Granularity

Token comparison gives precise change detection and useful symbol metadata. It
does not make an individual Java field or statement an independently loadable
artifact. `javac` emits class files and may need to recompile dependent source
units. Therefore the layers intentionally have different granularities:

1. detect and classify changes by file, token and symbol;
2. let Gradle/javac compile changed Java units and affected dependents;
3. activate changed class files in one runtime transaction when JVM rules allow;
4. require a process/generation replacement for structural changes unsupported
   by standard `Instrumentation`.

The world mirrors project inputs while retaining timestamps of identical blobs.
Gradle runs with its persistent daemon and incremental analysis, but its output
is redirected to the private session. A failed build never replaces live output.

## Inputs and restart policy

`src` and custom in-project Java/resource source directories are compiled from
the mirror. Resource files are ordinary binary blobs and participate in output
generations. Cached application resources may still require explicit invalidation.
Build scripts and dependency metadata are fingerprinted and evaluated by Gradle;
dependency or launch changes require runtime replacement.

Environment files are represented only by path, size and hash. Their contents
are never copied into the compilation mirror or JSON. A change reports
`RESTART_REQUIRED`, matching the expectation that configuration is read during
bootstrap. Stopping and starting development adopts it; automatic restart remains
an explicit opt-in because it loses in-memory state.

## What remains unsolved

The standard JVM cannot redefine class shape: fields, methods, inheritance and
class additions/removals need another activation mechanism. Tokenization improves
compilation cost and diagnosis but does not remove that JVM boundary. Keeping the
same native windows through a structural replacement still requires a stable host
boundary with an explicit state/UI contract, an enhanced VM, or a carefully
isolated generation loader. Raw memory blocks and arbitrary Java object graphs
cannot be copied safely between class versions.

The current native-host prototype is a separate experiment and must not be
presented as the meaning of this compilation protocol. No application-specific
WorkerMain is necessary for project mapping. A future worker entry should be
generated only after its activation/state contract is general enough for arbitrary
Megalodonte applications, rather than embedding assumptions from one consumer.
