// This class runs SPCAccumulator on the data in each .csv file in a specified directory
// and saves the output as a new .csv file in the same directory.

package uk.ac.ic.doc.wishnwl.tw;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.io.*;

import org.eclipse.birt.data.engine.core.DataException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class SPCIO {

	 CSVReader reader;
     List<String[]> myEntries;
     Vector<Double[]> csvVals;

	public void loadCsv(String filename) {

		try {
			this.reader = new CSVReader(new FileReader(filename));
			this.myEntries = reader.readAll();}
		catch(IOException e) {
			e.printStackTrace();
			}
		finally {
			try{this.reader.close();}
			catch(IOException e){
				e.printStackTrace();
			}

			}


	}

	public void makeVector() {

		this.csvVals = new Vector<Double[]>();
		Double x = new Double(0);

		for (String[] s : myEntries) {
			try {
				x = Double.valueOf(String.valueOf(s[0].toCharArray()));
			}
			catch (NumberFormatException e) {
//				System.out.println(String.valueOf(s[0].toCharArray())
//						+ " is not a Double - replaced with null.");
				x = null;
			}
			finally {
				csvVals.add(new Double[]{x});
			}

		}

	}

	public static Vector<Double[]> analyseCsv(String fName, int maxIterations) throws DataException {
		SPCIO testIO = new SPCIO();
		testIO.loadCsv(fName);
		testIO.makeVector();

		//System.out.println("Begin Accumulator");

		SPCAccumulator spca = new SPCAccumulator(false, maxIterations);

		//First pass - load data into vals, pass to SPCCalculator spcCalc
		spca.start();
		for (Iterator<Double[]> i = testIO.csvVals.iterator(); i.hasNext();) {
			spca.onRow(i.next());
		}
		spca.finish();

		//Get the mean from spcCalc and add it to ret
		Vector ret = new Vector();
		spca.start();
		for (Iterator<Double[]> i = testIO.csvVals.iterator(); i.hasNext();) {
			spca.onRow(i.next());
			ret.add(spca.getValue());
		}
		spca.finish();

		//System.out.println("End Accumulator");

		//System.out.println("Begin Accumulator");

		SPCAccumulator spcm = new SPCAccumulator(true, maxIterations);

		//First pass - load data into vals, pass to SPCCalculator spcCalc
		spcm.start();
		for (Iterator<Double[]> i = testIO.csvVals.iterator(); i.hasNext();) {
			spcm.onRow(i.next());
		}
		spcm.finish();

		//Get the average moving range from spcCalc and add it to ret
		Vector retm = new Vector();
		spcm.start();
		for (Iterator<Double[]> i = testIO.csvVals.iterator(); i.hasNext();) {
			spcm.onRow(i.next());
			retm.add(spcm.getValue());
		}
		spcm.finish();

		//System.out.println("End Accumulator");

		//structure the output
		int n = ret.size();
		Vector<Double[]> vOut = new Vector<Double[]>(n);



		//System.out.println("Structure Output");
		for (int i = 0; i < n; i++) {
			Double[] vItem = new Double[3];
			vItem[0] = testIO.csvVals.get(i)[0];
			if (ret.get(i) != null) {
				vItem[1] = Double.valueOf(String.valueOf(ret.get(i)));
			} else {
				vItem[1] = null;
			}
			if (retm.get(i) != null) {
				vItem[2] = Double.valueOf(String.valueOf(retm.get(i)));
			} else {
				vItem[2] = null;
			}

			vOut.add(i, vItem);
		}

		return vOut;
	}

	public static Vector<Double[]> analyseCsv(String fName) throws DataException {
		return analyseCsv(fName, 0);
	}

	public static void saveSpcToCsv(String fName, String label, int maxIterations, Vector<Double[]> v) {
		int n = v.size();

		CSVWriter writer;
		boolean fOpen;
		String[] sOut = new String[3];
		String saveName = new String();

		try {
			// TODO: use a regex to do this properly
			saveName = fName.substring(0, fName.length() - 4) + "_" + label + "_OUT.csv";
			writer = new CSVWriter(new FileWriter(saveName));
			fOpen = true;
			//System.out.println("File Open: " + saveName);
		}
		catch(IOException e) {
			e.getStackTrace();
			fOpen = false;
			writer = null;
		}
		finally {
			//
		}

	     if (fOpen == true) {try{
	    	 //System.out.println("Writing...");

	    	 for (int j = 0; j < n; j++) {
	    		 sOut[0] = String.valueOf(v.get(j)[0]);
	    		 sOut[1] = String.valueOf(v.get(j)[1]);
	    		 sOut[2] = String.valueOf(v.get(j)[2]);

	    		 writer.writeNext(sOut);
	    	 }

	    	 	writer.close();
	    	 	//System.out.println("File Closed.");
	     	}
	     	catch (IOException e) {
	    	 	e.getStackTrace();
	     	}
	     	finally {
				//
			}
	     }
	}


	public static void csvSPC(String fName, int maxIterations) throws DataException {

		Vector<Double[]> vOut = new Vector<Double[]>();
		vOut = analyseCsv(fName, maxIterations);
		saveSpcToCsv(fName, "", maxIterations, vOut);

	}

	public static void csvSPC(String fName) throws DataException {
		csvSPC(fName, 0);
	}

	public static boolean equalVectors (Vector<Double[]> u, Vector<Double[]> v) {


		if(u.size() != v.size()) return false;

		boolean status = true;
		for (int j = 0; j < u.size(); j++) {
			if (u.get(j)[0] == null || u.get(j)[1] == null || u.get(j)[2] == null) {
				if (!(u.get(j)[0] == null && u.get(j)[1] == null && u.get(j)[2] == null)) {
					status = false;
				}
			} else {
			status = status && u.get(j)[0].equals(v.get(j)[0]) && u.get(j)[1].equals(v.get(j)[1]) && u.get(j)[2].equals(v.get(j)[2]);
			}
		}

		return status;
	}

	public static void main(String[] args) throws DataException {

		File folder = new File("C:\\Users\\tw299\\git\\spc-algorithm\\SPCalgorithm1\\data");
		File[] listOfFiles = folder.listFiles();
		String fileName = new String();
		int nloops = 0;

		//Set this flag to true to save output of each stage of the algorithm,
		//or false to only save final result.
		boolean outputStages = true;

		for (int i = 0;i < listOfFiles.length; i++) {
			fileName = listOfFiles[i].getAbsolutePath();
			//SPCIO.csvSPC(fileName, 0);
			Vector<Double[]> endResult = new Vector<Double[]>();
			Vector<Double[]> result = new Vector<Double[]>();
			endResult = analyseCsv(fileName);
			saveSpcToCsv(fileName, "endresult", 0, endResult);
			if(outputStages){
				nloops = 0;
				while(!equalVectors(endResult, result)) {
					nloops++;
					result = analyseCsv(fileName, nloops);
					saveSpcToCsv(fileName, String.valueOf(nloops), nloops, result);
				}
				System.out.println("Data in file " + fileName + " analysed in " + nloops + " stages.");
			}
			result = null;
			endResult = null;

		}



	}

}

