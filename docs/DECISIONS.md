# Decisões Arquiteturais

## 2026-11-09 — Mundo de compilação separado e live reload desktop

O “mundo virtual” foi implementado como área temporária de build e gerações de
artefatos. Copiar memória de outra JVM não preservaria identidade, referências ou
invariantes do coletor; usamos Instrumentation oficial para redefinir classes
compatíveis, sem substituir class loaders. O CLI conserva runtime e dependências
em cópias próprias; Gradle continua responsável por compilação incremental.

Todas as telas visíveis gerenciadas preparam suas novas raízes no mesmo turno FX.
Somente após todas renderizarem ocorre a troca. A instância da tela e seus campos
permanecem; não reiniciamos o ciclo de montagem nem presumimos migração automática
de objetos. `SubscriptionScope` identifica assinaturas síncronas do render, para
descartar raízes anteriores ou candidatas rejeitadas sem apagar o estado.

Alterações estruturais recusadas pela JVM, inclusão/remoção de classes e mudanças
de dependências exigem reinício, por padrão explícito. `--restart-on-incompatible`
autoriza reinício com perda de estado. Não há promessa de rollback de efeitos de
domínio ou de threads em execução. A documentação pública registra esses limites
e a necessidade de atualizar core/base/reactivity nos consumidores locais.

## 2026-11-09 — Documentar o tratamento centralizado de exceções

**Problema**: `ErrorReporter` já centralizava falhas de `Async.Run`, `Scope` e operações de tela, mas o README não mostrava ao aplicativo cliente como registrar sua própria política de tratamento.

**Decisão**:
1. Documentar `ErrorReporter.register(...)` como etapa inicial do bootstrap, antes de iniciar trabalho assíncrono.
2. Mostrar que o callback pertence ao aplicativo e pode combinar logs, alertas e integrações externas.
3. Encaminhar apresentação visual com `UI.runOnUi(...)`, pois a falha pode chegar fora da thread JavaFX.
4. Documentar `ErrorReporter.handle(...)` para exceções capturadas que devam seguir a mesma política e o log de segurança mantido em `~/.megalodonte/errors.log`.
5. Reproduzir essa orientação na skill `megalodonte-native-ui` distribuída pelo CLI.

## 2026-08-25 — Javadoc documentation for all project classes

**Problem**: Most classes lacked Javadoc documentation, making the API hard to understand for new developers.

**Decision**:
1. Added Javadoc to all public classes, records, interfaces, and relevant methods across the codebase.
2. Translated all remaining Portuguese comments to English (Animations.java had 15+ inline comments).
3. Documented parameters with `@param`, return values with `@return`, and cross-references with `@link`.
4. Records (ThemeColors, ThemeTypography, ThemeSpacing, ThemeBorder, RouteProps, RouteResult) got parameter-level documentation.

**Files modified**: All `.java` files under `src/main/java/` (37 files total)

## 2026-08-25 — SLF4J logging on key lifecycle points

**Problem**: No structured logging in the framework. Errors and lifecycle events were either silent or used `System.err.println` inconsistently.

