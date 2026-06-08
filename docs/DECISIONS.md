# Decisões Arquiteturais

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
