package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoRootGroup {
	
//	wo_name = workout['root_group']['name']
//	wo_notes = workout['root_group']['notes']

	@JsonProperty("name")
	private String name;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("notes")
	private String notes;
	
	@JsonProperty("children")
	private List<DtoFitoWorkoutExercise> children = new ArrayList<>();

	public List<DtoFitoWorkoutExercise> getChildren() {
		return children;
	}

	public void setChildren(List<DtoFitoWorkoutExercise> children) {
		this.children = children;
	}
	
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "DtoFitoRootGroup [children=" + children + "]";
	}


	
	



	

}
