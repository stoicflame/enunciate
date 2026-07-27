# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

Enunciate is a build-time tool for Java Web service projects (JAX-RS, JAX-WS, Spring Web).
It runs as a Java annotation processor over a project's source code and generates artifacts such as:

* HTML API documentation (scraped from Javadoc)
* Client-side libraries (Java, C#, Objective-C, Ruby, PHP, JavaScript, GWT, etc.)
* Interface definition documents (WSDL, WADL, XML Schema, Swagger/OpenAPI)

It does not affect the runtime behavior of the service being documented — it only inspects source and generates
artifacts at build time. Full project docs (user guide, module reference, FAQ) live in the GitHub wiki
(`stoicflame/enunciate.wiki.git` — see `LOCAL_CONTEXT.md` for the local clone path if you keep one); consult it
for user-facing configuration/behavior questions rather than guessing, since the in-repo README is minimal and
can lag behind the wiki.

## Build

Requires JDK 17 (enforced by `maven-enforcer-plugin`) and Maven 3.8.1+.

```
export JAVA_HOME=/path/to/jdk17
mvn clean install
```

By default, tests that exercise Enunciate's generated client-side code (compiling/running the generated C, C#,
Objective-C, Ruby, PHP, JS client libraries) are skipped, with an `echo-maven-plugin` reminder printed during the
build. To run the full suite you need `libxml2-dev`, `mono-devel`, `gnustep`/`gnustep-devel`, `ruby`, `nodejs`,
`php` installed (see README for the Ubuntu package list), then:

```
mvn clean install -Penunciate-full-tests
```

The `deploy` and `release` lifecycle phases are gated by an enforcer rule that requires `enunciate-full-tests`
to have been run — don't try to deploy/release without it.

## Release process

There is no CI/CD for this repo — releasing is a manual, local process. The authoritative, step-by-step
procedure (version bump mechanics, required Maven profiles/commands, PGP/OSSRH setup, wiki and schema updates,
and the sibling repos that also need touching) is documented in `release-procedure.md` at the repo root. If asked
to help with a release, follow that file rather than improvising, and if the process changes, update it (it's
meant to stay both human-readable and detailed enough to act on).

