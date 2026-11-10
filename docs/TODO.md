# TODO

## Concluído
- [x] Live reload de métodos compatíveis em múltiplas janelas, agente opt-in, cópia ativa isolada, rollback de render e descarte de assinaturas síncronas. Teste em JVM real e consumidor temporário pelo CLI.
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
- [x] Documented client-side centralized error handling with `ErrorReporter.register(...)`, `UI.runOnUi(...)` and `ErrorReporter.handle(...)`
- [x] Fixed unchecked cast warning in `Component.ref()`

## Pendências
- [ ] Evoluir o live reload para mudanças estruturais sem reinício; preservar as mesmas janelas nativas é requisito. Avaliar host permanente de Stage/Scene com gerações isoladas do código, contrato explícito de migração de estado e descarte de tarefas/callbacks antigos; proposta ainda não implementada em [LIVE_RELOAD.md](LIVE_RELOAD.md).
- [ ] Avaliar dependências novas em processo vivo, com isolamento, conflito de classes e reversão; atualmente qualquer mudança exige reinício.
- [ ] Integrar caches JSONC, builds multimódulo e janelas customizadas ao live reload.
- [ ] Preservar estado efêmero dos controles (foco, cursor, rolagem) e rastrear recursos/assinaturas criados fora do render síncrono.
- Revisar componentes que usam `ThemeInterface` para garantir que `applyTheme` funciona com o novo `ThemeBorder` (radius agora está em `border().radiusMd()` em vez de `radius().md()`)
