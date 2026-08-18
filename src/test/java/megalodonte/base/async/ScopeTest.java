package megalodonte.base.async;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScopeTest {

    @Test
    void runExecutaTaskQuandoNaoCancelado() throws InterruptedException {
        var scope = new Scope();
        var latch = new CountDownLatch(1);
        var executou = new AtomicBoolean(false);

        scope.run(() -> {
            executou.set(true);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "task deveria ter rodado");
        assertTrue(executou.get());
    }

    @Test
    void runNaoExecutaTaskSeEscopoJaCanceladoAntesDeSubmeter() throws InterruptedException {
        var scope = new Scope();
        scope.cancel();

        var executou = new AtomicBoolean(false);
        var latch = new CountDownLatch(1);
        scope.run(() -> {
            executou.set(true);
            latch.countDown();
        });

        boolean rodou = latch.await(300, TimeUnit.MILLISECONDS);
        assertFalse(rodou, "task não deveria ter rodado — escopo já estava cancelado");
        assertFalse(executou.get());
    }

    @Test
    void onCancelDisparaNaHoraSeEscopoJaCancelado() {
        var scope = new Scope();
        scope.cancel();

        var chamado = new AtomicBoolean(false);
        scope.onCancel(() -> chamado.set(true));

        assertTrue(chamado.get(), "onCancel registrado depois do cancel() devia disparar na hora");
    }

    @Test
    void onCancelDisparaQuandoCancelRodaDepoisDeRegistrado() {
        var scope = new Scope();
        var chamado = new AtomicBoolean(false);
        scope.onCancel(() -> chamado.set(true));

        assertFalse(chamado.get(), "não devia ter disparado antes do cancel()");
        scope.cancel();
        assertTrue(chamado.get());
    }

    @Test
    void cancelSoDisparaCadaCallbackUmaVezMesmoChamadoVariasVezes() {
        var scope = new Scope();
        var contador = new AtomicInteger(0);
        scope.onCancel(contador::incrementAndGet);

        scope.cancel();
        scope.cancel();
        scope.cancel();

        assertEquals(1, contador.get());
    }

    @Test
    void isCancelledRefleteEstadoAntesEDepoisDoCancel() {
        var scope = new Scope();
        assertFalse(scope.isCancelled());
        scope.cancel();
        assertTrue(scope.isCancelled());
    }

    @Test
    void callbackQueEstouraExcecaoNaoImpedeOsOutrosDeRodar() {
        var scope = new Scope();
        var segundoRodou = new AtomicBoolean(false);

        scope.onCancel(() -> {
            throw new RuntimeException("callback quebrado de propósito");
        });
        scope.onCancel(() -> segundoRodou.set(true));

        assertDoesNotThrow(scope::cancel);
        assertTrue(segundoRodou.get(), "segundo callback devia rodar mesmo o primeiro tendo estourado");
    }
}
