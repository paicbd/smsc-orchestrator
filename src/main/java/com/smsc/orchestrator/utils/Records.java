package com.smsc.orchestrator.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import jakarta.annotation.Nonnull;

import java.util.List;

public class Records {

    public record DlrRequest(
            @Nonnull @JsonProperty("message_id") String messageId,
            @JsonProperty("source_addr_ton") int sourceAddrTon,
            @JsonProperty("source_addr_npi") int sourceAddrNpi,
            @JsonProperty("source_addr") String sourceAddr,
            @JsonProperty("dest_addr_ton") int destAddrTon,
            @JsonProperty("dest_addr_npi") int destAddrNpi,
            @JsonProperty("destination_addr") String destinationAddr,
            @JsonProperty("status") String status,
            @JsonProperty("error_code") String errorCode,
            @JsonProperty("optional_parameters") List<UtilsRecords.OptionalParameter> optionalParameters
    ) {
        @Override
        public String toString() {
            return Converter.valueAsString(this);
        }
    }
}
