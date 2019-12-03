package fitness.thunderwave.fitocracy.exporter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fitness.thunderwave.fitocracy.exporter.dtos.DtoFitoWorkoutDay;

public class FitocracyApiManager {
	
    final static private String DATE_FORMAT = "yyyy-MM-dd";
    public final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    
    public void getWorkouts(String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date startDate) {
    	
		Date startSearchDate = startDate;
		Date endSearchDate = getEndofDay(new Date());
		Date tmpDate = startSearchDate;


		// doing one day at time sequentially, to keep the load on Fitocracy serves light
		while(tmpDate.getTime() <=  endSearchDate.getTime()) {

			makeApiCall(fitocracyId, sessionId, fitocracyApiBaseUrl, tmpDate);
			
			tmpDate = addDays(tmpDate, 1);
			try {
				// sleep for 1 second, don't want to DOS attack fitocracy
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

		}
    	
    }
	
	
	private void makeApiCall(String fitocracyId, String sessionId, String fitocracyApiBaseUrl, Date date)  {
		
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
			StringBuilder sb = new StringBuilder();
			sb.append("\nGetting Data for " + fitocracyId + " for date " + DATE_FORMATTER.format(date));
			sb.append("\nStatusCode=" + respEntity.getStatusCode());
			
			System.out.println(sb.toString());
			
			ObjectMapper objectMapper = new ObjectMapper();
			String json = respEntity.getBody();
			
			if(json != null && json.length() > 0) {
				try {
					DtoFitoWorkoutDay data = objectMapper.readValue(json, DtoFitoWorkoutDay.class);
					System.out.println(data);
				} catch (JsonProcessingException e) {
					System.err.println(e.getMessage());
				} 
			}
			
			

//			List<TbWorkout> tbWorkouts = workoutManager.convert(vwUser.getUserId(), respEntity.getBody());
//			sb.append("\nNumber of Workouts=" + tbWorkouts.size());
//
//			for (TbWorkout tbWorkout : tbWorkouts) {
//				sb.append("\n   tbWorkout:" + tbWorkout);
//				workoutDao.saveTbWorkout(tbWorkout);
//			}
//			
//			logger.debug(sb.toString());
			
		} catch(HttpClientErrorException e) {
			System.err.println(e.getMessage());
			return;
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
    
    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

}
