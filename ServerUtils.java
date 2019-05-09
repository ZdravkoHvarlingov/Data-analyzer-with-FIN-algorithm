import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.FIN;

public class ServerUtils {
			
	private static final int MORNING_ID = 1;
	private static final int LUNCH_ID = 2;
	private static final int AFTERNOON_ID = 3;
	private static final int EVENING_ID = 4;
	private static final int NIGHT_ID = 5;
	private static final int STARTING_ID = 6;
	
	private static final int EVENT_USER_IND = 0;
	private static final int EVENT_TIME_IND = 1;
	private static final int EVENT_CONTEXT_IND = 2;
	private static final int EVENT_NAME_IND = 3;
	
	public static String runAlgo(String fileDir, String uniqueId) throws IOException {
		
		String formatedLogDir = "Server" + File.separator + "formatedLog" + uniqueId;
		String outputDir = "Server" + File.separator + "output" + uniqueId;
		String finalResultDir = "Server" + File.separator + "finalResult" + uniqueId;
		
		BufferedReader fileIn = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));
		
		BufferedWriter fileOut = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(formatedLogDir), "UTF-8"));
		
		Map<String, Integer> colValueToType = new TreeMap<String, Integer>();
		Map<String, Integer> colValueToId = new TreeMap<String, Integer>();
		Map<Integer, String> idToColValue = new TreeMap<Integer, String>();
		idToColValue.put(MORNING_ID, "Morning");
		colValueToType.put("Morning", EVENT_TIME_IND);
		idToColValue.put(LUNCH_ID, "Lunch");
		colValueToType.put("Lunch", EVENT_TIME_IND);
		idToColValue.put(AFTERNOON_ID, "Afternoon");
		colValueToType.put("Afternoon", EVENT_TIME_IND);
		idToColValue.put(EVENING_ID, "Evening");
		colValueToType.put("Evening", EVENT_TIME_IND);
		idToColValue.put(NIGHT_ID, "Night");
		colValueToType.put("Night", EVENT_TIME_IND);
		Integer nextId = STARTING_ID;
		
		String line = fileIn.readLine();
		while((line = fileIn.readLine()) != null) {
			String formatedLine = "";
			Integer[] lineIds = new Integer[4];
			String[] splitLine = line.split(";");
			
			//If there is no information about the user, just skip the log line
			if(splitLine[4].split("'").length < 2) {
				continue;
			}
			
			Integer hour = Integer.parseInt(splitLine[0].split(",")[1].substring(1).split(":")[0]);
			lineIds[0] = convertHourToId(hour);
			
			if(!colValueToId.containsKey(splitLine[1])) {
				colValueToId.put(splitLine[1], nextId);
				colValueToType.put(splitLine[1], EVENT_CONTEXT_IND);
				idToColValue.put(nextId, splitLine[1]);
				++nextId;
			}
			lineIds[1] = colValueToId.get(splitLine[1]);
			
			if(!colValueToId.containsKey(splitLine[3])) {
				colValueToId.put(splitLine[3], nextId);
				colValueToType.put(splitLine[3], EVENT_NAME_IND);
				idToColValue.put(nextId, splitLine[3]);
				++nextId;
			}
			lineIds[2] = colValueToId.get(splitLine[3]);
			
			splitLine[4] = splitLine[4].split("'")[1];
			if(!colValueToId.containsKey(splitLine[4])) {
				colValueToId.put(splitLine[4], nextId);
				colValueToType.put(splitLine[4], EVENT_USER_IND);
				idToColValue.put(nextId, splitLine[4]);
				++nextId;
			}
			lineIds[3] = colValueToId.get(splitLine[4]);
			
			Arrays.sort(lineIds);
			for (int i = 0; i < lineIds.length; i++) {
				formatedLine += lineIds[i].toString();
				if(i < 3) {
					formatedLine += " ";
				}
				else {
					formatedLine += System.lineSeparator();
				}
			}
			
			fileOut.write(formatedLine);
			fileOut.flush();
		}
		
		double minsup = 0.7;
		FIN algorithm = new FIN();
		algorithm.runAlgorithm(formatedLogDir, minsup, outputDir);
		while(minsup > 0 && countLines(outputDir) < 20) {
			minsup -= 0.05;
			algorithm.runAlgorithm(formatedLogDir, minsup, outputDir);
		}
		
		BufferedReader algOutput = new BufferedReader(
				new InputStreamReader(new FileInputStream(outputDir), "UTF-8"));
		
		BufferedWriter finalResult = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(finalResultDir), "UTF-8"));
		
		while((line = algOutput.readLine()) != null) {
			String[] ids = line.split("#")[0].split(" ");
			String[] values = new String[4];
			for (int i = 0; i < ids.length; i++) {
				String value = idToColValue.get(Integer.parseInt(ids[i]));
				values[colValueToType.get(value)] = value;
			}
			finalResult.write(GetMessage(values) + System.lineSeparator());
		}
		
		fileIn.close();
		fileOut.close();
		algOutput.close();
		finalResult.close();
		
		new File(fileDir).delete();
		new File(formatedLogDir).delete();
		new File(outputDir).delete();
		
		return finalResultDir;
	}
	
	private static Integer convertHourToId(Integer hour) {
		
		if(hour >= 6 && hour <= 11) {
			return MORNING_ID;
		}
		if(hour >= 12 && hour <= 14) {
			return LUNCH_ID;
		}
		if(hour >= 15 && hour <= 18) {
			return AFTERNOON_ID;
		}
		if(hour >= 19 && hour <= 23) {
			return EVENING_ID;
		}
		if(hour >= 0 && hour <= 5) {
			return NIGHT_ID;
		}
		
		return -1;
	}
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	public static String GetMessage(String[] values) {
		int count = 0;
		for (int i = 0; i < values.length; i++) {
			if(values[i] != null) {
				++count;
			}
		}
		
		if(count == 1) {
			return GetMessageForOneComponent(values);
		}
		if(count == 2) {
			return GetMessageForTwoComponents(values);
		}
		if(count == 3) {
			return GetMessageForThreeComponents(values);
		}
		return GetMessageForFourComponents(values);
		
	}
	
	public static String GetMessageForOneComponent(String[] values) {
		if(values[EVENT_TIME_IND] != null) {
			return String.format("High activity during part of the day: \"%s\".", values[EVENT_TIME_IND]);
		}
		if(values[EVENT_USER_IND] != null) {
			return String.format("High activity from user ID: \"%s\".", values[EVENT_USER_IND]);
		}
		if(values[EVENT_NAME_IND] != null) {
			return String.format("Often performed activity: \"%s\".", values[EVENT_NAME_IND]);
		}
		if(values[EVENT_CONTEXT_IND] != null) {
			return String.format("A lot of activities related to context : \"%s\".", values[EVENT_CONTEXT_IND]);
		}
		
		return null;
	}
	
	public static String GetMessageForTwoComponents(String[] values) {
		if (values[EVENT_TIME_IND] != null && values[EVENT_USER_IND] != null) {
			return String.format("Highly active user with ID \"%s\" during time of the day \"%s\".",
					values[EVENT_USER_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_TIME_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("A lot of activities related to context \"%s\" during time of the day \"%s\".",
					values[EVENT_CONTEXT_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_TIME_IND] != null && values[EVENT_NAME_IND] != null) {
			return String.format("Often performed activity \"%s\" during time of the day \"%s\".",
					values[EVENT_NAME_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_USER_IND] != null && values[EVENT_NAME_IND] != null) {
			return String.format("User with ID \"%s\" often do activity \"%s\".",
					values[EVENT_USER_IND], values[EVENT_NAME_IND]);
		}
		if(values[EVENT_USER_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("User with ID \"%s\" often do activities related to context \"%s\".",
					values[EVENT_USER_IND], values[EVENT_CONTEXT_IND]);
		}
		if(values[EVENT_NAME_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("Activity \"%s\" is often performed related to context \"%s\".",
					values[EVENT_NAME_IND], values[EVENT_CONTEXT_IND]);
		}
		
		return null;
	}
	
	public static String GetMessageForThreeComponents(String[] values) {
		if(values[EVENT_TIME_IND] != null && values[EVENT_NAME_IND] != null && values[EVENT_USER_IND] != null) {
			return String.format("User with ID \"%s\" often do activity \"%s\" during time of the day \"%s\".",
					values[EVENT_USER_IND], values[EVENT_NAME_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_TIME_IND] != null && values[EVENT_NAME_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("Activity \"%s\" is often performed related to context \"%s\" during time of the day \"%s\".",
					values[EVENT_NAME_IND], values[EVENT_CONTEXT_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_TIME_IND] != null && values[EVENT_USER_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("User with ID \"%s\" often do activities related to context \"%s\" during time of the day \"%s\".",
					values[EVENT_USER_IND], values[EVENT_CONTEXT_IND], values[EVENT_TIME_IND]);
		}
		if(values[EVENT_USER_IND] != null && values[EVENT_NAME_IND] != null && values[EVENT_CONTEXT_IND] != null) {
			return String.format("User with ID \"%s\" often do activity \"%s\" related to context \"%s\".",
					values[EVENT_USER_IND], values[EVENT_NAME_IND], values[EVENT_CONTEXT_IND]);
		}
		
		return null;
	}
	
	public static String GetMessageForFourComponents(String[] values) {
		return String.format("User with ID \"%s\" often do activity \"%s\" related to context \"%s\" during time of the day \"%s\".",
				values[EVENT_USER_IND], values[EVENT_NAME_IND], values[EVENT_CONTEXT_IND], values[EVENT_TIME_IND]);
	}
}
