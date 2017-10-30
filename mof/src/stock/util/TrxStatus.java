package stock.util;
/**
 * a class that describes our investment status: we have either bought a stock - we are long, or we sold the stock.
 *
 */
public class TrxStatus {
	private final String status;
	
	private TrxStatus(String status) { this.status = status;}
	
	public String toString() { return this.status;}
	
	public static final TrxStatus STOCK_SOLD = new TrxStatus("stock sold");
	public static final TrxStatus STOCK_BOUGHT = new TrxStatus("stock bought");
}
