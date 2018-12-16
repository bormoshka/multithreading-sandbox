package ru.ulmc.multithreading.producerConsumer;

import ru.ulmc.multithreading.producerConsumer.common.Starter;

public class ProducerConsumerManager implements Starter {
    private final Storage storage;

    ProducerConsumerManager(Storage storage) {
        this.storage = storage;
    }

    public void startProducing() throws InterruptedException {
        Thread timeProducer = new Thread(new ProducerTask(storage));
        timeProducer.start();
        Thread timeConsumer = new Thread(new ConsumerTask(storage));
        timeConsumer.start();

        timeProducer.join();
        timeConsumer.join();
    }
}
