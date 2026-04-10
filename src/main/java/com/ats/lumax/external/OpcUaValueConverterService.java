package com.ats.lumax.external;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "kafka", path = "/api/opcUaValueConverter")
public interface OpcUaValueConverterService {

	@PostMapping("/convertValue")
	public Object convertValue(Variant variant);

	@PostMapping("/convertDataValue")
	public DataValue convertDataValue(DataValue originalValue);

}
