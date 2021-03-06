package ui.dialogs;

import lombok.Getter;
import ui.GBFrame;
import util.ImageUtils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A dialog displaying settings for the grid display/snap.
 * Built with the Window Builder plugin for Eclipse.
 *
 * @author Brian Yao
 */
public class GridSettingsDialog extends JDialog {

	private static final long serialVersionUID = 8356862050505775547L;

	private static final int MIN_GRID_LEVEL = 5;
	private static final int MAX_GRID_LEVEL = 500;

	private static final int DEFAULT_GRID_LEVEL = 30;
	private static final boolean DEFAULT_SNAP_TO_GRID_ENABLED = false;
	private static final boolean DEFAULT_SHOW_GRID_ENABLED = false;
	private static final Color DEFAULT_GRID_COLOR = Color.DARK_GRAY;

	private JLabel gridLevelLabel;
	private JTextField gridLevelTextField;
	private JSlider gridLevelSlider;

	private JCheckBox snapToGridBox;
	private JCheckBox showGridBox;

	private JLabel gridColorLabel;
	private ImageIcon gridColorIcon;
	private JButton gridColorButton;

	private JButton applyButton;
	private JButton cancelButton;

	// The values which will be used by the editor panel
	@Getter
	private int gridLevel;
	private boolean snapToGrid;
	private boolean showGrid;
	@Getter
	private Color gridColor;

	// The temporary values which are not saved until "Apply" is clicked
	private Boolean tempSnapToGrid;
	private Boolean tempShowGrid;
	private Color tempGridColor;

