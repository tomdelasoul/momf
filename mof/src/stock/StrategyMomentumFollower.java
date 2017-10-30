package stock;

import java.util.Map.Entry;

import org.jfree.chart.plot.XYPlot;

import stock.util.StrategyException;
import stock.util.TrxStatus;
/**
 * a strategy that buys after a momentum trigger ans sells after a profit margin trigger
 *
 */
public class StrategyMomentumFollower extends Strategy implements StrategyInterface {

	public StrategyMomentumFollower() {
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
		Float profitDeal = 0f; // profit per deal
		Float profitTotal = 0f; // total profit
		Float feesDeal = 0f; // fee per deal
		Float feesTotal = 0f; // total fees we have to pay
		Integer numBuyTrx = 0; // number of buy transactions
		Integer numSellTrx = 0; // number of buy transactions
		Float priceBuySum = 0f; // sum of all prices where we bought
		
		if(this.sellGradDays <= this.buyGradDays) {
			throw new StrategyException("sellGrad ("+this.sellGradDays+") is equal or below buyGrad ("+this.buyGradDays+")");
		}
		int maxLookBackInDays = this.sellGradDays; // how many days do we look back to calculate momentum?
		
		if(stopLossPercent > 0) {
			throw new StrategyException("stopLossPercent need to be negative, but it is "+stopLossPercent);
		}

		for (Entry<Long, StockPrice> entry : series.getTimeSeries().entrySet()) {
			StockPrice price = entry.getValue();
			Long key = entry.getKey();
			if(verbose) System.out.print(price.getDateAsString()+" "+String.format("%.2f", price.getTrxPrice()));
			// now calculate delta closing prices to the previous closes
			for(Integer gradCtr = 1; gradCtr<=maxLookBackInDays; gradCtr++) {
				Float priceInPast = 0f;
				if(key >= gradCtr) {
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
					}
					// stop loss
					if(		 TrxStatus.STOCK_BOUGHT  == trx && 
							(price.getTrxPrice() - priceBuy)/(priceBuy) < stopLossPercent
							) {
						profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
						profitTotal += profitDeal;
						feesDeal =  Strategy.fees(price.getTrxPrice(), numStocks);
						feesTotal += feesDeal;
						numSellTrx++;
						trx = TrxStatus.STOCK_SOLD;
						Strategy.info(price,verbose, "L", gradCtr, delta, profitDeal, feesDeal, price.getTrxPrice()*numStocks);
					}
					// sell
					if(		TrxStatus.STOCK_BOUGHT == trx &&
							gradCtr == this.sellGradDays &&
							delta >= this.sellDeltaMille &&
							(price.getTrxPrice() > priceBuy)
							) {
						profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
						profitTotal += profitDeal;
						feesDeal =  Strategy.fees(price.getTrxPrice(), numStocks);
						feesTotal += feesDeal;
						numSellTrx++;
						trx = TrxStatus.STOCK_SOLD;
						Strategy.info(price, verbose, "S", gradCtr, delta, profitDeal, feesDeal, price.getTrxPrice()*numStocks);
					}
				}					
			}
			if(verbose) System.out.println();
		}
		Float averagePriceBought = priceBuySum/numBuyTrx;
		// Float profitPercent = 100*profitTotal/(priceBuySum/numBuyTrx)/numStocks;
		if(verbose) {
			System.out.println(
					"avg p: "+String.format("%.2f", averagePriceBought) +
					" income: "+String.format("%.2f", profitTotal)+" "+
					" fees: "+String.format("%.2f", feesTotal)+" "+
					" profit: "+String.format("%.2f", profitTotal-feesTotal)+" "+
					" "+
					numBuyTrx+"-"+numSellTrx+
					" "+
					trx);
		}
		return Math.round(profitTotal-feesTotal);
	}

}
