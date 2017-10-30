package stock;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * a series of stock prices
 */
public class StockPriceSeries {
	private SimpleDateFormat sdf;
	/**
	 * a description of the stock price series
	 */
	private String description;
	/**
	 * a sorted time series of stock prices that is indexed with the date of the price
	 * used to sort time series records when reading from files
	 */
	private TreeMap<Date, StockPrice> dateSeries;
	/**
	 * a sorted time series of stock prices that is indexed with a counter (0...n)
	 * used for calculation in time (e.g. looking back)
	 */
	private TreeMap<Long, StockPrice> timeSeries;
	
	public StockPriceSeries() {
		super();
		this.dateSeries = new TreeMap<Date, StockPrice>();
		this.timeSeries = new TreeMap<Long, StockPrice>();
		this.sdf = new SimpleDateFormat("dd.MM.yy");
	}
	
	public TreeMap<Date, StockPrice> getDateSeries() {
		return this.dateSeries;
	}
	
	public TreeMap<Long, StockPrice> getTimeSeries() {
		return this.timeSeries;
	}
	
	public int size() {
		return this.dateSeries.size();
	}
	
	public void loadSeriesFromCsvFile(String fileName) throws FileNotFoundException {		
		Scanner scanner = new Scanner(new File("data/"+fileName));
		scanner.useLocale(Locale.GERMAN);
        scanner.useDelimiter(";");
        int lineCtr = 0;
        try {
	        scanner.nextLine(); // skip first line, header
	        while(scanner.hasNextLine()) {
	        	StockPrice price = new StockPrice();
	        	price.setDate(sdf.parse(scanner.next().trim()));
	        	price.setOpen(scanner.nextFloat());
	        	price.setClose(scanner.nextFloat());
	        	price.setMax(scanner.nextFloat());
	        	price.setMin(scanner.nextFloat());
	        	// price.setVolume(scanner.nextLong()); // TODO - cannot read volume from file
	            scanner.nextLine(); // advance by one line
	            lineCtr++;
	            this.dateSeries.put(price.getDate(), price);
	        }
        }
        catch(InputMismatchException ex) {
        	System.err.println("cannot parse line #: "+lineCtr + " " + ex.getMessage());
        } catch (ParseException e) {
        	System.err.println("cannot parse date in line #: "+lineCtr);
		}
        scanner.close();
        
        Long timeSeriesCtr = 0l;
		for(Map.Entry<Date, StockPrice> entry : this.dateSeries.entrySet()) {
			StockPrice price = entry.getValue();
			this.timeSeries.put(timeSeriesCtr++, price);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
