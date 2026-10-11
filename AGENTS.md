# AGENTS.md

Guidance for AI coding agents (Codex, Cursor, Copilot, Claude Code, …) working in this repository. `CLAUDE.md` imports this file, so keep all shared guidance here.

## What this is

ChatImprover is a **client-side Fabric mod for Minecraft 26.2 (Java 25)** that acts as a host/framework for **server-specific addons**: separate mods that block ("filter") or rewrite ("improve") incoming server chat messages. This repo contains the host mod and its public API (`org.ayoree.chatimprover.api`). Known addons: [ChatImprove-dexland](https://github.com/Ayoree/ChatImprove-dexland) and [ChatImprove-template](https://github.com/Ayoree/ChatImprove-template). Use them to see real API usage and to check that API changes don't break them.

The mod also has its own features: a configurable command panel (`/ayo`), a right-click-on-player menu, and a chat message structure debugger (`/debug_messages`). Player-facing strings are hardcoded in Russian (no lang files); code, comments and README are in English.

## Build & run

Requires a local JDK 25 (Gradle toolchain, no auto-provisioning). All versions live in `gradle.properties`.

| Command | Purpose |
|---|---|
| `./gradlew build` | Compile + `checkLicenses` → `build/libs/chatimprover-<mod_version>.jar`. This is all CI runs (every push/PR). |
| `./gradlew runClient` | Dev client (ModMenu is on the dev runtime classpath). Game dir is `run/`, configs in `run/config/chatimprover/`. |
| `./gradlew applyLicenses` | Add the AGPL header from `HEADER.txt` to Java files. `build` fails if a header is missing or altered. |
| `./gradlew genSources` | Decompile Minecraft (sources jar goes under `.gradle/loom-cache/minecraftMaven/`). For a quick signature check, `javap` on the game jar there also works because names are unobfuscated. |
| `./gradlew publishToMavenLocal` | Addons list `mavenLocal()` first, so this lets them build against local changes (`org.ayoree:chatimprover:<mod_version>`; the addon's `chat_improver_version` must match). |

There are **no automated tests** (no `src/test`). Verify with `build`, then manually in `runClient`. Provider-based behaviour only triggers on a multiplayer server whose address an installed addon lists. `./gradlew publish` (GitHub Packages) runs in CI when a GitHub release is published. `mod_version` format is `<minecraft>-<build>` (e.g. `26.2-12`).

## Minecraft 26.2 specifics (read before writing MC code)

- 26.x is unobfuscated: **Mojang official names, no Yarn mappings**, Loom plugin `net.fabricmc.fabric-loom`, plain `implementation` dependencies (no `modImplementation`/`remapJar`). Commits before `a1f0e4a` use Yarn names (`MinecraftClient`, `ChatHud`, `Text`), so don't copy from old history. `ChatHudAccessor` keeps its Yarn-era name but targets `ChatComponent`.
- API shapes used here: `client.gui.setScreen(..)`, `client.gui.screen()`, `client.gui.hud.getChat()`, `Identifier.fromNamespaceAndPath(..)`, `new ClickEvent.RunCommand(..)`, `new HoverEvent.ShowText(..)`, `ChatComponent.addClientSystemMessage(..)`, `scrollChat(int)`.
- `@Accessor` strings are raw field names (`allMessages`, `chatScrollbarPos`). Re-check them on every Minecraft update.
- owo-lib UI classes are `UIComponent`, `UIComponents`, `UIContainers` and `BaseParentUIComponent` (renamed in owo for 1.21.11+, see `d8979f3`). Older owo docs and examples use `Component`/`Components`/`Containers`.
- A Minecraft bump (see `a1f0e4a`) touches:
  - `gradle.properties`: minecraft, loader, fabric_api, owo, modmenu and `mod_version`.
  - `fabric.mod.json` `depends`.
  - Mixin accessors.
  - Renamed vanilla APIs across the sources.
  - On a Java bump, also the Java version in `build.gradle`, the mixins `compatibilityLevel`, and `"java"` in `fabric.mod.json`.

  Addons then bump `chat_improver_version`.

## Architecture

The entry point `ChatImprover.onInitialize()` is registered as a `main` entrypoint, but all the code is client code (`Minecraft`, chat HUD). The mod is client-only even though `fabric.mod.json` says `"environment": "*"`. The initializer loads both configs and registers `ChatHandler`, `ConnectionHandler`, `RightClickHandler` and `CustomCommands`.

### Message pipeline

Only **game/system** messages are processed (`ClientReceiveMessageEvents.ALLOW_GAME` / `MODIFY_GAME`). Signed player chat (`*_CHAT` events) is never touched.

```
ALLOW_GAME
  phase ChatImproverApi.ADDON_PHASE → addon listeners (ordered before the default phase in ChatHandler.init)
  default phase → ChatHandler.onAllowMessage
     ├─ overlay (action bar)                                     → allow untouched
     ├─ isBlockTrash && FilterFactory.testAllFilters(msg)        → cancel (message dropped)
     ├─ fixChatOnFocus && chat open && chatScrollbarPos == 0
     │     → ChatImproverApi.reemit(msg) → cancel
     │       (improve + addClientSystemMessage + scrollChat(1): visible lines don't jump while chat is open)
     └─ otherwise allow
MODIFY_GAME → ChatHandler.onModifyMessage
     ├─ overlay (action bar) → returned unchanged
     └─ otherwise            → ChatImproverApi.improve(msg)

improve(msg) = ChatMessageFactory.createChatMessage(msg)   // first matching provider, else plain ChatMessage (no-op)
                 .generateChangedMsg()   if isImproveMessages
                 .addChatButtons()       if chatButtons
                 .getChangedMessage()
```

- When ChatImprover cancels (filtered or reemitted), `MODIFY_GAME` and every later `ALLOW_GAME` listener are skipped. That is why addons must register their `ALLOW_GAME` listeners in `ADDON_PHASE`. `reemit()` does not fire the events again, so there is no recursion and filters don't run twice.
- Fabric also fires `ALLOW_GAME`/`MODIFY_GAME` for action-bar messages (`overlay == true`). Both handlers return early for them, so filters, reemit and `improve()` apply only to chat messages. Keep this check in any new `*_GAME` listener.

### Provider registry (`internal/factories`)

- `ChatMessageFactory` and `FilterFactory` are near-identical twins, so change both together.
- Each one loads its provider type with `java.util.ServiceLoader` in a static initializer. Addons annotate their providers with `@AutoService`, which generates `META-INF/services`.
- Providers are indexed by the lowercased addresses from `AddonInformer.IPs()`. An address without a port is also indexed as `host:25565`.
- Lookup uses `ServerData.ip`, the exact string the player typed in the server list. `ConnectionHandler` sets it on join. It is `null` in singleplayer, where no provider applies.
- Providers whose `addonID()` is in `CONFIG.disabledAddons()` are skipped.
- The first provider whose `validator()` returns true wins. Order across addons is ServiceLoader order, which is undefined.

### Public API contract (`api/`, consumed by external addon jars)

Treat everything in `api/` as published API. Renaming or changing the signature of anything there breaks addons, and that includes the protected `m_message`/`m_changedMsg` fields, which addon subclasses use directly.

- `Filter.Provider.validator()` returns **true to block** the message.
- `ChatMessage.Provider`: `validator()` decides whether `creator()` should build this `ChatMessage` subclass. Subclasses override `generateChangedMsg()` to build `m_changedMsg` from `m_message`, then `return this`.
- `ChatMessageWithSender`, `ChatMessageWithReceiver` and `ChatMessageWithReceiverAndSender`: the subclass sets the nick(s) in its constructor. `addChatButtons()` prepends the clickable `senderSymbol`/`receiverSymbol` (config strings, `&` becomes `§`), which run `/ayo open <nick>`.
- `AddonInformer.addonID()` must be the addon's real Fabric mod id, because the config screen calls `FabricLoader.getModContainer(id).get()` on it.
- `ChatImproverApi` provides `ADDON_PHASE`, `improve(msg)`, and `reemit(msg)`. `reemit` is for addons that cancel a message and display it later.

### Commands, screens, mixins

- `/ayo edit` opens `ChatimproverEditCustomScreen`, the category/command editor. It edits a deep copy of the config and writes a fresh deep copy back on "Save", so the screen and the live config never share mutable objects.
- `/ayo open [nick]` opens `ChatimproverCustomScreen`, a button grid built from `CustomScreenConfig`.
  - `{INPUT}` in a command is replaced with the text-box value (the nick).
  - Commands that start with `/` are sent with `sendCommand`; anything else is sent as chat.
  - Right-clicking a player with an empty main hand opens the same screen for that player (`RightClickHandler`, setting `rightclickMenu`).
- `/debug_messages` opens `LastMessagesScreen`: the last 50 chat lines as a collapsible tree of `Component` siblings, with copy buttons. This is the tool for working out validators for a server's message format.
- From command callbacks, open screens with `CommandToScreenHandler.openScreen(parent -> new X(parent))`. It defers to the next client tick, because a screen set directly from a command gets closed together with the chat screen.
- ModMenu config screen: `ModMenuIntegration` (`modmenu` entrypoint) opens `ChatimproverConfigScreen`, which stages edits and writes them only on "Save".
- UI: each screen is a `BaseUIModelScreen<FlowLayout>` that loads `assets/chatimprover/owo_ui/<name>.xml` via `DataSource.asset(Identifier.fromNamespaceAndPath(MOD_ID, "<name>"))`. Java code finds components by XML `id` (`childById`) and builds the dynamic parts in code.
- Mixins:
  - `ChatHudAccessor` exposes chat history and scroll position.
  - `FlowLayoutMixin` (with `FlowLayoutAccessor` and `BaseParentComponentInvoker`) adds `swapChilds()` to owo's `FlowLayout`. It is exposed through the duck interface `api.mixin.FlowLayoutOperations` and used for ↑/↓ reordering in the edit screen.
  - Duck interfaces must live outside the `mixin` package.

### Config (owo-lib)

- `internal/configs/ConfigModel` generates `ChatimproverConfig` (`ChatImprover.CONFIG`), and `CustomScreenConfigModel` generates `CustomScreenConfig` (`ChatImprover.CONFIG_CUSTOM_SCREEN`).
  - owo's annotation processor produces these wrappers in `build/generated/sources/annotationProcessor/…`. Never write them by hand. If they show up as missing, run `./gradlew compileJava`.
  - The `generatedSourceOutputDirectory`, `srcDirs` and `eclipse.classpath` tweaks in `build.gradle` exist so that VS Code (Eclipse JDT) resolves these wrappers. Keep them.
- Configs are saved as `config/chatimprover/chatimprover-config.json5` and `custom-screen-config.json5` through reflective (Jankson) serialization. Field names are the JSON keys.
  - Renaming a field silently resets players' saved values. This includes the private `m_*` fields in `customconfig/*`.
  - Keep the no-arg constructors.
- A new boolean or string setting takes 3 changes:
  1. Add the field to `ConfigModel`.
  2. Add `<checkbox id="<field>">` or `<text-box id="<field>">` to `owo_ui/config_ui.xml`.
  3. Add its key to `CHECKBOXES_KEYS` or `STRING_KEYS` in `ChatimproverConfigScreen`. The lookup is by `key.name()`, which equals the field name.

## Conventions

- Every `.java` file starts with the AGPL-3.0 header from `HEADER.txt`. This is enforced; run `applyLicenses`.
- Java style:
  - `m_` prefix on instance fields.
  - `UPPER_CASE` for static state (`CONFIG`, `REGISTRY`, `OPENER`).
  - `final` on parameters and locals.
  - Accessor pairs named after the property (`name()` / `name(String)`).
  - Static `init()` methods that register handlers.
  - 4-space indent in Java, tabs in Gradle files.

## Stale leftovers (don't be misled)

- The README's dependency snippet says `modImplementation`. On 26.x, addons use plain `implementation`, as the template does.
