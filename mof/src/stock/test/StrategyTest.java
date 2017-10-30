package stock.test;

import static org.junit.Assert.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import stock.StockPriceSeries;
import stock.StrategyMomentumFollower;
import stock.util.StrategyException;

public class StrategyTest {
	
	StockPriceSeries timeSeriesLinear;
	StockPriceSeries timeSeriesRandom;
	/**
	 * price time series with a linear up/down behaviour
	 */
	String testDataFileLinear;
	/**
	 * price time series with a random up-down behaviour
	 */
	String testDataFileRandom;
	/**
	 * we seed the generator in order to get repeatable results
	 */
	Random generator;
	/**
	 * where is our test file located?
	 */
	static String pathPrefix = "data/";
	/**
	 * what kind of test data shall we create?
	 */
	enum TestDataCharacteristics {LINEAR, RANDOM};
	

	@Before
	public void setUp() {
		this.generator = new Random(41567l);
		this.testDataFileLinear = "testDataLinear.csv";
		this.testDataFileRandom = "testDataRandom.csv";
		this.createTestDataFile(testDataFileLinear, TestDataCharacteristics.LINEAR);
		this.createTestDataFile(testDataFileRandom, TestDataCharacteristics.RANDOM);
	}
	
	
	/**
	 * test if test file exists and has been written to recently
	 * @param testDataFile
	 * @return
	 */
	private boolean testDataFileExists(String testDataFile) {
		File file = new File(pathPrefix+testDataFile);
		Date now = new Date();
		if(file.exists() && file.lastModified() > (now.getTime() - 10*1000)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private static String getRandomVolumeString() {
		Integer volume = Double.valueOf(10000000*Math.random()).intValue();
		return String.format("%d",volume);
	}

	@Test
	public void theTestDataFileWasCreated() {
		assertTrue(testDataFileExists(testDataFileLinear));
		assertTrue(testDataFileExists(testDataFileRandom));
	}
	
	@Test
	public void testTimeSeries() {
		try {
			// test our linear (but cyclic) time series.
			this.timeSeriesLinear = new StockPriceSeries(); 
			this.timeSeriesLinear.loadSeriesFromCsvFile(this.testDataFileLinear);
			
			StrategyMomentumFollower mof = new StrategyMomentumFollower();
			mof.buyGradDays = 3;
			mof.buyDeltaMille = 20;
			mof.sellGradDays = 8;
			mof.sellDeltaMille = 30;
			mof.stopLossPercent = -0.04f;
			
			assertTrue(185  == mof.run(this.timeSeriesLinear, null, false));
			
			mof.buyGradDays = 4;
			mof.sellGradDays = 6;
			
			assertTrue(215  == mof.run(this.timeSeriesLinear, null, false));
			
			mof.sellGradDays = 9;
			
			assertTrue(3147  == mof.run(this.timeSeriesLinear, null, false));
			
			mof.sellGradDays = 8;
			mof.sellDeltaMille = 31;
			
			assertTrue(3199  == mof.run(this.timeSeriesLinear, null, false));
			
			
			// test our random time series
			this.timeSeriesRandom = new StockPriceSeries();
			this.timeSeriesRandom.loadSeriesFromCsvFile(this.testDataFileRandom);
			
			mof.buyGradDays = 2;
			mof.buyDeltaMille = 20;
			mof.sellGradDays = 5;
			mof.sellDeltaMille = 50;
			mof.stopLossPercent = -0.04f;
			
			assertTrue(6304  == mof.run(this.timeSeriesRandom, null, false));
			
			mof.sellDeltaMille = 51;
			
			assertTrue(6664  == mof.run(this.timeSeriesRandom, null, false));
			
			mof.sellDeltaMille = 52;
			
			assertTrue(7212  == mof.run(this.timeSeriesRandom, null, false));	
			
			mof.sellDeltaMille = 58;
			
			assertTrue(8038  == mof.run(this.timeSeriesRandom, null, false));

		}
		catch(FileNotFoundException fe) {
			assertTrue(false);
		} catch (StrategyException e) {
			assertTrue(false);
		}
		assertTrue(true);
	}
	
	/**
	 * create test price time series data
	 * @param fileName
	 */
	public void createTestDataFile(String fileName, TestDataCharacteristics tdc) {
		Charset charset = Charset.forName("UTF-8");
		Path path = new File(pathPrefix+fileName).toPath();
		String line;
		try {
			BufferedWriter writer = Files.newBufferedWriter(path, charset);
			String header = "Datum;Eröffnung;Schluss;Tageshoch;Tagestief;Volumen\n";
			writer.write(header, 0, header.length());
			Float highPrice = 100f;
			Float lowPrice = 100f;
			boolean directionUp = false; // we start with an upward trend
			GregorianCalendar start = new GregorianCalendar(2016, 01, 01, 12, 0);
			for(int lineNr = 0; lineNr < 365; lineNr++) {
				// add day, skip weekends
				start.add(GregorianCalendar.DAY_OF_MONTH, 1);
				if(start.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY) 
					start.add(GregorianCalendar.DAY_OF_MONTH, 2);
				if(start.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY) 
					start.add(GregorianCalendar.DAY_OF_MONTH, 1);
				String dateString = 
					String.format("%02d", start.get(GregorianCalendar.DAY_OF_MONTH)) +"."+
					String.format("%02d", start.get(GregorianCalendar.MONTH))+"."+
					String.format("%04d", start.get(GregorianCalendar.YEAR));
				// csv file line assembly
				line = 
						dateString +
						";100;100;"+
						String.format("%03.2f",highPrice)+";"+
						String.format("%03.2f",lowPrice)+";"+
						StrategyTest.getRandomVolumeString()+
						"\n";
				writer.write(line, 0, line.length());
				
				// now change direction and/or gradient
				int changeDirectionEvery = 0;
				if(tdc == TestDataCharacteristics.LINEAR) {
					changeDirectionEvery = 8;
				}
				if(tdc == TestDataCharacteristics.RANDOM) {
					changeDirectionEvery = 1+this.generator.nextInt(12);
				}
				if(0 == lineNr%changeDirectionEvery) {
					directionUp = !directionUp;
				}
				if(tdc == TestDataCharacteristics.RANDOM) {
					Float randomPercentage = new Float(this.generator.nextDouble()*0.02f);
					
					if(directionUp) {
						highPrice 	= highPrice * (1+randomPercentage);
						lowPrice 	= lowPrice  * (1+randomPercentage);
					}
					else {
						highPrice 	= highPrice / (1+randomPercentage);
						lowPrice 	= lowPrice  / (1+randomPercentage);					
					}
				}
				if(tdc == TestDataCharacteristics.LINEAR) {
					if(directionUp) {
						highPrice 	= highPrice * 1.005f;
						lowPrice 	= lowPrice  * 1.005f;
					}
					else {
						highPrice 	= highPrice / 1.005f;
						lowPrice 	= lowPrice  / 1.005f;				
					}
				}
			}
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}

}
