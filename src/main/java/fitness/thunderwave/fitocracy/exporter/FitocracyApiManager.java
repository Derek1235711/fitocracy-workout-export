package fitness.thunderwave.fitocracy.exporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
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
    
    public void getWorkouts(String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date startDate) throws IOException {
    	
		Date startSearchDate = startDate;
		Date endSearchDate = getEndofDay(new Date());
		Date tmpDate = startSearchDate;

		
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(fitocracyId + ".csv"));

		// doing one day at time sequentially, to keep the load on Fitocracy serves light
		while(tmpDate.getTime() <=  endSearchDate.getTime()) {

			makeApiCall(csvWriter, fitocracyId, sessionId, fitocracyApiBaseUrl, tmpDate);
			
			tmpDate = addDays(tmpDate, 1);
			try {
				// sleep for 1 second, don't want to DOS attack fitocracy
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
			csvWriter.flush();
		}
		
		csvWriter.close();
    	
    }
	
	
	private void makeApiCall(BufferedWriter csvWriter, String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date date)  {
		
		try {
			
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
				
				
				ObjectMapper objectMapper = new ObjectMapper();
				String json = respEntity.getBody();
				
				if(json != null && json.length() > 0) {

					boolean foundData = false;
					
					try {
						DtoFitoWorkoutDay data = objectMapper.readValue(json, DtoFitoWorkoutDay.class);
						if(data.getData() != null && data.getData().size() > 0) {
							foundData = true;
							System.out.println("found " + data.getData().size() + " workout(s)");
						} else {
							System.out.println("found 0 workout(s)");
						}
						
					} catch (JsonProcessingException e) {
						System.err.println(e.getMessage());
					} 
					
					if(foundData) {
						try {
							BufferedWriter writer = new BufferedWriter(new FileWriter(fitocracyId + "_" + DATE_FORMATTER.format(date) + ".json"));
							writer.write(json);
							writer.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

				}
			}
			
		} catch(HttpClientErrorException e) {
			System.err.println(e.getMessage());
			return;
		}

	}
	

    

    
	public void processWorkoutDay(BufferedWriter csvWriter, DtoFitoWorkoutDay day) throws Exception {
		
		if(day == null) {
			return;
		}
		
		for (DtoFitoWorkout fitoWorkout : day.getData()) {
			
			if(fitoWorkout.getRootGroup() != null) {
				
				processWorkout(csvWriter, fitoWorkout, fitoWorkout.getRootGroup());
				
				
				// should always be group for the root
				if("group".equals(fitoWorkout.getRootGroup().getType())) {
					
					List<DtoFitoWorkoutExercise> children = fitoWorkout.getRootGroup().getChildren();
					
					int exSeqId = 1;
					for (DtoFitoWorkoutExercise child : children) {
						if("group".equals(child.getType())) {
							
							processWorkoutGroup(csvWriter, child);
							
							processGroupChildrenExercise(csvWriter, child.getChildren());
							
						} else if("exercise".equals(child.getType())) {
							processChildExercise(csvWriter, child, exSeqId);
							exSeqId += 1;
						}
					}
				} 
			}
		}
	}
	
	private void processWorkout(BufferedWriter csvWriter, DtoFitoWorkout fitoWorkout, DtoFitoRootGroup rootGroup) throws IOException {
		
		csvWriter.write(toCsv(rootGroup.getName()));
		csvWriter.write(toCsv(rootGroup.getNotes()));
		csvWriter.write(toCsv(fitoWorkout.getWorkoutTimestamp().toInstant().toString()));


	}
	
	private void processWorkoutGroup(BufferedWriter csvWriter, DtoFitoWorkoutExercise group) throws IOException {

		csvWriter.write(toCsv(group.getName()));
	}

	private void processGroupChildrenExercise(BufferedWriter csvWriter, List<DtoFitoWorkoutGroupExercise> exercises) throws Exception {
		if(exercises == null) {
			return;
		}
		
		int exSeq = 1;
		for (DtoFitoWorkoutGroupExercise ex : exercises) {
			
			
			DtoFitoWorkoutExerciseDetail detail = ex.getExercise();
			
			if(detail != null) {
				processExercise(csvWriter, detail, exSeq);
				List<DtoFitoWorkoutSet> sets = detail.getSets();
				
				int setSeq = 1;
				for (DtoFitoWorkoutSet set : sets) {
					
					processSet(csvWriter, set, setSeq);
					setSeq++;
				}
			}
			exSeq++;
			
		}
		
	}

	
	private void processChildExercise(BufferedWriter csvWriter, DtoFitoWorkoutExercise exercise, int exSeq) throws Exception {
		if(exercise == null) {
			return;
		}

		DtoFitoWorkoutExerciseDetail detail = exercise.getExercise();
		
		if(detail != null) {
			processExercise(csvWriter, detail, exSeq);
			List<DtoFitoWorkoutSet> sets = detail.getSets();
			
			int setSeq = 1;
			for (DtoFitoWorkoutSet set : sets) {
				
				processSet(csvWriter, set, setSeq);
				setSeq++;
			}
		}

	}
	
	private void processExercise(BufferedWriter csvWriter, DtoFitoWorkoutExerciseDetail exerciseDetail, int seq) throws Exception {

		csvWriter.write(toCsv("" + seq));
		csvWriter.write(toCsv("" + exerciseDetail.getExerciseId()));

		
		// TODO get list of exercises
		
//		if(exerciseDetail.getExerciseId() != null) {
//			Long exId = exerciseDetail.getExerciseId();
//			if(this.fitocracyDuplicatedExerciseMap.containsKey(exId)) {
//				exId = this.fitocracyDuplicatedExerciseMap.get(exId);
//				logger.debug("Found duplicate fitocracy exercise:" + exerciseDetail.getExerciseId() + " -> " + exId);
//			}
//			VwExercise vwExercise = viewDao.getVwExerciseByOldId(exId);
//			if(vwExercise != null) {
//				tbWorkoutExercise.setExerciseId(vwExercise.getExerciseId());
//			} else {
//				throw new Exception("Can't find exercise: " + exerciseDetail);
//			}
//		}

		
	}
	
	
	private void processSet(BufferedWriter csvWriter, DtoFitoWorkoutSet set, int seq) throws IOException {

		csvWriter.write(toCsv("" + seq));
		
		if(set.getPoints() != null) {
			csvWriter.write(toCsv("" + set.getPoints().longValue()));
		}

		List<DtoFitoWorkoutInput> inputs = set.getInputs();
		
		for (DtoFitoWorkoutInput input : inputs) {
			
			if("distance".equals(input.getType())) {
				
				csvWriter.write(toCsv(input.getUnit()));
				csvWriter.write(toCsv("" + input.getValue()));
				
			} else if("time".equals(input.getType())) {
				
				csvWriter.write(toCsv(input.getUnit()));
				csvWriter.write(toCsv("" + input.getValue()));
				
			} else if("reps".equals(input.getType())) {
				
				csvWriter.write(toCsv("" + input.getValue()));
				
			} else if("weight".equals(input.getType())) {
				
				csvWriter.write(toCsv(input.getUnit()));
				csvWriter.write(toCsv("" + input.getValue()));

			}

			if("assisted".equals(input.getAssistType())) {
				
				csvWriter.write(toCsv(input.getAssistType()));
				csvWriter.write(toCsv(input.getUnit()));
				csvWriter.write(toCsv("" + input.getValue()));
				
			} else if("weighted".equals(input.getAssistType())) { 

				csvWriter.write(toCsv(input.getAssistType()));
				csvWriter.write(toCsv(input.getUnit()));
				csvWriter.write(toCsv("" + input.getValue()));

			}
		}
	}
	
	public String toCsv(String input) {
		if(StringUtils.isNotEmpty(input)) {
			return StringEscapeUtils.escapeCsv(input) + ",";
		} else {
			return ",";
		}
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
	
}
