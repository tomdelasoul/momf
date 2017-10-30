package stock;

import java.math.BigDecimal;

import stock.util.TrxType;

/**
 * a financial transaction can be either a buy or a sell
 *
 */
public class Transaction {
	/**
	 * buy or sell
	 */
	static TrxType trxType;
	/**
	 * price at which transaction shall be executed (buy or sell limit)
	 */
	BigDecimal limitPrice;
	
	public static Transaction getInstanceBuy(BigDecimal limit) {
		Transaction.trxType = TrxType.BUY;
		return new Transaction(limit);
	}
	
	public static Transaction getInstanceSell(BigDecimal limit) {
		Transaction.trxType = TrxType.SELL;
		return new Transaction(limit);
	}
	
	public void setLimit(BigDecimal limit) {
		this.limitPrice = limit;
	}
	
	public boolean isPossibleAt(StockPrice price) {
		return true;
	}
	
	
	private Transaction(BigDecimal limit) {
		Transaction.trxType = null;
		this.limitPrice = limit;
	}
	
	public BigDecimal execute(StockPrice price) {
		return BigDecimal.valueOf(0l);
	}
	

}
