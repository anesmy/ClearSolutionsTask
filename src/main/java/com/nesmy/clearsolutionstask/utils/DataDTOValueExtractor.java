package com.nesmy.clearsolutionstask.utils;

import com.nesmy.clearsolutionstask.dto.DataDTO;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

public class DataDTOValueExtractor implements ValueExtractor<DataDTO<@ExtractedValue ?>> {

    @Override
    public void extractValues(DataDTO<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
        if (originalValue != null) {
            receiver.value(null, originalValue.getData());
        }
    }
}
