package fitness.thunderwave.fitocracy.exporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoRootGroup;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkout;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutDay;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutExercise;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutExerciseDetail;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutGroupExercise;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutInput;
import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutSet;

public class FitocracyApiManager {
	
    final static private String DATE_FORMAT = "yyyy-MM-dd";
    public final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    
    public void processCSVFileOnly(String fitocracyId, String path) throws Exception {


    	File exportFolder = new File(path);
    	
    	if(!exportFolder.exists()) {
    		boolean result = exportFolder.mkdir();
    		if(!result) {
    			throw new Exception("Unable to create directory");
    		}
    	} 

	    for (final File fileEntry : exportFolder.listFiles()) {
	        if (fileEntry.isFile()) {
	        	 System.out.println(fileEntry.getName());
	        	 if(fileEntry.getName().endsWith(".json")) {
	        		 String content = new String ( Files.readAllBytes( Paths.get(fileEntry.getPath()) ) );
	        		 if(StringUtils.isNotEmpty(content)) {
	        			 processJson(content, fitocracyId, null, path);
	        		 }
	        		 
	        	 }
	        } 
	    }
    }
    
    public void getWorkouts(String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date startDate, String path) throws Exception {
    	
    	File exportFolder = new File(path);
    	
    	if(!exportFolder.exists()) {
    		boolean result = exportFolder.mkdir();
    		if(!result) {
    			throw new Exception("Unable to create directory");
    		}
    	} 
    	
		Date startSearchDate = startDate;
		Date endSearchDate = getEndofDay(new Date());
		Date tmpDate = startSearchDate;

		List<CsvRow>  csvRows = new ArrayList<>(100);

		// doing one day at time sequentially, to keep the load on Fitocracy serves light
		while(tmpDate.getTime() <=  endSearchDate.getTime()) {
			try {
				// sleep for 2 seconds, don't want to DOS attack fitocracy
				Thread.sleep(2 * 1000L);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				return;
			}
			
			String jsonResponse = makeApiCall(fitocracyId, sessionId, fitocracyApiBaseUrl, tmpDate);

			List<CsvRow> workoutRows = processJson(jsonResponse, fitocracyId, tmpDate, exportFolder.getPath());
			csvRows.addAll(workoutRows);
			
			tmpDate = addDays(tmpDate, 1);

		}
		
		

    	
    }
	
	
	private String makeApiCall(String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date date) throws IOException  {

			
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Cookie", sessionId );

		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> respEntity = restTemplate.exchange(fitocracyApiBaseUrl 
																			+ "/user/"
																			+ fitocracyId
																			+ "/workouts/"
																			+ DATE_FORMATTER.format(date) 
																			+ "/",
																			HttpMethod.GET, entity, String.class);
		
		System.out.println("Getting Data for " + fitocracyId + " for date " + DATE_FORMATTER.format(date));
		
		if(respEntity.getStatusCode().is2xxSuccessful()) {

			String json = respEntity.getBody();
			return json;

		} else {
			System.err.println("Unsuccessful attempt to get data for " + fitocracyId + " for date " + DATE_FORMATTER.format(date) + "" + respEntity.getStatusCode());
		}
			

		return null;
	}
	
