package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import javax.swing.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class Epoch extends JFrame implements Runnable {
	private static final String[] formats = {
		"MM/dd/yy HH:mm:ss",
		"dd.MM.yy HH:mm:ss",
		"yy-MM-dd HH:mm:ss",
		"MM/dd/yy HH:mm",
		"dd.MM.yy HH:mm",
		"yy-MM-dd HH:mm",
		"MM/dd/yy",
		"dd.MM.yy",
		"yy-MM-dd",
		"HH:mm MM/dd/yy",
		"HH:mm dd.MM.yy",
		"HH:mm yy-MM-dd",
		"HH:mm:ss MM/dd/yy",
		"HH:mm:ss dd.MM.yy",
		"HH:mm:ss yy-MM-dd"
	};

	private static final SimpleDateFormat[] parsers = new SimpleDateFormat[formats.length];
	private static final String helpText;

	static {
		for(int i = 0; i < parsers.length; i++) {
			parsers[i] = new SimpleDateFormat(formats[i]);
			parsers[i].setLenient(true);
		}
		StringBuffer tooltipBuff = new StringBuffer("<html><b>Supported input formats:</b><br>");
		for(int i = 0; i < formats.length; i++) {
			tooltipBuff.append(formats[i] + "<br>");
		}
		tooltipBuff.append("<b>RRDTool time format</b><br>");
		tooltipBuff.append("... including timestamps</html>");
		helpText = tooltipBuff.toString();
	}

	private JLabel topLabel = new JLabel("Enter timestamp or readable date:");
	private JTextField inputField = new JTextField(25);
	private JButton button = new JButton("Convert");
	//private JLabel helpLabel = new JLabel(helpText);

	private static final SimpleDateFormat OUTPUT_DATE_FORMAT =
			new SimpleDateFormat("MM/dd/yy HH:mm:ss EEE");

	Epoch() {
		super("Epoch");
		constructUI();
		setVisible(true);
		new Thread(this).start();
	}

	private void constructUI() {
		JPanel c = (JPanel) getContentPane();
		c.setLayout(new BorderLayout());

		c.add(topLabel, BorderLayout.NORTH);
		c.add(inputField, BorderLayout.CENTER);
		c.add(button, BorderLayout.EAST);
		// c.add(helpLabel, BorderLayout.WEST);

		button.setToolTipText(helpText);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				convert();
			}
		});

		inputField.requestFocus();
		getRootPane().setDefaultButton(button);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		centerOnScreen();

	}

	void centerOnScreen() {
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension screenSize = t.getScreenSize();
		Dimension frameSize = getPreferredSize();
		double x = (screenSize.getWidth() - frameSize.getWidth()) / 2;
		double y = (screenSize.getHeight() - frameSize.getHeight()) / 2;
		setLocation((int) x, (int) y);
	}

	private void convert() {
		String time = inputField.getText().trim();
		if(time.length() > 0) {
			// try simple timestamp
			try {
				long timestamp = Long.parseLong(time);
				Date date = new Date(timestamp * 1000L);
				formatDate(date);
			}
			catch(NumberFormatException nfe) {
				// failed, try as a date
				try {
					inputField.setText("" + parseDate(time));
				}
				catch (RrdException e) {
					inputField.setText("Could not convert, sorry");
				}
			}
		}
	}

	public void run() {
		for(;;) {
			Date now = new Date();
			long timestamp = now.getTime() / 1000L;
			setTitle(timestamp + " seconds since epoch");
			try {
				Thread.sleep(1000L);
			}
			catch (InterruptedException e) {
			}
		}
	}

	void formatDate(Date date) {
		inputField.setText(OUTPUT_DATE_FORMAT.format(date));
	}

	private long parseDate(String time) throws RrdException {
		for(int i = 0; i < parsers.length; i++) {
			try {
				return Util.getTimestamp(parsers[i].parse(time));
			}
			catch (ParseException e) {
			}
		}
		return new TimeParser(time).parse().getTimestamp();
	}


	public static void main(String[] args) {
		new Epoch();
	}
}