**Decision**:
1. Added `org.slf4j:slf4j-api:2.0.17` as a dependency (facade only — binding is the consuming app's responsibility).
2. Added `Logger` to: `MegalodonteApp`, `Bootstrap`, `ThemeManager`, `FontLoader`, `Async`, `Scope`.
3. Log levels: `INFO` for lifecycle events (launch, bootstrap dispatch, theme set), `DEBUG` for detailed flow (font loading details, scope cancellation), `WARN` for recoverable issues, `ERROR` for failures.
4. Converted existing `System.err.println` calls in `FontLoader` to SLF4J.
5. All logs are in English.
6. Translated all remaining Portuguese code comments to English across the codebase.
7. Added rule to `AI_RULES.md`: comments always in English, add useful comments on complex code (race conditions, workarounds, non-obvious decisions, platform APIs).

**Files modified**: `MegalodonteApp.java`, `Bootstrap.java`, `ThemeManager.java`, `FontLoader.java`, `Async.java`, `Scope.java`, `ErrorReporter.java`, `Context.java`, `LinuxDesktopEntry.java`, `ScreenComponent.java`, `Component.java`, `AI_RULES.md`

## 2026-07-24 — Carregamento de fontes customizadas (FontLoader + convenção assets/fonts)

**Problema**: `ThemeTypography` ganhou um campo `fontFamily`, mas pra uma fonte custom
(ex: Roboto) funcionar de verdade, os arquivos `.ttf`/`.otf` precisam ser registrados via
`Font.loadFont(...)` antes de qualquer Scene usar o tema. A primeira versão disso exigia
que a aplicação cliente chamasse `Font.loadFont(...)` arquivo por arquivo, no `Main.java`
— boilerplate repetitivo, e fácil de esquecer de recarregar algum peso da fonte.

**Decisão**: convenção + automação.
1. Aplicações cliente colocam suas fontes em `assets/fonts/` dentro de resources (ex:
   `src/main/resources/assets/fonts/roboto/Roboto-Regular.ttf`). Subpastas são permitidas
   e escaneadas recursivamente — dá pra organizar cada família na sua própria pasta.
2. `megalodonte.base.theme.FontLoader.loadAll()` varre `assets/fonts/` em toda raiz do
   classpath (jar da app, jars de dependência, diretório de classes explodido ao rodar
   pela IDE) e registra cada `.ttf`/`.otf` encontrado. Sem erro se a pasta não existir —
   apps sem fonte customizada não pagam custo nenhum.
3. `Bootstrap.dispatch(...)` chama `FontLoader.loadAll()` automaticamente, uma vez, antes
   de disparar o `handler` da aplicação — nenhuma app precisa chamar nada no `Main`. Só
   apontar `ThemeTypography.fontFamily()` pro nome da família já é suficiente.

**Arquitetura final**:
- `assets/fonts/**/*.{ttf,otf}` (resource, por convenção) → descoberto e carregado
  automaticamente por `FontLoader.loadAll()`.
- `FontLoader` lida com os dois formatos de resource root que o classloader pode devolver:
  `file:` (rodando exploded/IDE) e `jar:` (app empacotada).
- `Bootstrap.dispatch` chama isso antes de qualquer `Scene`/`ThemeManager.applyFontFamily`.
- `ThemeTypography.fontFamily()` só precisa citar o nome da família (`"Roboto"`); carregar
  o arquivo em si não é mais responsabilidade da aplicação.

## 2026-06-03 — Simplificação do Theme

**Problema**: O sistema de Theme tinha duplicação de interface (`Theme` + `ThemeInterface`), granularidade excessiva (`ThemeRadius` isolado com apenas 3 campos) e misturava conceitos do core theme com tokens específicos de componente (cores de botão em `ThemeColors`).

**Decisão**:
1. Remover `Theme.java` — era duplicata não utilizada de `ThemeInterface.java`.
2. Remover `ThemeRadius.java` — granularidade excessiva; mesclado em `ThemeBorder`.
3. `ThemeBorder` agora inclui `width`, `radiusSm`, `radiusMd`, `radiusLg` — borda e raio são propriedades de borda.
4. `ThemeColors` agora contém apenas o palette core: `background`, `surface`, `primary`, `secondary`, `textPrimary`, `textSecondary`, `border`. Cores específicas de botão foram removidas — cada componente deve mapear as cores core para seus próprios tokens.
5. `ThemeInterface` mantido como contrato único com 4 métodos.

**Arquitetura final**:
- `ThemeInterface` → `colors()`, `typography()`, `spacing()`, `border()`
- `ThemeColors` — palette core
- `ThemeTypography` — font sizes + resolve(TextVariant)
- `ThemeSpacing` — spacing scale
- `ThemeBorder` — border width + radius scale