	private List<CsvRow> processJson(String json, String fitocracyId, Date date, String outputFolder) throws Exception  {
		
		List<CsvRow>  csvRows = new ArrayList<>();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		boolean foundData = false;
		
		try {

			DtoFitoWorkoutDay data = objectMapper.readValue(json, DtoFitoWorkoutDay.class);
			if(data.getData() != null && data.getData().size() > 0) {
				foundData = true;
				System.out.println("found " + data.getData().size() + " workout(s)");
				csvRows.addAll(processWorkoutDay(data));
			} else {
				if(StringUtils.isNotBlank(data.getError())) {
					throw new Exception(data.getError());
				} 
				System.out.println("found 0 workout(s)");
			}
			
		} catch (JsonProcessingException e) {
			System.err.println(e.getMessage());
		} 
		
		if(foundData) {
			if(date != null) {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter("exports/" + fitocracyId + "_" + DATE_FORMATTER.format(date) + ".json"));
					writer.write(json);
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			writeOutCsv(fitocracyId, csvRows, outputFolder);
		}
		
		return csvRows;
	}
	
	
	private void writeOutCsv(String fitocracyId, List<CsvRow> csvRows, String outputFolder) throws IOException {
		
		if(csvRows == null || csvRows.isEmpty()) {
			return;
		}
		
		String defaultCsvFilePath = outputFolder + "/" + fitocracyId + ".csv";
		
		File csvFile = new File(defaultCsvFilePath);
		
		FileWriter fileWriter = null;
		BufferedWriter csvWriter = null;
		
		if(!csvFile.exists()) {
			// Create csv headers
			fileWriter = new FileWriter(defaultCsvFilePath);
			csvWriter = new BufferedWriter(fileWriter);
		
			{ // Titles
				
				csvWriter.write(toCsv("Workout Time"));
				csvWriter.write(toCsv("Name"));

				// workout group info
				csvWriter.write(toCsv("Superset Name"));

				// workout exercise
				csvWriter.write(toCsv("Exercise Sequence"));
				csvWriter.write(toCsv("Exercise Name"));
				
				
				// set data
				csvWriter.write(toCsv("Set Sequence"));

				

				csvWriter.write(toCsv("Distance Unit"));
				csvWriter.write(toCsv("Distance Value"));

				csvWriter.write(toCsv("Time Unit"));
				csvWriter.write(toCsv("Time Value"));

				csvWriter.write(toCsv("Reps Value"));
				
				csvWriter.write(toCsv("Weight Unit"));
				csvWriter.write(toCsv("Weight Value"));
				

				csvWriter.write(toCsv("Assist Type"));
				csvWriter.write(toCsv("Assist Unit"));
				csvWriter.write(toCsv("Assist Value"));
				
				csvWriter.write(toCsv("Weighted Type"));
				csvWriter.write(toCsv("Weighted Unit"));
				csvWriter.write(toCsv("Weighted Value"));
				
				csvWriter.write(toCsv("Points"));
				
				csvWriter.write(toCsv("Workout Notes"));
				csvWriter.write(toCsv("Exercise Notes", false));
				
				csvWriter.write("\n");
				
			}
			
		} else {
			fileWriter = new FileWriter(defaultCsvFilePath, true);
			csvWriter = new BufferedWriter(fileWriter);
		}

		Map<String, String> exerciseMap = getExerciseMap();

		for (CsvRow csvRow : csvRows) {
			
			
			csvWriter.write(toCsv(csvRow.getWorkoutTime()));
			csvWriter.write(toCsv(csvRow.getWorkoutName()));

			// workout group info
			csvWriter.write(toCsv(csvRow.getGroupName()));

			// workout exercise
			csvWriter.write(toCsv(csvRow.getExerciseSequence()));
			
			if(exerciseMap.containsKey(csvRow.getExerciseId())) {
				csvWriter.write(toCsv(exerciseMap.get(csvRow.getExerciseId())));
			} else {
				csvWriter.write(toCsv("ID:" + csvRow.getExerciseId()));
			}

			// set data
			csvWriter.write(toCsv(csvRow.getSetSequence()));

			

			csvWriter.write(toCsv(csvRow.getDistanceUnit()));
			csvWriter.write(toCsv(csvRow.getDistanceValue()));

			csvWriter.write(toCsv(csvRow.getTimeUnit()));
			csvWriter.write(toCsv(csvRow.getTimeValue()));

			csvWriter.write(toCsv(csvRow.getRepsValue()));
			
			csvWriter.write(toCsv(csvRow.getWeightUnit()));
			csvWriter.write(toCsv(csvRow.getWeightValue()));
			

			csvWriter.write(toCsv(csvRow.getAssistedType()));
			csvWriter.write(toCsv(csvRow.getAssistedUnit()));
			csvWriter.write(toCsv(csvRow.getAssistedValue()));
			
			csvWriter.write(toCsv(csvRow.getWeightedType()));
			csvWriter.write(toCsv(csvRow.getWeightedUnit()));
			csvWriter.write(toCsv(csvRow.getWeightedValue()));
			
			csvWriter.write(toCsv(csvRow.getPoints()));
			
			csvWriter.write(toCsv(csvRow.getWorkoutNotes()));
			csvWriter.write(toCsv(csvRow.getExerciseNotes(), false));
			
			csvWriter.write("\n");
			
		}
		
		
		csvWriter.close();
	}

    

    
	private List<CsvRow> processWorkoutDay(DtoFitoWorkoutDay day) throws IOException {
		
		List<CsvRow> csvRows = new ArrayList<>();
		
		if(day == null) {
			return csvRows;
		}
		
		for (DtoFitoWorkout fitoWorkout : day.getData()) {
			
			if(fitoWorkout.getRootGroup() != null) {
				
				CsvRow workout = processWorkout(fitoWorkout, fitoWorkout.getRootGroup());
				csvRows.add(workout);
				
				
				// should always be group for the root
				if("group".equals(fitoWorkout.getRootGroup().getType())) {
					
					List<DtoFitoWorkoutExercise> children = fitoWorkout.getRootGroup().getChildren();
					
					int childCount = 1;
					int exSeqId = 1;
					for (DtoFitoWorkoutExercise child : children) {
						if("group".equals(child.getType())) {
							
							if(childCount > 1) {
								workout = new CsvRow(workout);
								workout.resetExerciseGroup();
								workout.resetExercise();
								workout.resetSet();
							}
							
							workout = processWorkoutGroup(workout, child);
							
							List<CsvRow> exerciseRows = processGroupChildrenExercise(workout, child.getChildren());
							csvRows.addAll(exerciseRows);
							
						} else if("exercise".equals(child.getType())) {
							
							if(childCount > 1) {
								workout = new CsvRow(workout);
								workout.resetExerciseGroup();
								workout.resetExercise();
								workout.resetSet();
							}

							
							List<CsvRow> exerciseRows = processChildExercise(workout, child, exSeqId);
							csvRows.addAll(exerciseRows);
							exSeqId += 1;
						}
						childCount++;
					}
				} 
			}
		}
		return csvRows;
	}
	
