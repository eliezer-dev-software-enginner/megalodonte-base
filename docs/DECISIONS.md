# Decisões Arquiteturais

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
