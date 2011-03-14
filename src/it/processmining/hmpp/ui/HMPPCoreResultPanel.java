package it.processmining.hmpp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import org.deckfour.slickerbox.components.GradientPanel;


/**
 * This class represents the main result container
 * 
 * @author Andrea Burattin
 */
public class HMPPCoreResultPanel extends GradientPanel {

	
	private static final long serialVersionUID = 7336170449014439824L;


	/**
	 * Class constructor
	 */
	public HMPPCoreResultPanel() {
		super(new Color(60, 60, 60), new Color(20, 20, 20));
		setupUI();
	}


	/**
	 * This method prepares the GUI
	 */
	private void setupUI() {
		this.setDoubleBuffered(true);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
	}
	
}
