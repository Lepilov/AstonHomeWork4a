import java.util.concurrent.locks.Lock;

import java.util.concurrent.locks.ReentrantLock;

public class LiveLock {
    // Вместо Object используем ReentrantLock
    public static final Lock lock1 = new ReentrantLock();
    public static final Lock lock2 = new ReentrantLock();

    public static void main(String[] args) {
        Thread thread1 = new Thread(new WorkerLive1());
        Thread thread2 = new Thread(new WorkerLive2());
        thread1.start();
        thread2.start();
    }
}

class WorkerLive1 implements Runnable {
    @Override
    public void run() {
        while (true) {
            // Поток 1 пытается захватить lock1
            if (LiveLock.lock1.tryLock()) {
                System.out.println("Поток 1: захватил lock1");

                try {
                    // Имитируем небольшую задержку перед попыткой взять второй замок
                    Thread.sleep(10);

                    System.out.println("Поток 1: пробует захватить lock2");

                    // Пытаемся захватить второй замок
                    if (LiveLock.lock2.tryLock()) {
                        try {
                            System.out.println("Поток 1: Захвачены оба замка.");
                            break;
                        } finally {
                            LiveLock.lock2.unlock();
                        }
                    } else {
                        // Если lock2 занят, Поток 1 отпускает свой lock1, чтобы не мешать другим
                        System.out.println("Поток 1: lock2 занят. Отпускаю lock1 и попробую позже.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    LiveLock.lock1.unlock();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

        }
    }
}

class WorkerLive2 implements Runnable {
    @Override
    public void run() {
        while (true) {
            // Поток 2 делает то же самое, но начинает с lock2
            if (LiveLock.lock2.tryLock()) {
                System.out.println("Поток 2: захватил lock2");

                try {
                    Thread.sleep(10);

                    System.out.println("Поток 2: пробует захватить lock1");
                    if (LiveLock.lock1.tryLock()) {
                        try {
                            System.out.println("Поток 2: Захвачены оба замка.");
                            break;
                        } finally {
                            LiveLock.lock1.unlock();
                        }
                    } else {
                        // Если lock1 занят, Поток 2 отпускает свой lock2
                        System.out.println("Поток 2: lock1 занят. Отпускаю lock2 и попробую позже.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    LiveLock.lock2.unlock();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

        }
    }
}