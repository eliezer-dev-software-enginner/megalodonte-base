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
- [x] Javadoc documentation added to all project classes and relevant methods
- [x] `./gradlew javadoc` configured and generating HTML successfully
- [x] Added `slf4j-simple` as test runtime binding for log visibility during tests
- [x] Implemented `ErrorReporter.log()` — writes errors to `~/.megalodonte/errors.log`
- [x] Fixed unchecked cast warning in `Component.ref()`

## Pendências
- Revisar componentes que usam `ThemeInterface` para garantir que `applyTheme` funciona com o novo `ThemeBorder` (radius agora está em `border().radiusMd()` em vez de `radius().md()`)
- Consider publishing Javadoc HTML via GitHub Pages or as part of the release process
