package stock.util;
/**
 * a transaction type can be Buy, Sell or Stop-Loss
 *
 */
public class TrxType {
	private final String transaction;
	
	private TrxType(String transaction) { this.transaction = transaction;}
	
	public String toString() { return this.transaction;}
	
	public static final TrxType BUY = new TrxType("buy");
	public static final TrxType SELL = new TrxType("sell");
	public static final TrxType STOP_LOSS = new TrxType("stop loss");
}
