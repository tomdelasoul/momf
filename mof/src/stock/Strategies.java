package stock;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

import stock.graph.CandleChart;
import stock.util.StrategyException;

/**
 * test various buy/sell strategies on historic series of stock price data
 */
public class Strategies {	
	private JFreeChart chart;
	XYPlot plot;
	
	public static Strategies testStrategyWithHistoricPrices(int threadCtr) {
		return new Strategies(threadCtr);
	}
	
	private Strategies(int threadCtr) {
		super();
		try {
			// String historicPrices = "xetra_gold2012.csv";
			// String historicPrices = "deutsche_telekom2012.csv";
			String deutsche = "deutsche_bank2012.csv";
			String deutscheShort = "deutsche_bank2012only.csv";
			// String historicPrices = "bmw_2012.csv";
			// String historicPrices = "Volkswagen_vz2012.csv";
			// String random = "testDataRandom.csv";
			
			StockPriceSeries series;

			
			series = new StockPriceSeries(); 
			
			series.loadSeriesFromCsvFile(deutscheShort); 
			series.setDescription(deutscheShort);
	      
			CandleChart candleChart = new CandleChart(deutscheShort); // TODO: make series an argument to CandleChart, no need to load series in Chart and Strategy twice
			
	        candleChart.pack();
	        RefineryUtilities.centerFrameOnScreen(candleChart);
	        candleChart.setVisible(true);
	        this.chart = candleChart.getChart();
	        this.plot = (XYPlot) chart.getPlot();
	        
	        
			//  this.momentumFollowerEnumerator(series);
			this.momentumFollowerTwoEnumerator(series);
		} catch (FileNotFoundException | StrategyException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * find the best parameters for the momentumFollower strategy by enumerating parameters.
	 * 
	 * @throws StrategyException
	 * @throws FileNotFoundException
	 */
	public void momentumFollowerEnumerator(StockPriceSeries series) throws StrategyException, FileNotFoundException {

		long profitMax = 0l; // remember the maximum profit
		int widthOfDays = 15; // maximum number of days we wait to buy and wait to sell
		
		for(Float stopLoss = -0.04f; stopLoss >= -0.15f; stopLoss-=0.01f) { // stop-loss in percentage
			for(int buyDelta = 20; buyDelta <= 40; buyDelta++) { // momentum to be achieved when we buy
				for(int sellDeltaAdd = 10; sellDeltaAdd <= 40; sellDeltaAdd++) { // additional momentum to be achieved to sell
					for(int buyGrad = 1; buyGrad <= widthOfDays; buyGrad++) { // days we look back for momentum to buy
						for(int sellGrad = buyGrad+1; sellGrad <= buyGrad+widthOfDays; sellGrad++) { // days we look back for momentum to sell
							
							
							StrategyMomentumFollower mof = new StrategyMomentumFollower();
							mof.buyGradDays = buyGrad;
							mof.buyDeltaMille = buyDelta;
							mof.sellGradDays = sellGrad;
							mof.sellDeltaMille = buyDelta+sellDeltaAdd;
							mof.stopLossPercent = stopLoss;
							long profit = mof.run(series, this.plot, false);
							
							// long profit = Strategies.momentumFollower(series, false, buyGrad, buyDelta, sellGrad, buyDelta+sellDeltaAdd, stopLoss);
							if(profit > profitMax) {
								System.out.print(series.getDescription());
								System.out.println(String.format(" mf: %d %d %d %d %.2f %d", buyGrad, buyDelta, sellGrad, buyDelta+sellDeltaAdd, stopLoss, Math.round(profit)));
								profitMax = profit;
							}			
						}
					}
				}
			}
		}
		System.out.println("-- mf finished for: "+series.getDescription());
	}


	/**
	 * find the best parameters for the momentumFollower2 strategy by enumerating parameters.
	 * 
	 * @throws StrategyException
	 * @throws FileNotFoundException
	 */
	public void momentumFollowerTwoEnumerator(StockPriceSeries series) throws StrategyException, FileNotFoundException {
	{
		long profitMax = 0l; // remember the maximum profit
		
		for(Float stopLoss = -0.02f; stopLoss >= -0.15f; stopLoss-=0.01f) { // stop-loss in percentage
			for(int buyDelta = 2; buyDelta <= 40; buyDelta++) { // momentum to be achieved when we buy
					for(int buyGrad = 1; buyGrad <= 20; buyGrad++) { // days we look back for momentum to buy
						for(Float winPercent = 0f; winPercent <= 0.20f; winPercent += 0.01f) {
							
							StrategyMomentumFollowerTwo mof2 = new StrategyMomentumFollowerTwo();
							mof2.buyGradDays = buyGrad;
							mof2.buyDeltaMille = buyDelta;
							mof2.stopLossPercent = stopLoss;
							mof2.sellLimitPercent = winPercent;
							long profit = mof2.run(series, this.plot, false);
							
							// long profit = Strategies.momentumFollowerTwo(series, false, buyGrad, buyDelta, stopLoss, winPercent);
							if(profit > profitMax) {
								System.out.print(series.getDescription());
								System.out.println(String.format(" mf2: %d %d %.2f %.2f %d", buyGrad, buyDelta, stopLoss, winPercent, Math.round(profit)));
								profitMax = profit;
								
							}			
						}
					}
				}
			}
		}
	System.out.println("-- mf2 finished for: "+series.getDescription());
	}

	private void dumpHistoricPrices(StockPriceSeries series) {
		for (Map.Entry<Long, StockPrice> entry : series.getTimeSeries().entrySet()) {
			StockPrice price = entry.getValue();
			System.out.print("k: "+entry.getKey());
			System.out.print(" "+price);
			System.out.println();
		}
	}

	/**
	 * calculate momentum
	 */
	private void statistics(StockPriceSeries series) {
		for (Map.Entry<Long, StockPrice> entry : series.getTimeSeries().entrySet()) {
			StockPrice price = entry.getValue();
			Long key = entry.getKey();
			System.out.print(key+";"+price.getDateAsString()+";"+price.getTrxPrice()+";");
			// now calculate delta closing prices to the previous closing prices
			Integer maxLookBackInDays = 7;
			Float sumDelta = 0f; // for average calc
			for(Integer gradCtr = 1; gradCtr<=maxLookBackInDays; gradCtr++) {
				if(key >= maxLookBackInDays) {
					Float priceCloseInPast = series.getTimeSeries().get(key-gradCtr).getTrxPrice();
					Float delta = 1000*(price.getTrxPrice()-priceCloseInPast)/price.getTrxPrice();
					sumDelta += delta;
				}
			}
			System.out.print(Math.round(sumDelta/maxLookBackInDays));
			System.out.println();
		}
	}

	private static void info(boolean verbose, String trx, Integer gradCtr, Float delta, Float profitDeal,
			Float feesDeal, float VolumeDeal) {
		if(!verbose) {
			return;
		}
		System.out.print(
				" "+
						trx+
						" ["+
						gradCtr+
						":"+
						String.format("%3.0f", delta)+
						"]"
				);
		switch(trx) {
		case "B":
			// break;
		case "S":
		case "L":
			System.out.print(
					" € "+
							String.format("%.02f", profitDeal) +
							"-"+
							String.format("%.02f", feesDeal) +
							"("+
							String.format("%3d", Math.round(100*profitDeal/VolumeDeal))
							+"%)"
					);
			break;
		default:
			System.err.printf("unknown trx type %s", trx);
			break;
		}
		
	}


}
