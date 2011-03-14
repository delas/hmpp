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
import cern.colt.matrix.DoubleMatrix2D;


/**
 * This class is the widget for the overlap matrix.
 * 
 * @author Andrea Burattin
 */
public class HMPPOverlapMatrix extends JComponent implements MouseListener, 
	MouseMotionListener {
	
	
	private static final long serialVersionUID = -7722657396781246771L;
	private DoubleMatrix2D overlapMeasure;
	private ArrayList<String> transitions;
	private int mouseX = -1;
	private int mouseY = -1;
	
	
	/**
	 * Default widget constructor
	 * 
	 * @param overlapMeasure the matrix with the overlapping measure (all this 
	 *                       must be between 0 and 1)
	 * @param transitions an array list with all the transition names
	 */
	public HMPPOverlapMatrix(DoubleMatrix2D overlapMeasure, 
			ArrayList<String> transitions) {
		this.overlapMeasure = overlapMeasure;
		this.transitions = transitions;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}


	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		int width = getWidth();
		int height = getHeight();
		
		// draw background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
		
		int size = overlapMeasure.rows();
		int[] blockSize = calculateBlockSize(size, width, height);
		
		BufferedImage matrixBuffer;
		matrixBuffer = new BufferedImage(size * blockSize[0], 
				size * blockSize[1], BufferedImage.TYPE_INT_RGB);
		Graphics2D gBuf = matrixBuffer.createGraphics();
		
		int x = 0;
		int y = 0;
		for(int a = 0; a < size; a++) {
			y = 0;
			for(int b = 0; b < size; b++) {
				gBuf.setColor(measureColor(overlapMeasure.get(a, b)));
				gBuf.fillRect(x+1, y+1, blockSize[0]-1, blockSize[1]-1);
				y += blockSize[1];
			}
			x += blockSize[0];
		}
		gBuf.setColor(Color.RED);
		gBuf.drawLine(0, 0, x, y);
		gBuf.dispose();
		g2d.drawImage(matrixBuffer, 0, 0, this);
		if(mouseX >= 0 && mouseX < (size * blockSize[0]) && 
				mouseY >= 0 && mouseY < (size * blockSize[1])) {
			// draw info
			int eventX = mouseX / blockSize[0];
			int eventY = mouseY / blockSize[1];
			paintInfo(g2d, eventX, eventY, mouseX, mouseY);
		}
	}


	/**
	 * This method calculates each block optimal size
	 * 
	 * @param size the number of columns and rows of the matrix
	 * @param width the total width space
	 * @param height the total height space
	 * @return an array with the optimal block size (in order to keep the matrix
	 *         square)
	 */
	protected int[] calculateBlockSize(int size, int width, int height) {
		int [] toReturn = {0, 0};
		int maxRes = Math.min(width, height);
		if(maxRes <= (size * 2)) {
			toReturn[0] = 1;
			toReturn[1] = 1;
		} else {
			int bSize = maxRes / size;
			if((bSize * size) > maxRes) {
				bSize--;
			}
			toReturn[0] = bSize;
			toReturn[1] = bSize;
		}
		return toReturn;
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
	protected void paintInfo(Graphics2D g2d, int eventX, int eventY, int x, 
			int y) {
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		DecimalFormat f = new DecimalFormat("#.#####");
		
		String evtX = transitions.get(eventX);
		String evtY = transitions.get(eventY);
		String valueStr = "Overlap: " + 
			f.format(overlapMeasure.get(eventX, eventY));
		FontMetrics fontMetrics = 
			this.getFontMetrics(g2d.getFont().deriveFont(11.0f));
		
		int widthA = fontMetrics.stringWidth(evtX);
		int widthB = fontMetrics.stringWidth(evtY);
		int widthC = fontMetrics.stringWidth(valueStr);
		int width = Math.max(widthA, widthB);
		width = Math.max(width, widthC);
		
		Color bgColor = new Color(0, 0, 0, 0.75f);
		Color fgColor = new Color(220, 220, 220, 220);
		
		g2d.setColor(bgColor);
		g2d.fillRoundRect(x + 18, y - 12, width + 12, 51, 5, 5);
		g2d.drawOval(x - 3, y - 3, 8, 8);
		g2d.drawLine(x + 3, y + 1, x + 18, y + 1);
		g2d.setFont(g2d.getFont().deriveFont(11.0f));
		g2d.setColor(fgColor);
		g2d.drawOval(x - 4, y - 4, 8, 8);
		g2d.drawLine(x + 4, y, x + 18, y);
		g2d.drawString(evtX, x + 24, y + 2);
		g2d.drawString(evtY, x + 24, y + 17);
		g2d.drawString(valueStr, x + 24, y + 32);
	}


	/**
	 * This method extracts the color from the current cell measure
	 * 
	 * @param measure the current measure (must be between 0 and 1)
	 * @return the color object associated with the measure
	 */
	private Color measureColor(double measure) {
		Color c = Color.BLACK;
		int step = 10;
		int colorInc = 255 / step;
		double thresholdInc = 1.0 / step;
		for (int i = 0; i < step; i++) {
			if (measure > (thresholdInc * i) && 
					measure <= (thresholdInc * (i+1))) {
				c = new Color((colorInc * i), 0, 0);
				break;
			}
		}
		return c;
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		mouseX = arg0.getX() - 10;
		mouseY = arg0.getY() - 10;
		repaint();
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		mouseX = arg0.getX() - 10;
		mouseY = arg0.getY() - 10;
		repaint();
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		mouseX = -1;
		mouseY = -1;
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
		mouseX = arg0.getX() - 10;
		mouseY = arg0.getY() - 10;
		repaint();
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX() - 10;
		mouseY = arg0.getY() - 10;
		repaint();
	}

}
