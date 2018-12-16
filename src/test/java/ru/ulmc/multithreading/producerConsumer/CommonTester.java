package ru.ulmc.multithreading.producerConsumer;

import java.time.LocalTime;
import java.util.List;

import ru.ulmc.multithreading.producerConsumer.common.ResultProvider;
import ru.ulmc.multithreading.producerConsumer.common.Starter;

import static org.junit.Assert.assertEquals;
import static ru.ulmc.multithreading.producerConsumer.ProducerTask.PRODUCE_COUNT;

public class CommonTester {

    private final int count;

    public CommonTester(int count) {
        this.count = count;
    }

    public void test(Starter starter, ResultProvider storage) throws InterruptedException {
        starter.startProducing();
        List<LocalTime> output = storage.getOutput();
        assertEquals(count, output.size());
        System.out.println("Size OK...");
        System.out.println("Seems legit...");
    }
}
