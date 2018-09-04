package mpi.eudico.client.annotator.timeseries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.viewer.*;

/**
 * Test Frame for Time Series track viewer.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TSTestFrame extends JFrame {
	
	public TSTestFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Track viewer");
		initComponents();
		setSize(500, 400);
	}
	
	private void initComponents() {
		float[] range = new float[]{2, 10};
		float[] range2 = new float[]{-20f, 60f};
		ContinuousRateTSTrack track = createDummyTrack(range, 25f);
		ContinuousRateTSTrack track2 = createDummyTrack(range, 150f);
		NonContinuousRateTSTrack track3 = createNCRTrack(range2);
		track2.setColor(Color.ORANGE);
		
		TSTrackPanelImpl panel = new TSTrackPanelImpl();
		panel.setMargin(new Insets(3, 3, 3, 0));
		panel.setHeight(300);
		panel.setWidth(450);
		TSRulerImpl ruler = new TSRulerImpl(range, 30, 200);
		ruler.setFont(Constants.SMALLFONT);
		ruler.setUnitString("m/s2");
		panel.setRuler(ruler);
		
		panel.addTrack(track2);
		panel.addTrack(track);
		
		TSTrackPanelImpl panel2 = new TSTrackPanelImpl();
		panel2.setMargin(new Insets(3, 3, 3, 0));
		panel2.setHeight(300);
		panel2.setWidth(450);
		TSRulerImpl ruler2 = new TSRulerImpl(new float[] {0.0f, 60f}, 30, 200);
		ruler2.setFont(Constants.SMALLFONT);
		ruler2.setUnitString("m/s");
		panel2.setRuler(ruler2);
		
		TSTrackPanelImpl panel3 = new TSTrackPanelImpl();
		panel3.setMargin(new Insets(3, 3, 3, 0));
		panel3.setHeight(300);
		panel3.setWidth(450);
		TSRulerImpl ruler3 = new TSRulerImpl(range2, 30, 200);
		ruler3.setFont(Constants.SMALLFONT);
		ruler3.setUnitString("xxx");
		panel3.setRuler(ruler3);
		panel3.addTrack(track3);
		
		TimeSeriesViewer viewer = new TimeSeriesViewer();
		viewer.addTSTrackPanel(panel);
		//viewer.addTSTrackPanel(panel2);
		viewer.addTSTrackPanel(panel3);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(viewer);
		
	}
	
	private ContinuousRateTSTrack createDummyTrack(float[] range, float rate) {
		ContinuousRateTSTrack track = new ContinuousRateTSTrack();
		track.setType(TimeSeriesTrack.VALUES_FLOAT_ARRAY);
		track.setRange(range);
		track.setColor(Color.MAGENTA);
		track.setSampleRate(rate);
		
		float[] data = new float[(int)(rate * 10)];
		float val = (range[1] - range[0]) / 2;
		float dif = (range[1] - range[0]) / 10;
		for (int i = 0; i < data.length; i++) {
			data[i] = val;
			if (dif > 0) {
				if (val + dif > range[1]) {
					dif = -dif;					
				} 				
			} else {
				if (val + dif < range[0]) {
					dif = -dif;
				}				
			}
			val += dif; 
		}
		track.setData(data);
		return track;
	}
	
	private NonContinuousRateTSTrack createNCRTrack(float[] range) {
	    NonContinuousRateTSTrack track = new NonContinuousRateTSTrack("non-c", "nc test track");
	    track.setRange(range);
	    track.setColor(Color.GREEN);
	    // create Data
	    List<TimeValue> data = new ArrayList<TimeValue>(100);
	    float ext = range[1] - range[0];
	    float v;
	    for (int i = 0, j = 0; i < 30000; i += 200, j++) {
	        v = range[0] + (float)(Math.random() * ext);
	        if (j % 10 != 0) {
	            data.add(new TimeValue(i, v));    
	        } else {
	            i += 400;
	            data.add(new TimeValueStart(i, v));
	        }
	        i += (int)(Math.random() * 100);
	    }
	    track.setData(data);
	    
	    return track;
	}

	public static void main(String[] args) {
		new TSTestFrame().setVisible(true);
	}

}