	private CsvRow processWorkout(DtoFitoWorkout fitoWorkout, DtoFitoRootGroup rootGroup) throws IOException {
		
		CsvRow row = new CsvRow();
		
		row.setWorkoutName(rootGroup.getName());
		row.setWorkoutNotes(rootGroup.getNotes());
		if(fitoWorkout.getWorkoutTimestamp() != null) {
			row.setWorkoutTime(fitoWorkout.getWorkoutTimestamp().toInstant().toString());
		}
		

		return row;
	}
	
	private CsvRow processWorkoutGroup(CsvRow csvRow, DtoFitoWorkoutExercise group) throws IOException {

		csvRow.setGroupName(group.getName());
		
		return csvRow;
	}

	private List<CsvRow> processGroupChildrenExercise(CsvRow workout, List<DtoFitoWorkoutGroupExercise> exercises) throws IOException {
		List<CsvRow> csvRows = new ArrayList<>();
		
		if(exercises == null) {
			return csvRows;
		}
		
		CsvRow tmpExRow = workout;
		
		int exSeq = 1;
		for (DtoFitoWorkoutGroupExercise ex : exercises) {

			DtoFitoWorkoutExerciseDetail detail = ex.getExercise();
			
			if(detail != null) {
				
				if(exSeq > 1) {
					tmpExRow = new CsvRow(tmpExRow);
					tmpExRow.resetExercise();
					tmpExRow.resetSet();
				}
				csvRows.add(tmpExRow);
				
				
				tmpExRow = processExercise(tmpExRow, detail, exSeq);
				List<DtoFitoWorkoutSet> sets = detail.getSets();
				
				CsvRow tmpSetRow = tmpExRow;
				int setSeq = 1;
				for (DtoFitoWorkoutSet set : sets) {
					
					if(setSeq > 1) {
						tmpSetRow = new CsvRow(tmpExRow);
						tmpSetRow.resetSet();
						csvRows.add(tmpSetRow);
					}
					
					
					processSet(tmpSetRow, set, setSeq);
					setSeq++;
				}
			}
			exSeq++;
			
		}
		return csvRows;
	}

	
	private List<CsvRow> processChildExercise(CsvRow row, DtoFitoWorkoutExercise exercise, int exSeq) throws IOException {
		List<CsvRow> csvRows = new ArrayList<>();
		
		if(exercise == null) {
			return csvRows;
		}

		DtoFitoWorkoutExerciseDetail detail = exercise.getExercise();
		CsvRow tmpExRow = row;
		
		
		if(detail != null) {
			
			if(exSeq > 1) {
				tmpExRow = new CsvRow(tmpExRow);
				tmpExRow.resetExercise();
				tmpExRow.resetSet();
				csvRows.add(tmpExRow);
			}
			
			processExercise(tmpExRow, detail, exSeq);
			List<DtoFitoWorkoutSet> sets = detail.getSets();
			
			CsvRow tmpSetRow = tmpExRow;
			int setSeq = 1;
			for (DtoFitoWorkoutSet set : sets) {
				if(setSeq > 1) {
					tmpSetRow = new CsvRow(tmpSetRow);
					tmpSetRow.resetSet();
					csvRows.add(tmpSetRow);
				}
				
				tmpSetRow = processSet(tmpSetRow, set, setSeq);
				setSeq++;
			}
		}
		return csvRows;
	}
	