	public GridSettingsDialog(GBFrame g) {
		super(g, "Grid: Settings");

		// Set default values for settings
		gridLevel = DEFAULT_GRID_LEVEL;
		snapToGrid = DEFAULT_SNAP_TO_GRID_ENABLED;
		showGrid = DEFAULT_SHOW_GRID_ENABLED;
		gridColor = DEFAULT_GRID_COLOR;

		gridLevelLabel = new JLabel("Grid Level (px):");
		gridLevelTextField = new JTextField(3);
		gridLevelTextField.setText(String.valueOf(DEFAULT_GRID_LEVEL));
		gridLevelSlider = new JSlider(JSlider.HORIZONTAL, MIN_GRID_LEVEL, MAX_GRID_LEVEL, DEFAULT_GRID_LEVEL);
		gridLevelSlider.setPreferredSize(new Dimension(100, 10));

		gridLevelTextField.addActionListener($ -> {
			String entered = gridLevelTextField.getText();
			try {
				int newValue = Integer.parseInt(entered);
				if (newValue < MIN_GRID_LEVEL) {
					JOptionPane.showMessageDialog(GridSettingsDialog.this, "The integer you entered is too " +
													  "small. It must be at least " + MIN_GRID_LEVEL + ".",
												  "Grid Settings: Error", JOptionPane.INFORMATION_MESSAGE);
				} else if (newValue > MAX_GRID_LEVEL) {
					JOptionPane.showMessageDialog(GridSettingsDialog.this, "The integer you entered is too " +
													  "large. It must be at most " + MAX_GRID_LEVEL + ".",
												  "Grid Settings: Error", JOptionPane.INFORMATION_MESSAGE);
				} else {
					// Set the slider's value
					gridLevelSlider.setValue(newValue);
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(GridSettingsDialog.this, "The value you entered is not a valid grid " +
					"level. It must be an integer.");
			}
		});

		gridLevelSlider.addChangeListener($ -> gridLevelTextField.setText(String.valueOf(gridLevelSlider.getValue())));
		snapToGridBox = new JCheckBox("Snap To Grid");
		snapToGridBox.setToolTipText("Allows nodes moved using the select tool to automatically snap to the grid. " +
										 "Existing nodes will not be automatically snapped to the grid.");
		snapToGridBox.addActionListener($ -> tempSnapToGrid = snapToGridBox.isSelected());

		showGridBox = new JCheckBox("Show Grid Lines");
		showGridBox.setToolTipText("Toggles the display of the grid lines on the editor panel.");
		showGridBox.addActionListener($ -> {
			boolean selected = showGridBox.isSelected();
			gridColorLabel.setEnabled(selected);
			gridColorButton.setEnabled(selected);
			tempShowGrid = selected;
		});

		gridColorLabel = new JLabel("Grid Line Color:");
		gridColorIcon = new ImageIcon(new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB));
		gridColorButton = new JButton(gridColorIcon);
		gridColorButton.addActionListener($ -> {
			tempGridColor = JColorChooser.showDialog(this.getParent(), "Choose Text Color", gridColor);
			ImageUtils.fillImage((BufferedImage) gridColorIcon.getImage(), tempGridColor);
		});

		applyButton = new JButton("Apply");
		applyButton.addActionListener($ -> {
			gridLevel = gridLevelSlider.getValue();
			snapToGrid = tempSnapToGrid != null ? tempSnapToGrid : snapToGrid;
			showGrid = tempShowGrid != null ? tempShowGrid : showGrid;
			if (tempGridColor != null)
				gridColor = tempGridColor;
			hideDialog();
		});

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener($ -> hideDialog());

		GroupLayout layout = new GroupLayout(this.getContentPane());
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
							  .addGroup(layout.createParallelGroup(Alignment.LEADING)
											.addGroup(layout.createSequentialGroup()
														  .addComponent(gridLevelLabel)
														  .addPreferredGap(ComponentPlacement.RELATED)
														  .addComponent(gridLevelTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														  .addGap(6)
														  .addComponent(gridLevelSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
											.addComponent(snapToGridBox)
											.addComponent(showGridBox)
											.addGroup(layout.createSequentialGroup()
														  .addGap(20)
														  .addComponent(applyButton)
														  .addGap(60)
														  .addComponent(cancelButton))
											.addGroup(layout.createSequentialGroup()
														  .addContainerGap()
														  .addComponent(gridColorLabel)
														  .addPreferredGap(ComponentPlacement.RELATED)
														  .addComponent(gridColorButton, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
							  .addGap(11)
							  .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
											.addComponent(gridLevelSlider, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addGroup(layout.createParallelGroup(Alignment.BASELINE)
														  .addComponent(gridLevelLabel)
														  .addComponent(gridLevelTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							  .addComponent(snapToGridBox)
							  .addComponent(showGridBox)
							  .addPreferredGap(ComponentPlacement.RELATED)
							  .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
											.addComponent(gridColorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(gridColorButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							  .addGap(18)
							  .addGroup(layout.createParallelGroup(Alignment.BASELINE)
											.addComponent(applyButton)
											.addComponent(cancelButton)))
		);

		layout.setAutoCreateContainerGaps(true);

		this.getContentPane().setLayout(layout);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(false);
	}

	/**
	 * Display the grid settings dialog.
	 */
	public void showDialog() {
		// Reset all temporary values to null
		tempSnapToGrid = null;
		tempShowGrid = null;
		tempGridColor = null;

		// Set values to reflect saved settings
		gridLevelTextField.setText(String.valueOf(gridLevel));
		gridLevelSlider.setValue(gridLevel);
		snapToGridBox.setSelected(snapToGrid);
		showGridBox.setSelected(showGrid);
		gridColorLabel.setEnabled(showGrid);
		gridColorButton.setEnabled(showGrid);
		ImageUtils.fillImage((BufferedImage) gridColorIcon.getImage(), gridColor);

		// Fit dialog to size of components
		pack();
		setLocationRelativeTo(this.getParent());
		setVisible(true);
	}

	/**
	 * Get the current snap-to-grid choice.
	 *
	 * @return Whether snap to grid is enabled.
	 */
	public boolean getSnapToGrid() {
		return snapToGrid;
	}

	/**
	 * Get whether the grid is displayed.
	 *
	 * @return Whether the grid is displayed.
	 */
	public boolean getShowGrid() {
		return showGrid;
	}

	/**
	 * Hide the grid settings dialog.
	 */
	private void hideDialog() {
		setVisible(false);
	}

}
