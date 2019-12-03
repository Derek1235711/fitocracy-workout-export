package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutDay {
	
	private List<DtoFitoWorkout> data = new ArrayList<>();

	public List<DtoFitoWorkout> getData() {
		return data;
	}

	public void setData(List<DtoFitoWorkout> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutDay [data=" + data + "]";
	}




	
	

}
