package stock;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * test various buy/sell strategies on historic series of stock price data
 * 
 * @author tomdelasoul
 */
public class Strategy {	
	enum TrxType {
		BOUGHT, // invested
		SOLD, // not invested
	};
	
	
	public Strategy() {
		super();
		try {
			this.momentumFollowerEnumerator();
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
	public void momentumFollowerEnumerator() throws StrategyException, FileNotFoundException {
		// String historicPrices = "xetra_gold2012.csv";
		// String historicPrices = "deutsche_telekom2012.csv";
		// String historicPrices = "deutsche_bank2012.csv";
		String historicPrices = "Volkswagen_vz2012.csv";
		// String historicPrices = "testData.csv";
		
		StockPriceSeries series = new StockPriceSeries(); 
		series.loadSeriesFromCsvFile(historicPrices);

		Float profitMax = 0f; // remember the maximum profit
		int widthOfDays = 15; // maximum number of days we wait to buy and wait to sell
		
		for(Float stopLoss = -0.04f; stopLoss >= -0.15f; stopLoss-=0.01f) { // stop-loss in percentage
			for(int buyDelta = 20; buyDelta <= 40; buyDelta++) { // momentum to be achieved when we buy
				for(int sellDeltaAdd = 10; sellDeltaAdd <= 40; sellDeltaAdd++) { // additional momentum to be achieved to sell
					// System.out.println("bd "+buyDelta);
					for(int buyGrad = 1; buyGrad <= widthOfDays; buyGrad++) { // days we look back for momentum to buy
						// System.out.print("B "+buyGrad+": ");
						for(int sellGrad = buyGrad+1; sellGrad <= buyGrad+widthOfDays; sellGrad++) { // days we look back for momentum to sell
							Float profit = Strategy.momentumFollower(series, false, buyGrad, buyDelta, sellGrad, buyDelta+sellDeltaAdd, stopLoss);
							if(profit > profitMax) {
								System.out.println(String.format("Max: %d %d %d %d %.2f %4.0f", buyGrad, buyDelta, sellGrad, buyDelta+sellDeltaAdd, stopLoss, profit));
								profitMax = profit;
							}
							// System.out.print(String.format("%4.0f", profit));				
						}
						// System.out.println();
					}
				}
			}
		}
		System.out.println("---");
		// this.momentumFollower(series, true, 8, 24, 14, 34, -0.05f);
	}

	

	/**
	 * a strategy that buys after a certain start momentum, and sells after a certain end momentum
	 * 
	 * @param series
	 * @return profit in EUR
	 * @throws Exception 
	 */
	public static Float momentumFollower (
			StockPriceSeries series, 
			boolean verbose, // yes = verbose, no = not
			int buyGrad, // after how many days have we reached. e.g. 5
			int buyDelta, // a cumulated growth of buyDelta promille, so we buy. e.g. 20 = 2%
			int sellGrad, // after how many days after a buy have we reached. e.g. 7
			int sellDelta, // a cumulated growth of seelDelta promille, so we sell. eg.g 30 = 3%
			Float stopLossPercent // a negative percentage of last buy. if higher, we sell. eg. -0.05
			) throws StrategyException {

		TrxType trx = TrxType.SOLD; // we start empty
		Integer numStocks = 100; // how many stocks do we buy 
		Float priceBuy = 0f; // the last price at which we bought
		Float profitDeal = 0f; // profit per deal
		Float profitTotal = 0f; // total profit
		Float feesDeal = 0f; // fee per deal
		Float feesTotal = 0f; // total fees we have to pay
		Integer numBuyTrx = 0; // number of buy transactions
		Integer numSellTrx = 0; // number of buy transactions
		Float priceBuySum = 0f; // sum of all prices where we bought

		int maxLookBackInDays = buyGrad + sellGrad; // how many days do we look back to calculate momentum?

		if(sellGrad <= buyGrad) {
			throw new StrategyException("sellGrad ("+sellGrad+") is equal or below buyGrad ("+buyGrad+")");
		}
		if(stopLossPercent > 0) {
			throw new StrategyException("stopLossPercent need to be negative, but it is "+stopLossPercent);
		}

		for (Map.Entry<Integer, StockPrice> entry : series.getSeries().entrySet()) {
			StockPrice price = entry.getValue();
			Integer key = entry.getKey();
			if(verbose) System.out.print(price.getDate()+" "+String.format("%.2f", price.getTrxPrice()));
			// now calculate delta closing prices to the previous closes
			for(Integer gradCtr = 1; gradCtr<=maxLookBackInDays; gradCtr++) {
				Float priceInPast = 0f;
				if(key >= gradCtr) {
					priceInPast = series.getSeries().get(key-gradCtr).getTrxPrice();
					Float delta = 1000f*((price.getTrxPrice()-priceInPast)/priceInPast);
					// buy
					if(		TrxType.SOLD == trx &&
							gradCtr == buyGrad && 
							delta >= buyDelta
							) {
						if(verbose) Strategy.printTrx("B", gradCtr, delta);
						priceBuy = price.getTrxPrice();
						priceBuySum += priceBuy;
						numBuyTrx++;
						feesTotal += Strategy.fees(priceBuy, numStocks);
						trx = TrxType.BOUGHT;
						
					}
					// stop loss
					if(		 TrxType.BOUGHT  == trx && 
							(price.getTrxPrice() - priceBuy)/(priceBuy) < stopLossPercent
							) {
						profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
						profitTotal += profitDeal;
						feesDeal =  Strategy.fees(priceBuy, numStocks);
						feesTotal += feesDeal;
						numSellTrx++;
						trx = TrxType.SOLD;
						if(verbose) {
							Strategy.printTrx("L", gradCtr, delta);
							Strategy.printTrxProfit(profitDeal, feesDeal, priceBuy*numStocks);
						}
					}
					// sell
					if(		TrxType.BOUGHT == trx &&
							gradCtr == sellGrad &&
							delta >= sellDelta &&
							(price.getTrxPrice() > priceBuy)
							) {
						profitDeal = (price.getTrxPrice()-priceBuy)*numStocks;
						profitTotal += profitDeal;
						feesDeal =  Strategy.fees(priceBuy, numStocks);
						feesTotal += feesDeal;
						numSellTrx++;
						trx = TrxType.SOLD;
						if(verbose) {
							Strategy.printTrx("S", gradCtr, delta);
							Strategy.printTrxProfit(profitDeal, feesDeal, priceBuy*numStocks);
						}
					}
				}					
			}
			if(verbose) System.out.println();
		}
		Float averagePriceBought = priceBuySum/numBuyTrx;
		Float profitPercent = 100*profitTotal/(priceBuySum/numBuyTrx)/numStocks;
		if(verbose) {
			System.out.println(
					"avg p: "+String.format("%.2f", averagePriceBought) +
					" profit: "+String.format("%.2f", profitTotal)+" "+
					" fees: "+String.format("%.2f", feesTotal)+" "+
					" "+
					String.format("%.0f%%", profitPercent)+
					" "+
					numBuyTrx+"-"+numSellTrx+
					" "+
					trx);
		}
		return profitTotal-feesTotal;
	}

	/**
	 * calculate fees for transaction
	 * @param priceBuy at which price did we buy
	 * @param numStocks number of stocks bought
	 * @return fees in EUR
	 */
	private static Float fees(Float priceBuy, Integer numStocks) {
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
	
	private void dumpHistoricPrices(StockPriceSeries series) {
		for (Map.Entry<Integer, StockPrice> entry : series.getSeries().entrySet()) {
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
		for (Map.Entry<Integer, StockPrice> entry : series.getSeries().entrySet()) {
			StockPrice price = entry.getValue();
			Integer key = entry.getKey();
			System.out.print(key+";"+price.getDate()+";"+price.getTrxPrice()+";");
			// now calculate delta closing prices to the previous closing prices
			Integer maxLookBackInDays = 7;
			Float sumDelta = 0f; // for average calc
			for(Integer gradCtr = 1; gradCtr<=maxLookBackInDays; gradCtr++) {
				if(key >= maxLookBackInDays) {
					Float priceCloseInPast = series.getSeries().get(key-gradCtr).getTrxPrice();
					Float delta = 1000*(price.getTrxPrice()-priceCloseInPast)/price.getTrxPrice();
					sumDelta += delta;
				}
			}
			System.out.print(Math.round(sumDelta/maxLookBackInDays));
			System.out.println();
		}
	}

	private static void printTrx(String trx, Integer gradCtr, Float delta) {
		System.out.print(
				" "+
						trx+
						" ["+
						gradCtr+
						":"+
						String.format("%3.0f", delta)+
						"]"
				);
	}

	private static void printTrxProfit(Float profit, Float fees, Float volume) {
		System.out.print(
				" € "+
						String.format("%.02f",profit) +
						"-"+
						String.format("%.02f", fees) +
						"("+
						String.format("%3d", Math.round(100*profit/volume))
						+"%)"
				);
	}


}
