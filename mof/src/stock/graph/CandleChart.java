/* --------------------------
 * CandlestickChartDemo1.java
 * --------------------------
 * (C) Copyright 2002-2008, by Object Refinery Limited.
 *
 */

package stock.graph;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

import stock.StockPrice;
import stock.StockPriceSeries;

/**
 * A demo showing a candlestick chart.
 */
public class CandleChart extends ApplicationFrame {
	/**
	 * our chart
	 */
	JFreeChart chart;
    /**
	 * 
	 */
	private static final long serialVersionUID = 8869502259558913097L;

	/**
     * A demonstration application showing a candlestick chart.
     *
     * @param title  the frame title.
	 * @throws FileNotFoundException 
     */
    public CandleChart(String fileName) throws FileNotFoundException {
        super(fileName);
        this.chart = createChart(createDataset(fileName));
        ChartPanel panel = new ChartPanel(this.chart);
        panel.setPreferredSize(new java.awt.Dimension(1024, 768));
        setContentPane(panel);
    }
    
    public JFreeChart getChart() {
    	return this.chart;
    }

    /**
     * Creates a chart.
     *
     * @param dataset  the dataset.
     *
     * @return The dataset.
     */
    private JFreeChart createChart(OHLCDataset dataset) {
        this.chart = ChartFactory.createCandlestickChart(
            "DB",
            "time",
            "value",
            dataset,
            false // no legend
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis) plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        axis.setUpperMargin(0.0);
        axis.setLowerMargin(0.0);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, Color.black);
        renderer.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        XYPointerAnnotation annotation = 
        		new XYPointerAnnotation(
        "1.3.", new GregorianCalendar(2012, 2, 1).getTime().getTime(), 25, Math.PI / 2.0);
        annotation.setTextAnchor(TextAnchor.TOP_CENTER);
        renderer.addAnnotation(annotation);
        plot.setRenderer(1, renderer);
        
        return chart;
    }

    /**
     * Creates a sample high low dataset.
     *
     * @return a sample high low dataset.
     * @throws FileNotFoundException 
     */
    public static OHLCDataset createDataset(String fileName) throws FileNotFoundException {
		StockPriceSeries series = new StockPriceSeries(); 
		series.loadSeriesFromCsvFile(fileName);
		
		int length = series.size();
        Date[] date = new Date[length];
        double[] high = new double[length];
        double[] low = new double[length];
        double[] open = new double[length];
        double[] close = new double[length];
        double[] volume = new double[length];
        
		int ctr = 0;
		for (Map.Entry<Long, StockPrice> entry : series.getTimeSeries().entrySet()) {
			StockPrice price = entry.getValue();
	        date[ctr]  = price.getDate();
	        high[ctr]  = price.getMax();
	        low[ctr]   = price.getMin();
	        open[ctr]  = price.getOpen();
	        close[ctr] = price.getClose();
	        volume[ctr] = 0; // TODO: read volume from CSV file
	        ctr++;
		}
        return new DefaultHighLowDataset(fileName, date, high, low, open, close, volume);
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        CandleChart demo = new CandleChart("deutsche_bank2012only.csv");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

}
