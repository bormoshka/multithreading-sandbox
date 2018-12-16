package ru.ulmc.multithreading.producerConsumer.common;

import java.time.LocalTime;
import java.util.List;

public interface ResultProvider {
    List<LocalTime> getOutput();
}
