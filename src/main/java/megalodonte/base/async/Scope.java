package megalodonte.base.async;

import megalodonte.application.ErrorReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Vínculo de cancelamento entre trabalho assíncrono ({@link Async#Run}) e o ciclo de vida de
 * quem o iniciou (uma tela, uma ViewModel). Resolve a mesma classe de corrida que
 * {@code viewModelScope}/{@code DisposableEffect} resolvem no Jetpack Compose: cancelar é
 * síncrono e barato (uma flag), mas o trabalho em si só sabe que foi cancelado quando checa —
 * por isso {@link #run} e {@link #onCancel} existem, em vez de tentar interromper a força.
 * <p>
 * Uma instância cobre um único ciclo de vida (uma tela, uma ViewModel) e não é reutilizável
 * depois de cancelada.
 */
public final class Scope {
    private volatile boolean cancelled = false;
    private final List<Runnable> onCancelCallbacks = new ArrayList<>();

    /**
     * Roda {@code task} numa thread virtual, mas só se o escopo ainda não tiver sido cancelado
     * no instante em que a tarefa começa a executar. Se {@link #cancel()} já tiver rodado antes
     * disso, {@code task} nem chega a ser chamada.
     * <p>
     * Isso fecha a corrida "onDestroy roda antes do Async.Run começar" — mas não sozinho a
     * corrida "onDestroy roda no meio de task", que ainda depende de {@code task} checar
     * {@link #isCancelled()} nos pontos certos (ex.: logo antes de abrir um recurso) ou registrar
     * a limpeza via {@link #onCancel}.
     */
    public void run(RunnableThrowing task) {
        Async.Run(() -> {
            if (cancelled) return;
            task.run();
        });
    }

    /**
     * Registra {@code cleanup} pra rodar quando o escopo for cancelado. Se já estiver cancelado
     * no momento da chamada, roda {@code cleanup} imediatamente, na própria thread chamadora —
     * cobre o caso em que o recurso terminou de ser adquirido depois que {@link #cancel()} já
     * rodou (ex.: conexão que terminou de abrir depois que a tela já foi destruída).
     */
    public void onCancel(Runnable cleanup) {
        boolean jaCancelado;
        synchronized (onCancelCallbacks) {
            jaCancelado = cancelled;
            if (!jaCancelado) onCancelCallbacks.add(cleanup);
        }
        if (jaCancelado) runCleanup(cleanup);
    }

    /**
     * Cancela o escopo: {@link #isCancelled()} passa a retornar {@code true} e todo callback
     * registrado via {@link #onCancel} roda agora, nessa mesma thread (síncrono, igual
     * {@code onDestroy()} — cancelar é barato, o trabalho de limpeza em si é responsabilidade de
     * cada callback).
     */
    public void cancel() {
        List<Runnable> callbacks;
        synchronized (onCancelCallbacks) {
            if (cancelled) return;
            cancelled = true;
            callbacks = new ArrayList<>(onCancelCallbacks);
            onCancelCallbacks.clear();
        }
        for (Runnable cleanup : callbacks) runCleanup(cleanup);
    }

    private void runCleanup(Runnable cleanup) {
        try {
            cleanup.run();
        } catch (Throwable t) {
            ErrorReporter.handle(t);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