	private CsvRow processExercise(CsvRow row, DtoFitoWorkoutExerciseDetail exerciseDetail, int seq) throws IOException {
		
		row.setExerciseSequence("" + seq);
		row.setExerciseId("" + exerciseDetail.getExerciseId());
		if(StringUtils.isNotBlank(exerciseDetail.getNotes())) {
			row.setExerciseNotes(exerciseDetail.getNotes());
		}

		return row;
	}
	
	
	private CsvRow processSet(CsvRow setRow, DtoFitoWorkoutSet set, int seq) throws IOException {
		
		setRow.setSetSequence("" + seq);
		
		if(set.getPoints() != null) {
			setRow.setPoints("" + set.getPoints().longValue());
		}

		List<DtoFitoWorkoutInput> inputs = set.getInputs();
		
		for (DtoFitoWorkoutInput input : inputs) {
			
			if("distance".equals(input.getType())) {
				setRow.setDistanceUnit(input.getUnit());
				setRow.setDistanceValue("" + input.getValue());
				
			} else if("time".equals(input.getType())) {
				
				setRow.setTimeUnit(input.getUnit());
				setRow.setTimeValue("" + input.getValue());
				
			} else if("reps".equals(input.getType())) {
				
				setRow.setRepsValue("" + input.getValue());
				
			} else if("weight".equals(input.getType())) {
				
				setRow.setWeightUnit(input.getUnit());
				setRow.setWeightValue("" + input.getValue());

			}

			if("assisted".equals(input.getAssistType())) {
				
				setRow.setAssistedType(input.getAssistType());
				setRow.setAssistedUnit(input.getUnit());
				setRow.setAssistedValue("" + input.getValue());
				
			} else if("weighted".equals(input.getAssistType())) { 
				
				setRow.setWeightedType(input.getAssistType());
				setRow.setWeightedUnit(input.getUnit());
				setRow.setWeightedValue("" + input.getValue());

			}
		}
		return setRow;
	}
	
	public String toCsv(String input) {
		return toCsv(input, true);
	}
	
	public String toCsv(String input, boolean addComma) {
		String ret = "";
		if(StringUtils.isNotEmpty(input)) {
			ret = StringEscapeUtils.escapeCsv(input);
		} 
		if(addComma) {
			ret += ",";
		}
		return ret;
	}

	
    public static Date getEndofDay(Date date) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
    
    
	private class CsvRow {
		
		String workoutName = null;
		String workoutNotes = null;
		String workoutTime = null;
		
		String groupName = null;

		
		String exerciseSequence = null;
		String exerciseId = null;
		String exerciseNotes = null;
		
		String setSequence = null;
		String points = null;
		
		String distanceUnit = null;
		String distanceValue = null;
		
		String timeUnit = null;
		String timeValue = null;
		
		
		String repsValue = null;
		
		String weightUnit = null;
		String weightValue = null;
		
		String assistedType = null;
		String assistedUnit = null;
		String assistedValue = null;
		
		String weightedType = null;
		String weightedUnit = null;
		String weightedValue = null;
		
		
		
		public CsvRow() {
			super();
		}
		
		public CsvRow(CsvRow row) {
			super();
			
			this.workoutName = row.workoutName;
			this.workoutNotes = row.workoutNotes;
			this.workoutTime = row.workoutTime;
			
			this.groupName = row.groupName;

			
			this.exerciseSequence = row.exerciseSequence;
			this.exerciseId = row.exerciseId;
			this.exerciseNotes = row.exerciseNotes;
			
			this.setSequence = row.setSequence;
			this.points = row.points;
			
			this.distanceUnit = row.distanceUnit;
			this.distanceValue = row.distanceValue;
			
			this.timeUnit = row.timeUnit;
			this.timeValue = row.timeValue;
			
			
			this.repsValue = row.repsValue;
			
			this.weightUnit = row.weightUnit;
			this.weightValue = row.weightValue;
			
			this.assistedType = row.assistedType;
			this.assistedUnit = row.assistedUnit;
			this.assistedValue = row.assistedValue;
			
			this.weightedType = row.weightedType;
			this.weightedUnit = row.weightedUnit;
			this.weightedValue = row.weightedValue;
		}
		
