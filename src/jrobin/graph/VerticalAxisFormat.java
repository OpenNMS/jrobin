package jrobin.graph;

import java.text.DecimalFormat;
import java.text.FieldPosition;

class VerticalAxisFormat extends DecimalFormat {
	VerticalAxisFormat() {

	}

	VerticalAxisFormat(String format) {
		super(format);
	}

	public StringBuffer format(double number, StringBuffer result,
							   FieldPosition fieldPosition) {
		String suffix = "";
		double absNumber = Math.abs(number);
		if(absNumber >= 1e9) {
			number /= 1e9;
			suffix = "G";
		}
		else if(absNumber >= 1e6) {
			number /= 1e6;
			suffix = "M";
		}
		else if(absNumber >= 1e3) {
			number /= 1e3;
			suffix = "k";
		}
		super.format(number, result, fieldPosition);
		result.append(suffix);
		return result;
	}

	public StringBuffer format(long number, StringBuffer result,
							   FieldPosition fieldPosition) {
		return format((double)number, result, fieldPosition);
	}

}
