package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutExerciseDetail {
	
//	exercise = child['exercise']
//	exercise_id = exercise['exercise_id']
//	ex_notes = exercise['notes']
	
	@JsonProperty("exercise_id")
	private Long exerciseId;
	
	@JsonProperty("name")
	private String name;

	@JsonProperty("notes")
	private String notes;
	
	private List<DtoFitoWorkoutSet> sets = new ArrayList<>();

	public Long getExerciseId() {
		return exerciseId;
	}

	public void setExerciseId(Long exerciseId) {
		this.exerciseId = exerciseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<DtoFitoWorkoutSet> getSets() {
		return sets;
	}

	public void setSets(List<DtoFitoWorkoutSet> sets) {
		this.sets = sets;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutExercise [exerciseId=" + exerciseId + ", name=" + name + ", notes=" + notes
				+ ", sets=" + sets + "]";
	}
	
	
	
	

}
