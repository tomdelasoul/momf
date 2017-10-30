package stock.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import stock.StockPrice;
import stock.StockPriceSeries;

public class StockPriceSeriesTest {
	StockPriceSeries series;
	Date day1;
	SimpleDateFormat sdf;

	@Before
	public void setUp() throws Exception {
		String historicPrices = "deutsche_bank2012.csv";
		this.series = new StockPriceSeries(); 
		this.series.loadSeriesFromCsvFile(historicPrices);
		sdf = new SimpleDateFormat("dd.MM.yy");
		day1 = sdf.parse("13.01.17");
	}

	@Test
	public void testGet() {
		int ctr = 0;
		for(Map.Entry<Date, StockPrice> entry : this.series.getDateSeries().entrySet()) {
			System.out.println(
					String.format("%04d", ctr) + ": "+sdf.format(entry.getKey()) + " "+ entry.getValue());
			if(ctr++ >= 100) {
				break;
			}
		}
		ctr = 0;
		for(Map.Entry<Long, StockPrice> entry : this.series.getTimeSeries().entrySet()) {
			System.out.println(entry.getKey() + " "+ entry.getValue());
			if(ctr++ >= 100) {
				break;
			}
		}
	}

}
