import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FilosofosIngenuo {

    static final int N = 5;
    static final Semaphore[] garfos = new Semaphore[N];
    static final AtomicReferenceArray<String> estados = new AtomicReferenceArray<>(N);
    static final AtomicLong ultimaAtividade = new AtomicLong(System.currentTimeMillis());

    static void registrar(int id, String mensagem) {
        ultimaAtividade.set(System.currentTimeMillis());
        System.out.println("[Filosofo " + id + "] " + mensagem);
    }

    static boolean todosComFome() {
        for (int i = 0; i < N; i++) {
            if (!"com fome".equals(estados.get(i))) {
                return false;
            }
        }
        return true;
    }

    static class Filosofo extends Thread {
        private final int id;
        private final int garfoEsquerdo;
        private final int garfoDireito;
        private final Random random = new Random();

        Filosofo(int id) {
            this.id = id;
            this.garfoEsquerdo = (id + N - 1) % N;
            this.garfoDireito = id;
        }

        private void mudarEstado(String novoEstado) {
            estados.set(id, novoEstado);
            registrar(id, novoEstado);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    mudarEstado("pensando");
                    Thread.sleep(random.nextInt(50));

                    mudarEstado("com fome");
                    garfos[garfoEsquerdo].acquire();
                    registrar(id, "pegou o garfo " + garfoEsquerdo);
                    Thread.sleep(100);
                    garfos[garfoDireito].acquire();
                    registrar(id, "pegou o garfo " + garfoDireito);

                    mudarEstado("comendo");
                    Thread.sleep(random.nextInt(200));

                    garfos[garfoDireito].release();
                    garfos[garfoEsquerdo].release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N; i++) {
            garfos[i] = new Semaphore(1);
            estados.set(i, "pensando");
        }

        for (int i = 0; i < N; i++) {
            new Filosofo(i).start();
        }

        while (true) {
            Thread.sleep(500);
            long parado = System.currentTimeMillis() - ultimaAtividade.get();
            if (parado > 3000 && todosComFome()) {
                System.out.println();
                System.out.println("DEADLOCK DETECTADO: nenhum filosofo progride ha " + parado + " ms");
                for (int i = 0; i < N; i++) {
                    System.out.println("Filosofo " + i + ": " + estados.get(i)
                            + " (segurando o garfo " + ((i + N - 1) % N) + ", esperando o garfo " + i + ")");
                }
                System.exit(1);
            }
        }
    }
}