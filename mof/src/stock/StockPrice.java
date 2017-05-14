package stock;

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
	// date of the price
	private String dateString;
	// 
	private Date date;
	
	public String toString() {
		return dateString+ " o: "+open.toString() + " h: "+max.toString()+" l:"+min.toString()+" c: "+close.toString();
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
	
	private float getClose() {
		return close;
	}
	public void setClose(float close) {
		this.close = close;
	}
	private float getOpen() {
		return open;
	}
	public void setOpen(float open) {
		this.open = open;
	}
	private float getMin() {
		return min;
	}
	public void setMin(float min) {
		this.min = min;
	}
	private float getMax() {
		return max;
	}
	public void setMax(float max) {
		this.max = max;
	}
	public String getDate() {
		return dateString;
	}
	// string is of the form YY.MM.DD
	public void setDate(String string) {
		this.dateString = string;
	}
}
