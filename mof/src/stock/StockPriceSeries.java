package stock;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * a series of stock prices
 */
public class StockPriceSeries {
	/**
	 * a series of stock prices
	 */
	private HashMap<Integer, StockPrice> series;
	
	public StockPriceSeries() {
		super();
		this.series = new HashMap<Integer, StockPrice>();
	}
	
	public HashMap<Integer, StockPrice> getSeries() {
		return this.series;
	}
	
	public void loadSeriesFromCsvFile(String fileName) throws FileNotFoundException {		
		Scanner scanner = new Scanner(new File("data/"+fileName));
        scanner.useDelimiter(";");
        int lineCtr = 0;
        try {
	        scanner.nextLine(); // skip first line, header
	        while(scanner.hasNextLine()) {
	        	StockPrice price = new StockPrice();
	        	price.setDate(scanner.next().trim());
	        	price.setOpen(scanner.nextFloat());
	        	price.setClose(scanner.nextFloat());
	        	price.setMax(scanner.nextFloat());
	        	price.setMin(scanner.nextFloat());
	            // System.out.println(price);
	            this.series.put(lineCtr++, price);
	            scanner.nextLine(); // advance by one line
	        }
        }
        catch(InputMismatchException ex) {
        	System.err.println("cannot parse line #: "+lineCtr);
        }
        scanner.close();
	}
	
}
