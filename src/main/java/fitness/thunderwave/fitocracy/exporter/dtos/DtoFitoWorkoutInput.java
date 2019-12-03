package fitness.thunderwave.fitocracy.exporter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoFitoWorkoutInput {
	
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
	
	@JsonProperty("unit")
	private String unit;
	
	@JsonProperty("value")
	private Double value;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("assist_type")
	private String assistType;

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAssistType() {
		return assistType;
	}

	public void setAssistType(String assistType) {
		this.assistType = assistType;
	}

	@Override
	public String toString() {
		return "DtoFitoWorkoutInput [unit=" + unit + ", value=" + value + ", type=" + type + ", assistType="
				+ assistType + "]";
	}
	
	

}
