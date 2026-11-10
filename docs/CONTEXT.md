# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)

## Última alteração
- Live reload desktop: `ReloadAgent` habilitado por `megalodonte dev`, atualização de bytecode e rerender das janelas de `Context`/`ScreenManager`. Instâncias/State são preservados em alterações compatíveis; raízes e bytecode anteriores são recuperados em falha. Assinaturas síncronas de render são descartadas via `SubscriptionScope` do core atualizado. Limites e comandos em [LIVE_RELOAD.md](LIVE_RELOAD.md).
- README agora documenta o tratamento centralizado de exceções com `ErrorReporter.register(...)`, incluindo registro em log, retorno seguro à thread de UI para alertas e encaminhamento manual por `ErrorReporter.handle(...)`.
- Added Javadoc documentation to all project classes and relevant methods
- Translated remaining Portuguese comments to English (Animations.java, Context.java, KeyBind.java)
- Hospedeiro nativo por mensageria: diagnóstico e primeira etapa experimental em [NATIVE_MESSAGING.md](NATIVE_MESSAGING.md). Primeira etapa validada ao final (10 testes unitários e integração com duas janelas), publicada no Maven Local. Ainda sem integração transparente com aplicações existentes.
- CLI experimental: `megalodonte dev --native-host --worker-main ...` validado com inclusão/remoção de classe e campo novo, mantendo PID do hospedeiro e identidade de duas janelas. Exige API NativeWorker; o modo padrão não mudou.
# Compilation-world clarification (2026-11-10)

- The proposed JSON messaging maps the complete project into the isolated build world; it is not an application-specific UI protocol.
- Initial requests contain a project manifest; subsequent requests contain file/token/symbol deltas. Responses contain status and compiled artifact metadata.
- Resources participate as content-addressed blobs. `.env` is hash-only and requires restart without exposing values.
- Token granularity improves change detection; Java compilation and runtime activation remain class/dependency granular.
- See `COMPILATION_WORLD.md` for the design and boundaries.
