package fitness.thunderwave.fitocracy.exporter.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutSet {
	
	@JsonProperty("inputs")
	private List<DtoFitoWorkoutInput> inputs = new ArrayList<>();
	
	// points
	@JsonProperty("points")
	private Long points;

	public List<DtoFitoWorkoutInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<DtoFitoWorkoutInput> inputs) {
		this.inputs = inputs;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutSet [inputs=" + inputs + "]";
	}

	public Long getPoints() {
		return points;
	}

	public void setPoints(Long points) {
		this.points = points;
	}
	
	
	
//	# inputs
//	for input in sets['inputs']:
//		if input['type'] == 'distance':
//			set_details.distance_unit = input['unit']
//			set_details.distance_value = input['value']
//		elif input['type'] == 'time':
//			set_details.time_unit = input['unit']
//			set_details.time_value = input['value']
//		elif input['type'] == 'reps':
//			# set_details.is_reps = True
//			set_details.reps_value = input['value'] # assumes exercise as the unit
//		elif input['type'] == 'weight':
//			set_details.weights_unit = input['unit'] 
//			set_details.weights_value = input['value'] 
//		elif 'assist_type' in input and input['assist_type'] == 'assisted':
//			set_details.modifier_type = input['type'] 
//			set_details.modifier_unit = input['unit'] 
//			set_details.modifier_value = input['value'] 
	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;
//	
//	@JsonProperty("exercise")
//	private String exercise;

}
