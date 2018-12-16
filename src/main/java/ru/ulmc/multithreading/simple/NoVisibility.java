package ru.ulmc.multithreading.simple;

public class NoVisibility {
    private static boolean ready;
    private static int number;

    public static void main(String[] args) {

        new Reader().start();
        number = 23;
        ready = true;
    }

    private static class Reader extends Thread {
        @Override
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }
}
