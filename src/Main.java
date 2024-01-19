import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        long startTs = System.currentTimeMillis();
        Thread fillingQueues = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (Exception e) {
                    return;
                }
            }
        });

        fillingQueues.start();

        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Future<String> threadA = threadPool.submit(finder(queueA, "a"));
        Future<String> threadB = threadPool.submit(finder(queueB, "b"));
        Future<String> threadC = threadPool.submit(finder(queueC, "c"));
        String maxA = threadA.get();
        String maxB = threadB.get();
        String maxC = threadC.get();
        System.out.printf("""
                текст с максимум а
                %s
                текст с максимум b
                %s
                текст с максимум c
                %s""", maxA, maxB, maxC);
        threadPool.shutdown();
        long endTs = System.currentTimeMillis();
        System.out.println("Time: " + (endTs - startTs) + "ms");
    }


    public static Callable<String> finder(BlockingQueue<String> queue, String letter){
        return () -> {
            long maxAmount = 0;
            String textMax = "";
            for(int i = 0; i < 10_000; i++) {
                try {
                    String text = queue.take();
                    long amount = Arrays.stream(text.split(""))
                            .filter(x -> x.equals(letter))
                            .count();
                    if (maxAmount < amount) {
                        maxAmount = amount;
                        textMax = text;
                    }
                } catch (InterruptedException e) {
                    continue;
                }
            }
            return textMax;
        };
    }


    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
