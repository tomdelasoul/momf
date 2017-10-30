
package stock;

import java.net.*;
import java.util.regex.*;
import java.io.*;

/**
 *
 */
public class Quote {
	Quote() throws Exception {
		// this.get();
		this.getHistoric();
	}
	void get() throws Exception {
		double askPrice = 0, bidPrice = 0;
		URL exchange = new URL("http://www.boerse-frankfurt.de/etp/XETRA-Gold-DE000A0S9GB0/ETR");
		URLConnection url = exchange.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
		String line;
		Pattern pricePatternAsk = Pattern.compile("^.*<div field=\"ask\".*id=\"\">(\\d*),(\\d*)</div>.*$");
		Pattern pricePatternBid = Pattern.compile("^.*<div field=\"bid\".*class=\"push-value\".*id=\"\">(\\d*),(\\d*)</div>.*$");
		while ((line = in.readLine()) != null) {
			// System.out.println(line);
			Matcher mAsk = pricePatternAsk.matcher(line);
			Matcher mBid = pricePatternBid.matcher(line);
			if(mAsk.matches()) {
				// System.out.println("ask "+mAsk.group(1)+"."+mAsk.group(2));
				askPrice = Double.parseDouble(mAsk.group(1)+"."+mAsk.group(2));
				System.out.println("ask "+askPrice);
			}
			if(mBid.matches()) {
				// System.out.println("bid "+mBid.group(1)+"."+mBid.group(2));
				bidPrice = Double.parseDouble(mBid.group(1)+"."+mBid.group(2));
				System.out.println("bid "+bidPrice);
			}
		}
		System.out.println("mid "+Math.round((bidPrice*1000.0+askPrice*1000.0)/2.0)/1000.0);
		in.close();
	}

	
	void getHistoric() throws Exception {
		Float priceClose = 0f;
		URL exchange = new URL("http://finanzen.net/historische-kurse/BMW");
		URLConnection url = exchange.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
		String line;
		Pattern pricePatternAsk = Pattern.compile("^.*<div field=\"ask\".*id=\"\">(\\d*),(\\d*)</div>.*$");
		Pattern pricePatternBid = Pattern.compile("^.*<div field=\"bid\".*class=\"push-value\".*id=\"\">(\\d*),(\\d*)</div>.*$");
		while ((line = in.readLine()) != null) {
			System.out.println(line);
			Matcher mAsk = pricePatternAsk.matcher(line);
			Matcher mBid = pricePatternBid.matcher(line);
			if(mAsk.matches()) {
				// System.out.println("ask "+mAsk.group(1)+"."+mAsk.group(2));
				priceClose = Float.parseFloat(mAsk.group(1)+"."+mAsk.group(2));
				System.out.println("close: "+priceClose);
			}
		}
		in.close();
	}
}
