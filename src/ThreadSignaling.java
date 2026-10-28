
public class ThreadSignaling {
    //2) Создать два потока (поток 1 постоянно выводит "1", а поток 2 выводит "2"),
    // которые будут бесконечно выводить в консоль "1" и "2" по очереди(начиная с "1")

    public static void main(String[] args) {
        Printer printer = new Printer();
        Worker worker1 = new Worker(printer, 1);
        Worker worker2 = new Worker(printer, 2);
        Thread thread1 = new Thread(worker1);
        Thread thread2 = new Thread(worker2);
        thread1.start();
        thread2.start();
    }
}

class Printer {
    private int active = 1;

    public synchronized void output(int num) {
        while (active != num) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Критическая ошибка: поток вывода был прерван", e);
            }
        }

        System.out.println(num);
        active = (num == 1) ? 2 : 1;
        notifyAll();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Критическая ошибка: задержка потока была прервана", e);
        }
    }
}

class Worker implements Runnable {
    private final Printer printer;
    private final int num;

    public Worker(Printer printer, int num) {
        this.printer = printer;
        this.num = num;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            printer.output(num);
        }
    }
}
