package ru.ulmc.multithreading.producerConsumer;

import org.junit.Test;

import lombok.val;
import ru.ulmc.multithreading.producerConsumer.blockingQueue.ProductionController;

import static ru.ulmc.multithreading.producerConsumer.blockingQueue.ProductionController.ITEM_COUNT;

public class BlockingQueueTest {
    private CommonTester tester = new CommonTester(ITEM_COUNT);

    @Test
    public void test() throws InterruptedException {
        val controller = new ProductionController();
        tester.test(controller, controller);
    }
}
