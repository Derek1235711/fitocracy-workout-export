package fitness.thunderwave.fitocracy.exporter.dtos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkout {
	
	public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public final static SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT);
	
//	wo_name = workout['root_group']['name']
//	wo_notes = workout['root_group']['notes']
//
//	wo_updated_ts_str = workout['updated_timestamp']
//	wo_ts_str = workout['workout_timestamp']
//
//	wo_updated_ts = datetime.strptime(wo_updated_ts_str, "%Y-%m-%dT%H:%M:%S")
//	wo_ts = datetime.strptime(wo_ts_str, "%Y-%m-%dT%H:%M:%S")
	
	@JsonProperty("updated_timestamp")
	private String updatedTimestampStr;
	
	@JsonProperty("workout_timestamp")
	private String workoutTimestampStr;
	
	@JsonProperty("root_group")
	private DtoFitoRootGroup rootGroup = new DtoFitoRootGroup();

	public DtoFitoRootGroup getRootGroup() {
		return rootGroup;
	}

	public void setRootGroup(DtoFitoRootGroup rootGroup) {
		this.rootGroup = rootGroup;
	}
	
	public Date getUpdatedTimestamp() {
		if(updatedTimestampStr != null) {
			try {
				return DATE_TIME_FORMATTER.parse(updatedTimestampStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getUpdatedTimestampStr() {
		return updatedTimestampStr;
	}

	public void setUpdatedTimestampStr(String updatedTimestampStr) {
		this.updatedTimestampStr = updatedTimestampStr;
	}
	
	public Date getWorkoutTimestamp() {
		if(workoutTimestampStr != null) {
			try {
				return DATE_TIME_FORMATTER.parse(workoutTimestampStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public String getWorkoutTimestampStr() {
		return workoutTimestampStr;
	}

	public void setWorkoutTimestampStr(String workoutTimestampStr) {
		this.workoutTimestampStr = workoutTimestampStr;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkout [rootGroup=" + rootGroup + "]";
	}
	

	


}
