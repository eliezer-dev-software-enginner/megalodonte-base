# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)

## Última alteração
- Simplificação do sistema de Theme:
  - Removido `Theme.java` (duplicata não utilizada)
  - Removido `ThemeRadius.java` (granularidade excessiva, mesclado em ThemeBorder)
  - `ThemeBorder.java` agora inclui `radiusSm`, `radiusMd`, `radiusLg`
  - `ThemeColors.java` simplificado: removidos tokens específicos de botão
  - `ThemeInterface.java` mantido como contrato único, com 4 métodos: colors(), typography(), spacing(), border()
