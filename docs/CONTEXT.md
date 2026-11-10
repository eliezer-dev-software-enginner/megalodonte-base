# Contexto do Projeto

## Estrutura
- JavaFX + Megalodonte (UI framework)

## Última alteração
- Live reload desktop: `ReloadAgent` habilitado por `megalodonte dev`, atualização de bytecode e rerender das janelas de `Context`/`ScreenManager`. Instâncias/State são preservados em alterações compatíveis; raízes e bytecode anteriores são recuperados em falha. Assinaturas síncronas de render são descartadas via `SubscriptionScope` do core atualizado. Limites e comandos em [LIVE_RELOAD.md](LIVE_RELOAD.md).
- README agora documenta o tratamento centralizado de exceções com `ErrorReporter.register(...)`, incluindo registro em log, retorno seguro à thread de UI para alertas e encaminhamento manual por `ErrorReporter.handle(...)`.
- Added Javadoc documentation to all project classes and relevant methods
- Translated remaining Portuguese comments to English (Animations.java, Context.java, KeyBind.java)
- Hospedeiro nativo por mensageria: diagnóstico e primeira etapa experimental em [NATIVE_MESSAGING.md](NATIVE_MESSAGING.md). Testes concentrados no final por solicitação do usuário; ainda sem integração transparente com aplicações existentes.
