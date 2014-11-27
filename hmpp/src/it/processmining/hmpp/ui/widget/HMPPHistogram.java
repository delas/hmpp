package it.processmining.hmpp.ui.widget;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;


/**
 * 
 *  
 * @author Andrea Burattin
 */
public class HMPPHistogram extends JComponent implements MouseListener, 
	MouseMotionListener {

	
	private static final long serialVersionUID = 7260478384117035832L;
	
	protected Color colorAxis;
	protected Color textColor = new Color(120, 120, 120);
	protected Color colorHover = new Color(240, 190, 100);//new Color(80, 80, 130);
	protected Color colorClicked = new Color(200, 10, 10);
	protected Color colorClickedHover = new Color(230, 40, 40);
	protected Color colorSuggestion = new Color(150, 150, 150);
	private ArrayList<Integer> elements;
	private ArrayList<Double> labels;
	private Integer maxVal;
	private int barWidth;
	private int mouseX = -1;
	private int mouseY = -1;
	private int columnHover = -1;
	private int columnClicked = -1;
	private int suggestedColumn = -1;
	
	
	public HMPPHistogram(ArrayList<Integer> elements, ArrayList<Double> labels) {
		this(elements);
		this.labels = labels;
	}
	
	
	public HMPPHistogram(ArrayList<Integer> elements) {
		this.elements = elements;
		this.maxVal = Integer.MIN_VALUE;
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i) > this.maxVal)
				this.maxVal = elements.get(i);
		}
		this.setBackground(Color.BLACK);
		this.setOpaque(true);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		int size = elements.size();
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		
		if (size < 1) {
			return;
		}
		
		barWidth = (int) ((width - 1) / size);
		int barX = 1;
		int maxBarHeight = height - 2;
		int barHeight;
		int barY;
		
		BufferedImage matrixBuffer;
		matrixBuffer = new BufferedImage(width,  height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gBuf = matrixBuffer.createGraphics();
		
		gBuf.setFont(gBuf.getFont().deriveFont(10.0f));
		int tempBarWidth = barWidth - 1;
		for(int i = 0; i < elements.size(); i++) {
			barHeight = (int)((double)maxBarHeight * ((double)elements.get(i) / maxVal));
			barY = 1 + maxBarHeight - barHeight;
			gBuf.setColor(measureColor(((double)elements.get(i) / maxVal)));
			if (i == columnHover)
				gBuf.setColor(colorHover);
			if (i == columnClicked)
				gBuf.setColor(colorClicked);
			if (i == columnClicked && i == columnHover)
				gBuf.setColor(colorClickedHover);
			gBuf.fillRect(barX, barY, tempBarWidth, barHeight);
			if (i == suggestedColumn) {
				gBuf.setColor(colorSuggestion);
				gBuf.drawRect(barX, barY, tempBarWidth - 1, barHeight - 1);
				int x_line = barX + (tempBarWidth / 2);
				if (suggestedColumn != columnClicked) {
					for (int j = 1; j < barY; ) {
						gBuf.drawLine(x_line, j, x_line, j+1);
						j += 4;
					}
				}
			}
			barX += barWidth;
		}
		gBuf.dispose();
		
		g2d.drawImage(matrixBuffer, 0, 0, this);
		
		if (mouseX >= 0 && mouseX < ((size * barWidth))) {
			// draw info
			int eventX = getColumn(mouseX);
			if (eventX < elements.size() && eventX >= 0)
				paintInfo(g2d, eventX, mouseX, mouseY, width);
		}
	}


	/**
	 * This method paints the tooltip with the current cell information
	 * 
	 * @param g2d the graphic object
	 * @param eventX event coordinate
	 * @param eventY event coordinate
	 * @param x mouse coordinate
	 * @param y mouse coordinate
	 */
	protected void paintInfo(Graphics2D g2d, int eventX, int x, int y, int canvasWidth) {
		DecimalFormat f = new DecimalFormat("#.####");
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		String countStr = "Total elements: " + elements.get(eventX);
		String valueStr = "Value: ";
		if (labels != null)
			valueStr = valueStr + f.format(labels.get(eventX));
		else
			valueStr = valueStr + "/";
		FontMetrics fontMetrics = this.getFontMetrics(g2d.getFont().deriveFont(11.0f));
		
		int widthA = fontMetrics.stringWidth(valueStr);
		int widthB = fontMetrics.stringWidth(countStr);
		int width = Math.max(widthA, widthB);
		
		Color bgColor = new Color(0, 0, 0, 0.75f);
		Color fgColor = new Color(220, 220, 220, 220);
		
		if (x + width + 36 < canvasWidth) {
			/* staight draw */
			g2d.setColor(bgColor);
			g2d.fillRoundRect(x + 18, y - 12, width + 12, 35, 5, 5);
			g2d.drawOval(x - 3, y - 3, 8, 8);
			g2d.drawLine(x + 3, y + 1, x + 18, y + 1);
			g2d.setFont(g2d.getFont().deriveFont(11.0f));
			g2d.setColor(fgColor);
			g2d.drawOval(x - 4, y - 4, 8, 8);
			g2d.drawLine(x + 4, y, x + 18, y);
			g2d.drawString(valueStr, x + 24, y + 2);
			g2d.drawString(countStr, x + 24, y + 17);
		} else {
			/* flipped draw */
			g2d.setColor(bgColor);
			g2d.fillRoundRect(x - width - 29, y - 12, width + 11, 35, 5, 5);
			g2d.drawOval(x - 3, y - 3, 8, 8);
			g2d.drawLine(x - 3, y + 1, x - 18, y + 1);
			g2d.setFont(g2d.getFont().deriveFont(11.0f));
			g2d.setColor(fgColor);
			g2d.drawOval(x - 4, y - 4, 8, 8);
			g2d.drawLine(x - 4, y, x - 18, y);
			g2d.drawString(valueStr, x - width - 24, y + 2);
			g2d.drawString(countStr, x - width - 24, y + 17);
		}
	}


	/**
	 * This method extracts the color from the current cell measure
	 * 
	 * @param measure the current measure (must be between 0 and 1)
	 * @return the color object associated with the measure
	 */
	private Color measureColor(double measure) {
		Color c = Color.BLACK;
//		new Color(50, 50, 100)
		int step = 15;
		int colorInc = 150 / step;
		double thresholdInc = 1.0 / step;
		for (int i = 0; i < step; i++) {
			if (measure > (thresholdInc * i) && 
					measure <= (thresholdInc * (i+1))) {
				c = new Color(0, 0, 50 + (colorInc * i));
				break;
			}
		}
		return c;
	}
	
	
	public int getColumn(int x) {
		if (x > 0 && barWidth > 0)
			return (x) / barWidth;
		else
			return -1;
	}
	
	
	public void suggestCount(int targetCount) {
		suggestedColumn = -1;
		for (int i = elements.size() -1; i >= 0; i--) {
			if (elements.get(i) >= targetCount) {
				suggestedColumn = i;
				break;
			}
		}
		if (suggestedColumn == -1)
			suggestedColumn = 0;
	}
	
	
	public void suggestCountColumn(int column) {
		suggestedColumn = column;
	}
	
	
	public Integer getColumnCount(int c) {
		return elements.get(c);
	}
	
	
	public Double getColumnValue(int c) {
		if (labels == null || c > labels.size())
			return 0.;
		else
			return labels.get(c);
	}
	
	
	public void setClickedColumn(int c) {
		columnClicked = c;
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		mouseX = arg0.getX();// - 10;
		mouseY = arg0.getY() - 10;
		repaint();
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		mouseX = arg0.getX();// - 10;
		mouseY = arg0.getY() - 10;
		columnHover = getColumn(mouseX/* + 10*/);
		repaint();
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		mouseX = -1;
		mouseY = -1;
		columnHover = -1;
		repaint();
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// ignored
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// ignored
	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		mouseX = arg0.getX();// - 10;
		mouseY = arg0.getY() - 10;
		columnHover = getColumn(mouseX/* + 10*/);
		repaint();
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();// - 10;
		mouseY = arg0.getY() - 10;
		columnHover = getColumn(mouseX/* + 10*/);
		repaint();
	}

}
