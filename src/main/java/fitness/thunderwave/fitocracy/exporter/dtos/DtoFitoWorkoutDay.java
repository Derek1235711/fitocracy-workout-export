package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutDay {
	
	private List<DtoFitoWorkout> data = new ArrayList<>();
	
	private String error;

	public List<DtoFitoWorkout> getData() {
		return data;
	}

	public void setData(List<DtoFitoWorkout> data) {
		this.data = data;
	}
	
	

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutDay [data=" + data + "]";
	}




	
	

}
