package stock;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * a class that describes a stock price at a given date
 */
public class StockPrice {
	// the open price
	private Float open;
	// the closing price
	private Float close;
	// the minimum price
	private Float min;
	// the maximum price
	private Float max;
	// date of the price as string
	private String dateString;
	// date of the price as date
	private Date date;
	// volume of trades
	private long volume;
	// format for print-outs
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
	
	public String toString() {
		return this.getDateAsString()+ " o: "+open.toString() + " h: "+max.toString()+" l:"+min.toString()+" c: "+close.toString();
	}
	
	public StockPrice() {
		this.open = 0f;
		this.close = 0f;
		this.min = 0f;
		this.max = 0f;
		
	}
	
	/**
	 * return the price at which a transaction would have been possible with high probability
	 * @return
	 */
	public float getTrxPrice() {
		return (this.max+this.min)/2;
	}
	
	public float getClose() {
		return close;
	}
	public void setClose(float close) {
		this.close = close;
	}
	public float getOpen() {
		return open;
	}
	public void setOpen(float open) {
		this.open = open;
	}
	public float getMin() {
		return min;
	}
	public void setMin(float min) {
		this.min = min;
	}
	public float getMax() {
		return max;
	}
	public void setMax(float max) {
		this.max = max;
	}
	public String getDateAsString() {
		return StockPrice.sdf.format(this.date);
	}

	public void setDate(Date date) {
		this.date = date;
	}
	/**
	 * returns date of price
	 * @return
	 */
	public Date getDate() {
		return this.date;
	}

	public void setVolume(long l) {
		this.volume = l;
	}
	
	public long getVolume() {
		return this.volume;
	}
}
