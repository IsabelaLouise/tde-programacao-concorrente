import java.util.Random;
import java.util.concurrent.Semaphore;

public class FilosofosHierarquia {
    static final int N = 5;
    static final long DURACAO_MS = 10000;
    static final Semaphore[] garfos = new Semaphore[N];
    static final int[] refeicoes = new int[N];
    static volatile boolean rodando = true;

    static class Filosofo extends Thread {
        private final int id;
        private final int primeiroGarfo;
        private final int segundoGarfo;
        private final Random random = new Random();

        Filosofo(int id) {
            this.id = id;
            int esquerdo = (id + N - 1) % N;
            int direito = id;
            this.primeiroGarfo = Math.min(esquerdo, direito);
            this.segundoGarfo = Math.max(esquerdo, direito);
        }

        private void log(String estado) {
            System.out.println("[Filosofo " + id + "] " + estado);
        }

        @Override
        public void run() {
            try {
                while (rodando) {
                    log("pensando");
                    Thread.sleep(random.nextInt(50));

                    log("com fome");
                    garfos[primeiroGarfo].acquire();
                    Thread.sleep(100);
                    garfos[segundoGarfo].acquire();

                    log("comendo (garfos " + primeiroGarfo + " e " + segundoGarfo + ")");
                    refeicoes[id]++;
                    Thread.sleep(random.nextInt(200));

                    garfos[segundoGarfo].release();
                    garfos[primeiroGarfo].release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N; i++) {
            garfos[i] = new Semaphore(1, true);
        }

        Filosofo[] filosofos = new Filosofo[N];
        for (int i = 0; i < N; i++) {
            filosofos[i] = new Filosofo(i);
            filosofos[i].start();
        }

        Thread.sleep(DURACAO_MS);
        rodando = false;

        for (Filosofo f : filosofos) {
            f.join();
        }

        System.out.println();
        System.out.println("Fim da simulacao. Refeicoes por filosofo:");
        int total = 0;
        for (int i = 0; i < N; i++) {
            System.out.println("Filosofo " + i + ": " + refeicoes[i]);
            total += refeicoes[i];
        }
        System.out.println("Total: " + total);
    }
}