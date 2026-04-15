import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

class Barbearia {

    private final int CAPACIDADE = 20;

    private int clientesNaLoja = 0;

    private Queue<Cliente> sofa = new LinkedList<>();
    private Queue<Cliente> emPe = new LinkedList<>();

    private Lock lock = new ReentrantLock(true);
    private Condition clienteDisponivel = lock.newCondition();

    private Semaphore caixa = new Semaphore(1, true);

    long totalEsperaPe = 0;
    long totalEsperaSofa = 0;
    int totalClientesAtendidos = 0;

    public boolean entrar(Cliente c) {
        lock.lock();
        try {
            if (clientesNaLoja >= CAPACIDADE) {
                log("Cliente " + c.id + " foi embora (lotado)");
                return false;
            }

            clientesNaLoja++;
            c.tempoEntrada = System.currentTimeMillis();

            log("Cliente " + c.id + " entrou");

            if (sofa.size() < 4) {
                c.tempoSofa = System.currentTimeMillis();
                sofa.add(c);
                log("Cliente " + c.id + " sentou no sofa");
            } else {
                c.tempoPe = System.currentTimeMillis();
                emPe.add(c);
                log("Cliente " + c.id + " ficou em pe");
            }

            clienteDisponivel.signalAll();
            return true;

        } finally {
            lock.unlock();
        }
    }

    public Cliente proximoCliente() throws InterruptedException {
        lock.lock();
        try {
            while (sofa.isEmpty()) {
                log("Barbeiro dormindo...");
                clienteDisponivel.await();
            }

            Cliente c = sofa.poll();
            c.tempoAtendimento = System.currentTimeMillis();

            if (c.tempoPe != 0) {
                totalEsperaPe += (c.tempoSofa - c.tempoPe);
            }

            totalEsperaSofa += (c.tempoAtendimento - c.tempoSofa);

            log("Cliente " + c.id + " foi chamado");

            if (!emPe.isEmpty()) {
                Cliente promovido = emPe.poll();
                promovido.tempoSofa = System.currentTimeMillis();
                sofa.add(promovido);
                log("Cliente " + promovido.id + " foi promovido para o sofa");
            }

            totalClientesAtendidos++;
            return c;

        } finally {
            lock.unlock();
        }
    }

    public void pagar(Cliente c) {
        try {
            caixa.acquire();
            log("Cliente " + c.id + " esta pagando");
            Thread.sleep(300);
            log("Cliente " + c.id + " pagou");
            caixa.release();
        } catch (InterruptedException e) {}
    }

    public void sair(Cliente c) {
        lock.lock();
        try {
            clientesNaLoja--;
            log("Cliente " + c.id + " saiu");
        } finally {
            lock.unlock();
        }
    }

    public void mostrarMetricas() {
        log("==== METRICAS ====");
        if (totalClientesAtendidos > 0) {
            log("Tempo medio em pe: " + (totalEsperaPe / totalClientesAtendidos) + " ms");
            log("Tempo medio no sofa: " + (totalEsperaSofa / totalClientesAtendidos) + " ms");
        }
    }

    private void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "] " + msg);
    }
}

class Cliente implements Runnable {

    int id;
    Barbearia barbearia;

    long tempoEntrada;
    long tempoPe;
    long tempoSofa;
    long tempoAtendimento;

    private Lock lock = new ReentrantLock();
    private Condition atendimentoFinalizado = lock.newCondition();
    private boolean finalizado = false;

    public Cliente(int id, Barbearia b) {
        this.id = id;
        this.barbearia = b;
    }

    public void aguardarAtendimento() throws InterruptedException {
        lock.lock();
        try {
            while (!finalizado) {
                atendimentoFinalizado.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void finalizarAtendimento() {
        lock.lock();
        try {
            finalizado = true;
            atendimentoFinalizado.signal();
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        if (!barbearia.entrar(this)) return;

        try {
            aguardarAtendimento();
        } catch (InterruptedException e) {}

        barbearia.pagar(this);
        barbearia.sair(this);
    }
}

class Barbeiro implements Runnable {

    int id;
    Barbearia barbearia;

    public Barbeiro(int id, Barbearia b) {
        this.id = id;
        this.barbearia = b;
    }

    public void run() {
        while (true) {
            try {
                Cliente c = barbearia.proximoCliente();

                System.out.println("Barbeiro " + id + " cortando cliente " + c.id);
                Thread.sleep(1000);
                System.out.println("Barbeiro " + id + " terminou cliente " + c.id);
                c.finalizarAtendimento();

            } catch (InterruptedException e) {}
        }
    }
}

public class BarbeariaHilzer {

    public static void main(String[] args) {

        Barbearia barbearia = new Barbearia();

        for (int i = 1; i <= 3; i++) {
            new Thread(new Barbeiro(i, barbearia)).start();
        }

        for (int i = 1; i <= 20; i++) {
            new Thread(new Cliente(i, barbearia)).start();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
        }

        new Timer().schedule(new TimerTask() {
            public void run() {
                barbearia.mostrarMetricas();
            }
        }, 10000);
    }
}