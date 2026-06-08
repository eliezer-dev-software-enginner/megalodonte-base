# TODO

## Concluído
- [x] Simplificação do Theme:
  - Removido `Theme.java` (duplicata)
  - Removido `ThemeRadius.java` (mesclado em ThemeBorder)
  - `ThemeColors.java` simplificado (removidos tokens de botão)
  - `ThemeInterface.java` limpo com 4 métodos

## Pendências
- Revisar componentes que usam `ThemeInterface` para garantir que `applyTheme` funciona com o novo `ThemeBorder` (radius agora está em `border().radiusMd()` em vez de `radius().md()`)
