package stock;

import static org.junit.Assert.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class StrategyTest {
	
	StockPriceSeries series;
	String testDataFile;

	@Before
	public void setUp() {
		this.testDataFile = "testData.csv";
		StrategyTest.createTestDataFile(testDataFile);
	}
	
	/**
	 * @param dayOffset days after the 1.1.2016
	 * @return
	 */
	private static String getDateString(int dayOffset) {
		GregorianCalendar start = new GregorianCalendar(2016, 01, 01, 12, 0);
		start.add(GregorianCalendar.DAY_OF_MONTH, dayOffset);
		String dateString = 
			String.format("%02d", start.get(GregorianCalendar.DAY_OF_MONTH)) +"."+
			String.format("%02d", start.get(GregorianCalendar.MONTH))+"."+
			String.format("%04d", start.get(GregorianCalendar.YEAR));
		return dateString; 
	}
	
	private static String getRandomVolumeString() {
		Integer volume = Double.valueOf(10000000*Math.random()).intValue();
		return String.format("%d",volume);
	}

	@Test
	public void testTimeSeries() {
		try {
			this.series = new StockPriceSeries(); 
			series.loadSeriesFromCsvFile(this.testDataFile);
			// 3 20 18 38 -0,04 
			Strategy.momentumFollower(series, true, 3, 20, 5, 30, -0.04f);
			assertTrue(true);
		}
		catch(StrategyException se) {
			assertTrue(false);
		}
		catch(FileNotFoundException fe) {
			assertTrue(false);
		}
		assertTrue(true);
	}
	
	private static Float randomGrowthPercentage() {
		return new Float(Math.random()*0.04f);
	}
	
	public static void createTestDataFile(String fileName) {
		Charset charset = Charset.forName("UTF-8");
		Path path = new File("data/"+fileName).toPath();
		String line;
		try {
			BufferedWriter writer = Files.newBufferedWriter(path, charset);
			String header = "Datum;Eröffnung;Schluss;Tageshoch;Tagestief;Volumen\n";
			writer.write(header, 0, header.length());
			Float highPrice = 100f;
			Float lowPrice = 100f;
			boolean directionUp = false; // we start with an upward trend
			for(int lineNr = 0; lineNr < 356; lineNr++) {
				line = 
						StrategyTest.getDateString(lineNr)+
						";100;100;"+
						String.format("%03.2f",highPrice)+";"+
						String.format("%03.2f",lowPrice)+";"+
						StrategyTest.getRandomVolumeString()+
						"\n";
				writer.write(line, 0, line.length());
				// now change direction and/or gradient
				if(0 == lineNr%8) {
					directionUp = !directionUp;
				}
				if(directionUp) {
					highPrice 	= highPrice * (1+StrategyTest.randomGrowthPercentage());
					lowPrice 	= lowPrice  * (1+StrategyTest.randomGrowthPercentage());
				}
				else {
					highPrice 	= highPrice / (1+StrategyTest.randomGrowthPercentage());
					lowPrice 	= lowPrice  / (1+StrategyTest.randomGrowthPercentage());					
				}
			}
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}

}