Each released minor version has a versioned copy of the Enunciate config XSD checked in at
`top/src/main/resources/META-INF/enunciate-<version>.xsd`. This isn't purely a release-time artifact: whenever a
change is made during ongoing development that affects the Enunciate config schema (e.g. a new/changed config
element or attribute in one of the modules), make sure the XSD for the *next* minor version (the current
`-SNAPSHOT` version, without the `-SNAPSHOT` suffix) exists — creating it (typically by copying the most recent
prior version's file) if it doesn't yet — and update it to reflect the change. That way the file is already
current by the time `release-procedure.md` step 12 publishes it. If a minor version is about to be released and
no config-affecting changes were made during that cycle, the XSD still needs to exist for that version — just
copy the previous version's file over verbatim (no diff needed beyond the filename).

Build a single module (and anything it depends on):

```
mvn -pl core -am install
```

Run a single test class:

```
mvn -pl core test -Dtest=ClassNameTest
```

Unit test coverage in most modules is thin — the primary correctness signal for module behavior comes from the
`examples/` integration projects (see below), not unit tests.

## Architecture

### Module layering

This is a Maven multi-module reactor (see root `pom.xml` for the full module list). The dependency flow is
roughly bottom-up:

1. **`core-annotations`** — the annotations Enunciate itself exposes to annotate a target API (no dependency on
   `core`).
2. **`javac-support`** — helpers for working with the `com.sun.source`/Javac compiler API, since Enunciate's
   engine is implemented as an annotation processor that walks the AST/mirror model of the target project's
   source rather than its compiled bytecode (needed to recover Javadoc, parameter names, etc.).
3. **`core`** — the engine: `com.webcohesion.enunciate.Enunciate` is the programmatic entry point;
   `EnunciateAnnotationProcessor` drives the annotation-processing pass; `EnunciateContext` holds the resulting
   API model. Also defines the module SPI (`com.webcohesion.enunciate.module.EnunciateModule`) and the
   `com.webcohesion.enunciate.api` package, which is the in-memory, framework-agnostic model of "the API"
   (resources, data types, etc.) that generating modules read from.
4. **`rt-util`** — small runtime utility classes shared by *generated* client code (not used by the engine
   itself).
5. **Framework/facet modules** — `jaxb`, `jaxrs`, `jaxws`, `jackson`, `spring-web`, `lombok`: each is an
   `EnunciateModule` implementation that knows how to interpret one API framework's annotations/conventions and
   contribute to the shared API model in `core`.
6. **Generating modules** — `idl` (WSDL/WADL/XSD), `swagger` (OpenAPI), `docs` (HTML docs from Freemarker
   templates), and the various `*-xml-client`/`*-json-client`/`javascript-client`/`gwt-json-overlay` modules:
   each reads the API model built by the facet modules and emits one kind of artifact.
7. **Invocation front-ends** — `maven-plugin` (all modules on the classpath by default) and
   `slim-maven-plugin` (no modules enabled by default; you add only what you need) wrap the `core` engine as
   Maven plugins. `top` is just a POM that pulls in the default set of modules for non-Maven (e.g. Ant,
   programmatic) use. `EnunciateTask` (in `core`) is the Ant entry point.
8. **`examples`** — sample Web service projects (`full-api-edge-cases`, `jackson2-api`,
   `jackson2-api-lombok`, `spring-petclinic`) that apply the Enunciate Maven plugin during their own build and
   assert on the generated output. These act as the project's integration/regression test suite and are also
   the reference for "how does a consumer actually configure this module."

### The module SPI

Everything that generates an artifact is an `EnunciateModule` (`core/.../module/EnunciateModule.java`),
discovered via `java.util.ServiceLoader` (`META-INF/services/com.webcohesion.enunciate.module.EnunciateModule`).
Most modules extend `BasicEnunicateModule` or `BasicGeneratingModule` rather than implementing the interface
directly. Key extension points a module can opt into:

* `DependencySpec` / `DependingModuleAwareModule` — declare/consume ordering dependencies between modules (e.g.
  `jaxrs` must run before `docs`).
* `ApiRegistryProviderModule` / `ApiRegistryAwareModule` — contribute to or read from the shared
  `AggregatedApiRegistry` (the cross-module API model).
* `ProjectExtensionModule` — mark generated output to be attached back into the consuming Maven project.
* `TypeDetectingModule`, `MediaTypeDefinitionModule`, `WebInfAwareModule`, `ContextModifyingModule` — narrower
  callbacks used by facet/generating modules as needed.

Third-party/custom modules follow the same SPI (see the wiki's Custom Modules page) and are added to the
classpath of whichever front-end (Maven/Ant/programmatic) is invoking Enunciate.

### Freemarker

Nearly every generating module (`docs`, `idl`, `swagger`, and each `*-client`/`gwt-json-overlay` module) produces
its artifacts by running a Freemarker template against a model built from the `core` API model
(`com.webcohesion.enunciate.api`). Template files use the `.fmt` extension (not the more common `.ftl`) and live
under each module's `src/main/resources/.../<module-package>/` next to the module class that drives them
(e.g. `docs/.../docs.fmt`, `swagger/.../openapi.fmt`, `java-xml-client/.../client-*.fmt`).

Enunciate deliberately uses Freemarker's **square-bracket tag syntax** (`[#if]`, `[#list]`, `[@myDirective/]`, ...)
instead of the default angle-bracket syntax. Since Enunciate templates are themselves generating XML and HTML,
angle-bracket Freemarker tags (`<#if>`, `<#list>`) are easy to confuse with the literal markup being emitted;
square brackets stay visually distinct from the output. This is opted into per-file, not globally configured: every
`.fmt` template starts with a `[#ftl]` header, which under Freemarker's tag-syntax auto-detection makes that whole
file parse with square-bracket tags. When adding a new template, always start it with `[#ftl]` and use square
brackets throughout.

Beyond the template files, most generating modules extend Freemarker with custom directives and methods (in
`com.webcohesion.enunciate.util.freemarker` for the shared/core ones, and in a `util` or top-level package per
client module for module-specific ones) rather than relying on stock Freemarker built-ins:

* `FileDirective` (core) — a `TemplateDirectiveModel` bound to `[@file name=...] ... [/@file]` in templates,
  used to split one template's output across multiple generated source files (e.g. one `.java`/`.cs`/`.rb` file
  per type).
* `ClientPackageForMethod` / namespace-equivalents — map an API package to the target language's
  package/namespace/module naming convention.
* `AnnotationValueMethod`, `SimpleNameWithParamsMethod`, `IsFacetExcludedMethod` (core) — shared helpers exposed
  to all templates for reading annotation values, computing simple/parameterized type names, and honoring facet
  include/exclude filtering, respectively.
* Module-specific `TemplateMethodModelEx` implementations (31+ across the codebase) for things like C
  identifier/name mangling (`c-xml-client`), Swagger definition/operation IDs and JSON examples (`swagger`), QName
  computation for WSDL/XSD (`idl`, `csharp-xml-client`), and JAXB namespace/prefix resolution (`jaxb`).

These are wired into the Freemarker data model by hand in each module's `processTemplate`/model-building code
(typically `model.put("methodName", new SomeMethod(...))`), not auto-discovered — so when a template needs a new
piece of logic, the pattern is: add a `TemplateMethodModelEx`/`TemplateDirectiveModel` class near the module that
needs it (or in `core/.../util/freemarker` if it's broadly reusable), `model.put(...)` it before processing the
template, then reference it by that name from the `.fmt` file.
