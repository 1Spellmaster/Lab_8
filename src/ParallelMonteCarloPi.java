import java.util.Locale;
import java.util.SplittableRandom;

public class ParallelMonteCarloPi {
    // загальна кількість "кинутих" точок
    static final long ITERATIONS = 1_000_000_000L;
    // зерно генератора випадкових чисел
    static final long SEED = 42;

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("Використання: java ParallelMonteCarloPi <кількість потоків>");
            return;
        }

        int threadsCount;
        try {
            threadsCount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Помилка: кількість потоків має бути цілим числом.");
            return;
        }

        if (threadsCount < 1) {
            System.out.println("Помилка: кількість потоків має бути більшою за 0.");
            return;
        }

        Thread[] threads = new Thread[threadsCount];
        // кожен потік записує сюди кількість своїх точок, що потрапили в коло
        long[] hits = new long[threadsCount];

        long start = System.nanoTime();

        for (int i = 0; i < threadsCount; i++) {
            int index = i;
            // ділимо точки між потоками порівну, залишок віддаємо першим потокам
            long points = ITERATIONS / threadsCount + (i < ITERATIONS % threadsCount ? 1 : 0);

            threads[i] = new Thread(() -> hits[index] = countHits(points, SEED + index));
            threads[i].start();
        }

        // чекаємо завершення всіх потоків
        for (Thread thread : threads) {
            thread.join();
        }

        long totalHits = 0;
        for (long h : hits) {
            totalHits += h;
        }

        // частка точок у колі ≈ π/4
        double pi = 4.0 * totalHits / ITERATIONS;
        double timeMs = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf(Locale.US, "PI is %.5f%n", pi);
        System.out.printf(Locale.US, "THREADS %d%n", threadsCount);
        System.out.printf(Locale.US, "ITERATIONS %,d%n", ITERATIONS);
        System.out.printf(Locale.US, "TIME %.2fms%n", timeMs);
    }

    // "кидає" points випадкових точок в одиничний квадрат
    // і рахує, скільки з них потрапило в чверть одиничного кола
    static long countHits(long points, long seed) {
        SplittableRandom random = new SplittableRandom(seed);
        long hits = 0;

        for (long i = 0; i < points; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();

            if (x * x + y * y <= 1) {
                hits++;
            }
        }

        return hits;
    }
}
