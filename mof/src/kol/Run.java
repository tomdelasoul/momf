package kol;

import stock.Strategies;

public class Run implements Runnable {
	private static int threadCtr = 0;
	
	public static void main(String[] args) throws Exception {
		/*
		Thread t2 = new Thread(new Run());
		threadCtr = 2; // thread 1 is the main thread
		t2.start();
		String threadName = Thread.currentThread().getName();
	    System.out.println("thread: " + threadName);
		*/
		Strategies.testStrategyWithHistoricPrices(1);
		// new Quote();
	}

	@Override
	public void run() {
		String threadName = Thread.currentThread().getName();
	    System.out.println("thread: " + threadName + " ctr: " + Run.threadCtr);
	    Strategies.testStrategyWithHistoricPrices(Run.threadCtr);
	}

}
