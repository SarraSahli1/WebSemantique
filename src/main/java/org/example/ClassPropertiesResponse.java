package org.example;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.PropertyValue;

import java.util.List;

@Getter
@Setter
public class ClassPropertiesResponse {
    private String className;
    private List<String> properties;

    public ClassPropertiesResponse(String className, List<String> properties) {
        this.className = className;
        this.properties = properties;
    }

}

