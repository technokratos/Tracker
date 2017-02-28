package checks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Created by denis on 04.02.17.
 */
public class Pool {

    private static ExecutorService service = Executors.newFixedThreadPool(4);

    public static void exec(Runnable... task){

        Stream.of(task).parallel().forEach(Runnable::run);

//        CountDownLatch countDownLatch = new CountDownLatch(task.length);
//        for (int i = 0; i < task.length; i++) {
//            int finalI = i;
//            service.execute(()->{
//                task[finalI].run();
//                countDownLatch.countDown();
//            });
//        }
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


}
