package stock;

import java.awt.Color;
import java.util.Date;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.TextAnchor;

/**
 * parent class for trading strategies - instantiate for individual strategies
 *
 */
public class Strategy {
	/**
	 * period in days that we look back to calculate buyGrad 
	 */
	public Integer buyGradDays;
	/**
	 * change in per-mill (1/1000) between stock price byGrad days before and today.
	 * 
	 * if a cumulated growth of buyDelta promille after BuyGrad days is achieved, we buy. e.g. 20 = 2%
	 */
	public Integer buyDeltaMille;
	/**
	 * period in days that we look back to calculate sellGrad 
	 */
	public Integer sellGradDays;
	/**
	 * Delta in per-mill (1/1000) between the stock price after sellGrad days.
	 * 
	 * if a cumulated growth of sellDelta promille after sellGrad days is achieved, we sell. e.g. 20 = 2%
	 */
	public Integer sellDeltaMille;
	/**
	 * Difference between the current stock price and the last buy, in negative percent
	 * 
	 * A negative percentage of last buy. if lower, we sell. eg. -0.05
	 */
	public Float stopLossPercent; 
	/**
	 * Difference between the current stock price and our last buy, in positive percent
	 * 
	 * a positive percentage of last buy. if higher, we sell. eg. 0.05
	 */
	public Float sellLimitPercent;
	
	public Strategy() {
			this.buyDeltaMille = null;
			this.buyGradDays = null;
			this.sellDeltaMille = null;
			this.sellGradDays = null;
			this.stopLossPercent = null;
			this.sellLimitPercent = null;
	}
	
	/**
	 * default fee logic for all strategies
	 * calculate fees for a transaction
	 * 
	 * @param priceBuy at which price did we buy
	 * @param numStocks number of stocks bought
	 * @return fees in EUR
	 */
	public static Float fees(Float priceBuy, Integer numStocks) {
		Float volume = priceBuy * numStocks;
		// own fees: 
		// fix EUR 7,95 +
		// Xetra Frankfurt, Börse München: 
		// 0,195% bis 9.999,-
		// 0,175% von 10.000,- bis 24.999,-
		// 0,175% von 10.000,- bis 24.999,-
		// 0,150% von 25.000,- bis 49.999,-
		// 0,125% von 50.000,- bis 74.999,-
		// 0,100% von 75.000,- bis 99.999,-
		// 0,080% ab 100.000,-
		Float ownFees = volume*0.00195f+7.95f; 
		// remote fees
		// Deutschland - Xetra
		// Provision: 0,04%, 0,08% bei ETFs
		// Mindestspesen: EUR 2,70; max. EUR 20,- 
		Float remoteFees = volume*0.00004f;
		remoteFees = (remoteFees < 2.7) ? 2.7f : remoteFees;
		remoteFees = (remoteFees > 20)  ? 20f  : remoteFees;
		return ownFees+remoteFees;
	}
	
	
	/**
	 * pretty printer for deal details
	 */
	public static void info(StockPrice price, boolean verbose, String trx, Integer gradCtr, Float delta, Float profitDeal,
			Float feesDeal, float VolumeDeal) {
		if(!verbose) {
			return;
		}
		System.out.print(price.getDateAsString()+" "+String.format("%.2f", price.getTrxPrice()));
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
		System.out.println();
	}

	/**
	 * add an annotation to a chart for a given Date
	 * 
	 * @param chart
	 */
	public void addMarkToChart(XYPlot plot, String text, Date date, Float price) {
        
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, Color.black);
        renderer.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        
        XYPointerAnnotation annotation = new XYPointerAnnotation(text, date.getTime(), price, Math.PI / 2.0);
        annotation.setTextAnchor(TextAnchor.TOP_CENTER);
        
        renderer.addAnnotation(annotation);
        plot.setRenderer(1, renderer);

	}

}