		public void resetExerciseGroup() {
			this.groupName = null;
		}
		
		public void resetExercise() {
			this.exerciseSequence = null;
			this.exerciseId = null;
			this.exerciseNotes = null;
		}
		
		public void resetSet() {
			this.setSequence = null;
			this.points = null;
			
			this.distanceUnit = null;
			this.distanceValue = null;
			
			this.timeUnit = null;
			this.timeValue = null;
			
			
			this.repsValue = null;
			
			this.weightUnit = null;
			this.weightValue = null;
			
			this.assistedType = null;
			this.assistedUnit = null;
			this.assistedValue = null;
			
			this.weightedType = null;
			this.weightedUnit = null;
			this.weightedValue = null;
		}
		
		
		public String getWorkoutName() {
			return workoutName;
		}
		public void setWorkoutName(String workoutName) {
			this.workoutName = workoutName;
		}
		public String getWorkoutNotes() {
			return workoutNotes;
		}
		public void setWorkoutNotes(String workoutNotes) {
			this.workoutNotes = workoutNotes;
		}
		public String getWorkoutTime() {
			return workoutTime;
		}
		public void setWorkoutTime(String workoutTime) {
			this.workoutTime = workoutTime;
		}
		public String getGroupName() {
			return groupName;
		}
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}
		public String getExerciseSequence() {
			return exerciseSequence;
		}
		public void setExerciseSequence(String exerciseSequence) {
			this.exerciseSequence = exerciseSequence;
		}
		public String getExerciseId() {
			return exerciseId;
		}
		public void setExerciseId(String exerciseId) {
			this.exerciseId = exerciseId;
		}
		public String getPoints() {
			return points;
		}
		public void setPoints(String points) {
			this.points = points;
		}
		public String getDistanceUnit() {
			return distanceUnit;
		}
		public void setDistanceUnit(String distanceUnit) {
			this.distanceUnit = distanceUnit;
		}
		public String getDistanceValue() {
			return distanceValue;
		}
		public void setDistanceValue(String distanceValue) {
			this.distanceValue = distanceValue;
		}
		public String getTimeUnit() {
			return timeUnit;
		}
		public void setTimeUnit(String timeUnit) {
			this.timeUnit = timeUnit;
		}
		public String getTimeValue() {
			return timeValue;
		}
		public void setTimeValue(String timeValue) {
			this.timeValue = timeValue;
		}
		public String getRepsValue() {
			return repsValue;
		}
		public void setRepsValue(String repsValue) {
			this.repsValue = repsValue;
		}
		public String getWeightUnit() {
			return weightUnit;
		}
		public void setWeightUnit(String weightUnit) {
			this.weightUnit = weightUnit;
		}
		public String getWeightValue() {
			return weightValue;
		}
		public void setWeightValue(String weightValue) {
			this.weightValue = weightValue;
		}
		public String getAssistedType() {
			return assistedType;
		}
		public void setAssistedType(String assistedType) {
			this.assistedType = assistedType;
		}
		public String getAssistedUnit() {
			return assistedUnit;
		}
		public void setAssistedUnit(String assistedUnit) {
			this.assistedUnit = assistedUnit;
		}
		public String getAssistedValue() {
			return assistedValue;
		}
		public void setAssistedValue(String assistedValue) {
			this.assistedValue = assistedValue;
		}
		public String getWeightedType() {
			return weightedType;
		}
		public void setWeightedType(String weightedType) {
			this.weightedType = weightedType;
		}
		public String getWeightedUnit() {
			return weightedUnit;
		}
		public void setWeightedUnit(String weightedUnit) {
			this.weightedUnit = weightedUnit;
		}
		public String getWeightedValue() {
			return weightedValue;
		}
		public void setWeightedValue(String weightedValue) {
			this.weightedValue = weightedValue;
		}
		public String getSetSequence() {
			return setSequence;
		}
		public void setSetSequence(String setSequence) {
			this.setSequence = setSequence;
		}

		public String getExerciseNotes() {
			return exerciseNotes;
		}

		public void setExerciseNotes(String exerciseNotes) {
			this.exerciseNotes = exerciseNotes;
		}
		
		

	}
	
	private Map<String, String> getExerciseMap() throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/exercises.csv");

		Map<String, String> map = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(",");
		        if(values.length > 1) {
		        	map.put(values[0], values[1]);
		        }
		    }
		}
		return map;
	}
	
}
