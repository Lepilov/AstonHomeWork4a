
public class DeadLock {
    // Создаем два объекта-заглушки лок, которые потоки будут пытаться заблокировать
    public static final Object lock1 = new Object();
    public static final Object lock2 = new Object();

    public static void main(String[] args) {
        Thread thread1 = new Thread(new WorkerDead1());
        Thread thread2 = new Thread(new WorkerDead2());
        thread1.start();
        thread2.start();
    }
}

class WorkerDead1 implements Runnable {
    @Override
    public void run() {
        System.out.println("Поток1: попытка захватить монитор объекта lock1");
        // Поток 1 успешно захватывает первый монитор lock1
        synchronized (DeadLock.lock1) {
            System.out.println("Поток1: монитор объекта lock1 захвачен");

            // Искусственная пауза. Она нужна для того, чтобы Поток 2 успел
            // проснуться и захватить свой первый монитор lock2 до того, как Поток 1 пойдет дальше.
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Поток 1 пытается захватить lock2, но он уже занят Потоком 2.
            // Поток 1 засыпает в состоянии BLOCKED, ожидая освобождения lock2.
            System.out.println("Поток1: попытка захватить монитор объекта lock2");
            synchronized (DeadLock.lock2) {
                System.out.println("Поток1: мониторы объектов захвачены");
            }
        }
    }
}

class WorkerDead2 implements Runnable {
    @Override
    public void run() {
        System.out.println("Поток2: попытка захватить монитор объекта lock2");
        // Поток 2 параллельно успешно захватывает свой первый монитор lock2
        synchronized (DeadLock.lock2) {
            System.out.println("Поток2: монитор объекта lock2 захвачен");

            // Искусственная пауза. Пока Поток 2 спит, Поток 1 просыпается
            // и упирается в закрытый lock2.
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Поток 2 просыпается и пытается захватить lock1, который все еще удерживает Поток 1.
            // Поток 2 тоже засыпает в состоянии BLOCKED.
            // В этот момент наступает Deadlock: оба потока спят и вечно ждут освобождения ресурсов друг друга.
            System.out.println("Поток2: попытка захватить монитор объекта lock1");
            synchronized (DeadLock.lock1) {
                System.out.println("Поток2: мониторы объектов захвачены");
            }
        }
    }
}
