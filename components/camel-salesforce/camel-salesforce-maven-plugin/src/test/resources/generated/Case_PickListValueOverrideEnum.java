/*
 * Salesforce DTO generated by camel-salesforce-maven-plugin
 */
package $packageName;

import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Salesforce Enumeration DTO for picklist PickListValueOverride
 */
@Generated("org.apache.camel.maven.CamelSalesforceMojo")
public enum Case_PickListValueOverrideEnum {

    // A+
    APlus("A+");

    final String value;

    private Case_PickListValueOverrideEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Case_PickListValueOverrideEnum fromValue(String value) {
        for (Case_PickListValueOverrideEnum e : Case_PickListValueOverrideEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
