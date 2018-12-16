package ru.ulmc.multithreading.producerConsumer;

import org.junit.Test;

import static ru.ulmc.multithreading.producerConsumer.ProducerTask.PRODUCE_COUNT;

public class WaitNotifyTest {
    CommonTester tester = new CommonTester(PRODUCE_COUNT);

    @Test
    public void test() throws InterruptedException {
        Storage storage = new Storage();
        ProducerConsumerManager pcm = new ProducerConsumerManager(storage);
        tester.test(pcm, storage);
    }
}
