package stock;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import stock.util.StrategyException;

public interface StrategyInterface {
	/**
	 * run a strategy 
	 * 
	 * @param series time series 
	 * @param verbose  true if verbose, false if not
	 * @return
	 * @throws StrategyException
	 */
	public long run (
			StockPriceSeries series, 
			XYPlot plot,
			boolean verbose
			) throws StrategyException;
	

}
