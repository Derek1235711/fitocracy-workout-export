package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutExercise {
	
//	exercise = child['exercise']
//	exercise_id = exercise['exercise_id']
//	ex_notes = exercise['notes']
	
	@JsonProperty("exercise")
	private DtoFitoWorkoutExerciseDetail exercise;
	
	@JsonProperty("children")
	private List<DtoFitoWorkoutGroupExercise> children = new ArrayList<>();
	
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("type")
	private String type;
	
	

	public DtoFitoWorkoutExerciseDetail getExercise() {
		return exercise;
	}

	public void setExercise(DtoFitoWorkoutExerciseDetail exercise) {
		this.exercise = exercise;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

	public List<DtoFitoWorkoutGroupExercise> getChildren() {
		return children;
	}

	public void setChildren(List<DtoFitoWorkoutGroupExercise> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutExercise [exercise=" + exercise + ", type=" + type + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	


	
	
	
	

}
