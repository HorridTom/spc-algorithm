package uk.ac.ic.doc.wishnwl.tw;

import java.util.Iterator;
import java.util.Vector;

public class SPCCalculator {

	//private int defaultMaximumNumberOfLoops = 0;
	public int maximumNumberOfLoops;
	//private int defaultBreakPadding = 5;
	public int breakPadding;
	Vector<Double> vals;
	double[] rawVals;
	double[] deltas;
	double[] means;
	double[] amrs;
	boolean[] breakPoints;

	public SPCCalculator(Vector vals2, int maxLoops, int padding) {
		this.maximumNumberOfLoops = maxLoops;
		this.breakPadding = padding;
		this.vals = vals2;
		rawVals = new double[vals.size()];
		for (int i = 0; i < vals.size(); i++) {
			double v = vals.elementAt(i).doubleValue();
			rawVals[i] = v;
		}
		initArrays();
	}

	public SPCCalculator(double[] vals2, int maxLoops, int padding) {
		this.maximumNumberOfLoops = maxLoops;
		this.breakPadding = padding;
		this.rawVals = vals2;
		initArrays();
	}

	public SPCCalculator(Vector vals2) {
		this(vals2, 0, 5);
	}

	public SPCCalculator(double[] vals2) {
		this(vals2, 0, 5);
	}

