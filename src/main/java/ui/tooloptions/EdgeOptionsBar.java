package ui.tooloptions;

import tool.Tool;
import ui.GBFrame;
import util.ImageUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * The option bar for the Edge and Directed Edge tools.
 */
public class EdgeOptionsBar extends ToolOptionsBar {

	private static final long serialVersionUID = -7044199334959545473L;

	// Interface components for the edge tool
	private JLabel lineWeightLabel;
	private JTextField lineWeightTextField;
	private JSlider lineWeightSlider;
	private JLabel lineColorLabel;
	private BufferedImage lineColorImage;
	private ImageIcon lineColorIcon;
	private JButton lineColorButton;
	private Color lineColor;

	private String choose = "Choose Edge Color";

	/**
	 * @param g The GBFrame object this bar will appear in.
	 */
	public EdgeOptionsBar(GBFrame g) {
		super(g);
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		lineWeightLabel = new JLabel("Line Weight: ");
		lineWeightTextField = new JTextField(3);
		lineWeightTextField.setText("2");
		lineWeightSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 2);
		lineWeightSlider.setPaintTicks(true);
		lineWeightSlider.setMinorTickSpacing(5);
		lineWeightSlider.setPaintLabels(true);
		lineWeightTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					int newWeight = Integer.parseInt(lineWeightTextField.getText());
					if (newWeight < lineWeightSlider.getMinimum() || newWeight > lineWeightSlider.getMaximum())
						JOptionPane.showMessageDialog(null,
													  String.format("Must be an integer between %d and %d inclusive.", lineWeightSlider.getMinimum(), lineWeightSlider.getMaximum()),
													  "Out of range", JOptionPane.WARNING_MESSAGE);
					else
						lineWeightSlider.setValue(newWeight);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Radius must be a well-formatted integer.", "Invalid integer", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		lineWeightSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				lineWeightTextField.setText(String.valueOf(lineWeightSlider.getValue()));
			}
		});
		lineColorLabel = new JLabel("Line Color: ");
		lineColor = Color.BLACK;
		lineColorImage = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
		ImageUtils.fillImage(lineColorImage, lineColor);
		lineColorIcon = new ImageIcon(lineColorImage);
		lineColorButton = new JButton(lineColorIcon);
		lineColorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Color c = JColorChooser.showDialog(EdgeOptionsBar.this.getGUI(), choose, lineColor);
				if (c != null) {
					lineColor = c;
					ImageUtils.fillImage(lineColorImage, c);
				}
			}
		});
		this.add(lineWeightLabel);
		this.add(lineWeightTextField);
		this.add(lineWeightSlider);
		this.addSeparator();
		this.add(lineColorLabel);
		this.add(lineColorButton);
	}

	public int getCurrentLineWeight() {
		Tool currentTool = getGUI().getCurrentTool();
		if (currentTool == Tool.EDGE || currentTool == Tool.DIRECTED_EDGE)
			return lineWeightSlider.getValue();
		return -1;
	}

	public Color getCurrentLineColor() {
		Tool currentTool = getGUI().getCurrentTool();
		if (currentTool == Tool.EDGE || currentTool == Tool.DIRECTED_EDGE)
			return lineColor;
		return null;
	}

}