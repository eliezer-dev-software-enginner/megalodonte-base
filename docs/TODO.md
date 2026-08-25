# TODO

## Concluído
- [x] Simplificação do Theme:
  - Removido `Theme.java` (duplicata)
  - Removido `ThemeRadius.java` (mesclado em ThemeBorder)
  - `ThemeColors.java` simplificado (removidos tokens de botão)
  - `ThemeInterface.java` limpo com 4 métodos
- [x] SLF4J logging added to key lifecycle points
- [x] Portuguese comments translated to English
- [x] AI_RULES.md updated with comment policy

## Pendências
- Revisar componentes que usam `ThemeInterface` para garantir que `applyTheme` funciona com o novo `ThemeBorder` (radius agora está em `border().radiusMd()` em vez de `radius().md()`)
- Consider adding a concrete SLF4J binding for dev/test (e.g. `slf4j-simple` or `logback`) so logs are visible during development