	private void initArrays() {
		double sum = 0.0D;
		means = new double[rawVals.length];
		breakPoints = new boolean[rawVals.length];
		deltas = new double[rawVals.length];
		amrs = new double[rawVals.length];
		if (deltas.length > 0) deltas[0] = 0.0D;
		double prev = 0.0D;
		for (int i = 0; i < rawVals.length; i++) {
			double v = rawVals[i];
			sum += v;
			if (i > 0) deltas[i] = Math.abs(v - prev);
			breakPoints[i] = false;
			prev = v;
		}
		double mean = sum / rawVals.length;
		for (int i = 0; i < means.length; i++) {
			means[i] = mean;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("rawVals:[");
		for (int i = 0; i < rawVals.length; i++) {
			if (i < rawVals.length - 1) sb.append(rawVals[i] + ",");
			else sb.append(rawVals[i] + "]\n");
		}
		sb.append("means:[");
		for (int i = 0; i < means.length; i++) {
			if (i < means.length - 1) sb.append(means[i] + ",");
			else sb.append(means[i] + "]\n");
		}
		sb.append("amrs:[");
		for (int i = 0; i < amrs.length; i++) {
			if (i < amrs.length - 1) sb.append(amrs[i] + ",");
			else sb.append(amrs[i] + "]\n");
		}
		sb.append("breakPoints:[");
		for (int i = 0; i < breakPoints.length; i++) {
			if (i < breakPoints.length - 1) sb.append(breakPoints[i] + ",");
			else sb.append(breakPoints[i] + "]\n");
		}
		return sb.toString();
	}

	public Object get(int index) {
		return new Double(means[index]);
	}

	public Object getLimit(int index) {
		return new Double(amrs[index]);
	}

	//public void add(double rawValue) {
		// TODO Auto-generated method stub
	//	vals.add(new Double(rawValue));
	//}

	/**
	 * Calculate the mean of a segment defined by start and end points.
	 * @param start Start of segment (inclusive).
	 * @param end End of segment (exclusive).
	 */
	private double calcMean(int start, int end) {
		double sum = 0.0D;
		for (int i = start; i < end; i++) {
			sum += rawVals[i];
		}
		double m = sum / (end - start);
		return m;
	}
	private double calcLimit(int start, int end) {
		double sum = 0.0D;
		for (int i = start; i < end; i++) {
			sum += deltas[i];
		}
		//Original:
		//double m = sum / (end - start);
		//I think this is a mistake - it should be:
		double m = sum / (end - start - 1);
		// - because the average moving range is found
		//by dividing by one less than the number of data
		return m;
	}
	private void recalculateMeans() {
		int segmentStart = 0;
		for (int i = 0; i < rawVals.length; i++) {
			if (breakPoints[i]) {
				double m = calcMean(segmentStart, i);
				double l = calcLimit(segmentStart, i);
				for (int j = segmentStart; j < i; j++) {
					means[j] = m;
					amrs[j] = l;
				}
				segmentStart = i;
			}
		}
		// have to do the last segment manually
		double m = calcMean(segmentStart, rawVals.length);
		double l = calcLimit(segmentStart, rawVals.length);
		for (int j = segmentStart; j < rawVals.length; j++) {
			means[j] = m;
			amrs[j] = l;
		}
	}

	/**
	 *  Checks for a break based on the rule 2a.
	 *
	 * @param startIndex The point from which the method starts looking for the breakpoint.
	 * @return null if no Break2a, otherwise the start and end indices in the arrays where the run occurs
	 */
	private Pair existsBreak2a(int startIndex) {
		for (int i = startIndex; i < rawVals.length; i++){
			int breakEnd = break2a(i);
			if (breakEnd > -1) return new Pair(i, breakEnd);
		}
		return null;
	}

	private int break2a(int i) {
		int seq = 0;
		while ((i + seq < rawVals.length) && (rawVals[i + seq] > means[i+seq]) ){
			seq++;
		}
		if (seq >= 8) // there is a run of at least 8 points
			return i+seq-1; //return the index of the last point
		seq = 0;
		while ((i + seq < rawVals.length) && (rawVals[i + seq] < means[i+seq]) ){
			seq++;
		}
		if (seq >= 8) // there is a run of at least 8 points
			return i+seq-1; //return the index of the last point

		return -1;
	}

	/**
	 * Alternative version of the algorithm, with provided mean, used for hypothetical testing.
	 * @param i
	 * @return
	 */
	private Pair existsBreak2aM(int startIndex, int endIndex, double m) {
		for (int i = startIndex; i < endIndex; i++){
			int breakEnd = break2aM(i, m);
			if (breakEnd > -1) return new Pair(i, breakEnd);
		}
		return null;
	}


	private int break2aM(int i, double m) {
		int seq = 0;
		while ((i + seq < rawVals.length) && (rawVals[i + seq] > m)){
			seq++;
		}
		if (seq >= 8) // there is a run of at least 8 points
			return i+seq-1; //return the index of the last point
		seq = 0;
		while ((i + seq < rawVals.length) && (rawVals[i + seq] < m)){
			seq++;
		}
		if (seq >= 8) // there is a run of at least 8 points
			return i+seq-1; //return the index of the last point

		return -1;
	}

	private boolean existsBreakPointBefore(int breakEnd) {
		for (int i = 0; i < breakEnd; i++) {
			if (breakPoints[i]) return true;
		}
		return false;
	}
	private int getSecondBreakPointBefore(int p) {
		int last = 0;
		int secondLast = 0;
		for (int i = 0; i < p; i++) {
			if (breakPoints[i]) {
				secondLast = last;
				last = i;
			}
		}
		return secondLast;
	}

	private int getBreakPointBefore(int p) {
		int last = 0;
		for (int i = 0; i < p; i++) {
			if (breakPoints[i]) {
				last = i;
			}
		}
		return last;
	}

// TODO: parametrise this method to allow user to vary the rulebreak padding constants - currently hard-wired to 5.
// TODO: parametrise whether the algorithm uses breakpoint removal - currently always does.
	public void calculate() {

		int numberOfLoops = 0;
		while(maximumNumberOfLoops == 0 || numberOfLoops < maximumNumberOfLoops) {
			recalculateMeans();
			int breakSearchStart = 0;

			while(true) {
				Pair breakIndexRun = existsBreak2a(breakSearchStart);
				if (breakIndexRun == null) return;
				int breakStart = breakIndexRun.a;
				int breakEnd = breakIndexRun.b;

				if (breakStart < 5 ||
						breakPoints[breakStart] ||
						breakPoints[breakStart-1] ||
						breakPoints[breakStart-2] ||
						breakPoints[breakStart-3] ||
						breakPoints[breakStart-4] ||
						breakPoints[breakStart-5]) {

					if (breakEnd >= (breakPoints.length - 5) ||
							breakPoints[breakEnd] ||
							breakPoints[breakEnd+1] ||
							breakPoints[breakEnd+2] ||
							breakPoints[breakEnd+3] ||
							breakPoints[breakEnd+4] ||
							breakPoints[breakEnd+5]) {
						//note: don't break!
					}
					else {
						if (breakEnd + 1 < breakPoints.length) breakPoints[breakEnd+1] = true;
						if (existsBreakPointBefore(breakEnd)) {
							int bp = getSecondBreakPointBefore(breakEnd);
							double m = calcMean(bp, breakEnd);
							Pair p = existsBreak2aM(bp, breakEnd, m);
							if (p == null) {
								int last = getBreakPointBefore(breakEnd);
								breakPoints[last] = false;
							}
						}
						break;
					}

				}
				else {
					breakPoints[breakStart] = true;
					break;
				}
				breakSearchStart++;
			}


			numberOfLoops++;
		}

	}


	class Pair
	{
		int a;
		int b;
		Pair (int a, int b) {
			this.a = a;
			this.b = b;
		}
		public String toString() { return "("+ a + ", " + b + ")"; }
	}

	public static void main(String[] args) {
		double[] testVals = {0.1875,
				0.0625,
				0.0217391,
				0.0227273,
				0.0169492,
				0.0487805,
				0.0909091,
				0.025641,
				0,
				0.0217391,
				0.0491803,
				0.0357143,
				0.0333333,
				0.0344828,
				0.157895,
				0.12,
				0.113636,
				0.108108,
				0.0833333,
				0.0408163,
				0.0759494,
				0.0888889,
				0.172414,
				0.130435,
				0.0689655,
				0.118644,
				0.104167,
				0.0638298,
				0.0689655,
				0.183673,
				0.28,
				0.155556,
				0.268293,
				0.3125,
				0.285714,
				0.175,
				0.333333,
				0.232143,
				0.181818,
				0.28125,
				0.0833333,
				0.218182,
				0.285714,
				0.333333,
				0.254902,
				0.323077,
				0.230769,
				0.266667,
				0.366667,
				0.345455,
				0.178571,
				0.285714,
				0.714286,
				0.25,
				0.214286,
				0.224138,
				0.2,
				0.219512,
				0.421053,
				0.126761,
				0.166667,
				0.257576,
				0.272727,
				0.32,
				0.619048,
				0.20339,
				0.25,
				0.253333,
				0.314815,
				0.5,
				0.295775,
				0.347826,
				0.291667,
				0.191176,
				0.244444,
				0.146341,
				0.273973,
				0.25,
				0.301587};
//		Vector<Double> testVals2 = new Vector<Double>();
//		testVals2.add(new Double(0.1875));
//		testVals2.add(new Double(0.0625));
//		testVals2.add(new Double(0.0217391));
//		testVals2.add(new Double(0.0227273));
//		testVals2.add(null);
//		testVals2.add(new Double(0.0169492));
//		testVals2.add(new Double(0.0487805));
//		testVals2.add(new Double(0.0909091));

		SPCCalculator calc = new SPCCalculator(testVals, 0, 5);
		System.out.println("Before:\n" + calc);
		calc.calculate();
		System.out.println("After:\n" + calc);
	}

}
