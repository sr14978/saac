package saac.utils;

public class RateUtils {
	public static String getRate(int i, int j) {
		float rateVal = round((float) i / j);
		return "Rate: " + String.format("%.2f", rateVal);
	}
	
    static float round(float i) {
    	return (float) Math.round(( i )*100 ) / 100;
    }
}
