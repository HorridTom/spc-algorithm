//This is Tom's copy of the code to run the SPC algorithm
//Questions:
//TODO: 1) Why use Vector<Double[]> and not just Vector<Double>?
//TODO: Bug - in both UCLH test datasets, the average moving range takes the value Infinity at least once

package uk.ac.ic.doc.wishnwl.tw;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.birt.data.engine.api.aggregation.Accumulator;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.i18n.ResourceConstants;

public class SPCAccumulator extends Accumulator {
	/**
	 * Does this accumulator return limits (true) or means (false)
	 */
	private boolean limits = false;
	public int maxIterations;
	public int breakPadding;
	int index = -1;
	boolean firstPass = true;
	SPCCalculator spcCalc;
	boolean isNull = false;
	Vector<Double> vals = new Vector<Double>();

	public SPCAccumulator(boolean limits, int maxIts, int padding) {
		this.limits = limits;
		this.maxIterations = maxIts;
		this.breakPadding = padding;
	}

	public SPCAccumulator(boolean limits) {
		this(limits, 0, 5);
	}

	public void start() {
		index = -1;
	}

	public void finish() {
		// at the end of the first pass, all data is loaded into the SPCCalculator object
		// and it can calculate the limits according to the algorithm.
		if (firstPass) {
			firstPass = false;
			spcCalc = new SPCCalculator(vals, maxIterations, breakPadding);
			spcCalc.calculate();
		}
	}

	@Override
	public Object getValue() throws DataException {
		if (firstPass) return null;

		if (isNull) return null;
		else if (limits) return spcCalc.getLimit(index);
		else return spcCalc.get(index);
	}

	@Override
	public void onRow(Object[] args) throws DataException {
		assert (args.length > 0);
		if (args[0] != null) {
			isNull = false;
			index++;
			if (firstPass) {
				try {
					//Add the passed value to vals as a double, catching errors
					vals.add(new Double(((Number)args[0]).doubleValue()));
				} catch (Exception e) {
					throw new DataException(ResourceConstants.DATATYPEUTIL_ERROR, e);
				}
			}
		}
		else isNull = true;
	}
	public static void main(String[] args) throws DataException {
		// Create a Vector to hold the data that will be analysed
		// TODO: TW: I don't understand why Double[] or Vector<Double> wouldn't do the job here
		Vector<Double[]> testVals = new Vector<Double[]>();

		// Populate with example data
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(3)});
		testVals.add(new Double[]{new Double(3)});
		testVals.add(new Double[]{null});
		testVals.add(new Double[]{new Double(4)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(3)});
		testVals.add(new Double[]{new Double(6)});
		testVals.add(new Double[]{new Double(5)});
		testVals.add(new Double[]{new Double(4)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(3)});
		testVals.add(new Double[]{new Double(4)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(4)});
		testVals.add(new Double[]{null});
		testVals.add(new Double[]{null});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(1)});
		testVals.add(new Double[]{new Double(2)});
		testVals.add(new Double[]{new Double(1)});

		// Create a SPCAccumulator object - this will hold the data and perform the analysis
		SPCAccumulator spca = new SPCAccumulator(true);

		//First pass - load data into vals, pass to SPCCalculator spcCalc
		spca.start();
		for (Iterator<Double[]> i = testVals.iterator(); i.hasNext();) {
			spca.onRow(i.next());
		}
		spca.finish();

		//Second pass - Get the mean or average moving range from spcCalc and add it to ret
		Vector ret = new Vector();
		spca.start();
		for (Iterator<Double[]> i = testVals.iterator(); i.hasNext();) {
			spca.onRow(i.next());
			ret.add(spca.getValue());
		}
		spca.finish();

		//Print the returned results out to the console
		for (Iterator i = ret.iterator(); i.hasNext();) {
			System.out.println(i.next());
		}

	}

}
