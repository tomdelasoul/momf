package stock;

import java.util.Map.Entry;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import stock.util.StrategyException;
import stock.util.TrxStatus;
/**
 * a strategy that buys after a momentum trigger ans sells after a profit margin trigger
 *
 */
public class StrategyMomentumFollowerTwo extends Strategy implements StrategyInterface {

	public StrategyMomentumFollowerTwo() {
		super();
	}

	/**
	 * a strategy that buys after a certain start momentum, and sells after a gain target
	 * 
	 * @param series
	 * @return profit in EUR
	 * @throws Exception 
	 */
	public long run (
			StockPriceSeries series,
			XYPlot plot,
			boolean verbose // yes = verbose, no = not
			) throws StrategyException {
	
		TrxStatus trx = TrxStatus.STOCK_SOLD; // we start empty
		Integer numStocks = 100; // how many stocks do we buy 
		Float priceBuy = 0f; // the last price at which we bought
		Float priceSell = 0f; // the price at which we sold
		Float profitDeal = 0f; // profit per deal
		Float profitTotal = 0f; // total profit
		Float feesDeal = 0f; // fee per deal
		Float feesTotal = 0f; // total fees we have to pay
		Integer numBuyTrx = 0; // number of buy transactions
		Integer numSellTrx = 0; // number of buy transactions
		Float priceBuySum = 0f; // sum of all prices where we bought
		Float priceSellSum = 0f; // sum of all prices we sold
		
		int maxLookBackInDays = this.buyGradDays; // how many days do we look back to calculate momentum?
		
		if(this.stopLossPercent > 0) {
			throw new StrategyException("stopLossPercent needs to be negative, but it is "+stopLossPercent);
		}
		if(this.sellLimitPercent < 0) {
			throw new StrategyException("sellLimitPercent needs to be positive, but it is "+sellLimitPercent);
		}
	
		for (Entry<Long, StockPrice> entry : series.getTimeSeries().entrySet()) {
			// iterate over time
			StockPrice price = entry.getValue();
			Long key = entry.getKey();
			Float priceInPast = 0f;
			// now calculate delta closing prices to the previous closes
			for(Integer gradCtr = 1; gradCtr<=maxLookBackInDays; gradCtr++) {
				if(key < gradCtr) {
					continue;
				}
				priceInPast = series.getTimeSeries().get(key-gradCtr).getTrxPrice();
				Float delta = 1000f*((price.getTrxPrice()-priceInPast)/priceInPast);
				// buy
				if(		TrxStatus.STOCK_SOLD == trx &&
						gradCtr == this.buyGradDays && 
						delta >= this.buyDeltaMille
						) {
					priceBuy = price.getTrxPrice();
					priceBuySum += priceBuy;
					numBuyTrx++;
					feesDeal = Strategy.fees(priceBuy, numStocks);
					feesTotal += feesDeal;
					trx = TrxStatus.STOCK_BOUGHT;
					Strategy.info(price, verbose, "B", gradCtr, delta, 0f, feesDeal, priceBuy*numStocks);
					if(null != plot)
						this.addMarkToChart(plot, "B", price.getDate(), price.getMin());
				}
				// stop loss
				if(		 TrxStatus.STOCK_BOUGHT  == trx && 
						(price.getTrxPrice() - priceBuy)/(priceBuy) < this.stopLossPercent
						) {
					profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
					profitTotal += profitDeal;
					priceSell = price.getTrxPrice();
					priceSellSum += priceSell;
					feesDeal =  Strategy.fees(priceSell, numStocks);
					feesTotal += feesDeal;
					numSellTrx++;
					trx = TrxStatus.STOCK_SOLD;
					Strategy.info(price, verbose, "L", gradCtr, delta, profitDeal, feesDeal, price.getTrxPrice()*numStocks);
					if(null != plot)
						this.addMarkToChart(plot, "L", price.getDate(), price.getMin());
				}
				// sell
				if(		TrxStatus.STOCK_BOUGHT == trx &&
						(price.getTrxPrice() - priceBuy)/(priceBuy) > this.sellLimitPercent
						) {
					profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
					profitTotal += profitDeal;
					priceSell = price.getTrxPrice();
					priceSellSum += priceSell;
					feesDeal =  Strategy.fees(priceSell, numStocks);
					feesTotal += feesDeal;
					numSellTrx++;
					trx = TrxStatus.STOCK_SOLD;
					Strategy.info(price, verbose, "S", gradCtr, delta, profitDeal, feesDeal, price.getTrxPrice()*numStocks);
					if(null != plot)
						this.addMarkToChart(plot, "S", price.getDate(), price.getMin());
				}
			}
		}
		Float averagePriceBought = priceBuySum/numBuyTrx;
		// Float profitPercent = 100*profitTotal/averagePriceBought;
		if(verbose) {
			System.out.println(
					"avg p: "+String.format("%.2f", averagePriceBought) +
					" income: "+String.format("%.2f", profitTotal)+" "+
					" fees: "+String.format("%.2f", feesTotal)+" "+
					" profit: "+String.format("%.2f", profitTotal-feesTotal)+" "+
					" "+
					numBuyTrx+"-"+numSellTrx+
					" "+
					((trx == TrxStatus.STOCK_SOLD) ? "short" : "long")
					);
			System.out.println("days: "+series.getTimeSeries().size()+" priceSellSum "+priceSellSum+ " priceBuySum: "+priceBuySum);
			System.out.println(
					"total profit: "+
					String.format("%.1f", (profitTotal-feesTotal)/priceBuySum)+"% "+
					"profit p.a.: " + 
					String.format("%.1f",(profitTotal-feesTotal)/priceBuySum/(series.getTimeSeries().size()/200))+"%"
			);
			
		}
		return Math.round(profitTotal-feesTotal);
	}

}
