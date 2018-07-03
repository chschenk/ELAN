package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

public class MFETypeTable extends MFETable {
	private static final long serialVersionUID = -6660589343733391482L;
	private MFEModel model;
	private CheckBoxTableCellRenderer cbRenderer;
	public final Color EVEN_LIGHT_BLUE = new Color(240, 255, 255);
	public final Color LESS_LIGHT_BLUE = new Color(200, 255, 255);
	public final Color INCONS_LIGHT_GREY = new Color(240, 240, 240);
	public final Color INCONS_DARK_GREY = new Color(150, 150, 150);
	

	public MFETypeTable(MFEModel model, final MFEFrame parent) {
		super(model);
		this.model = model;
		
		setModel(new TableByTypeModel(model));
		
        ImageIcon tickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Tick16.gif"));
        ImageIcon untickIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Untick16.gif"));
        cbRenderer = new CheckBoxTableCellRenderer();
        cbRenderer.setIcon(untickIcon);
        cbRenderer.setSelectedIcon(tickIcon);
        cbRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				parent.initCombobox();
			}
		});
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c;
		if(vColIndex==MFEModel.TYPE_TIMEALIGNABLECOLUMN)
			c = super.prepareRenderer(cbRenderer, rowIndex, vColIndex);
		else
			c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(EVEN_LIGHT_BLUE);
		} else {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		}
		c.setForeground(Color.BLACK);
		if(rowIndex == getSelectedRow())
			c.setBackground(LESS_LIGHT_BLUE);
		if(!model.isConsistentType(rowIndex)) {
			c.setBackground(INCONS_LIGHT_GREY);
			c.setBackground(INCONS_DARK_GREY);
		}
		return c;
	}

}
