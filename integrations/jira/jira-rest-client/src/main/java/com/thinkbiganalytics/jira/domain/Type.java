
package com.thinkbiganalytics.jira.domain;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "name",
    "inward",
    "outward",
    "self"
})
public class Type {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("inward")
    private String inward;
    @JsonProperty("outward")
    private String outward;
    @JsonProperty("self")
    private String self;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The inward
     */
    @JsonProperty("inward")
    public String getInward() {
        return inward;
    }

    /**
     * 
     * @param inward
     *     The inward
     */
    @JsonProperty("inward")
    public void setInward(String inward) {
        this.inward = inward;
    }

    /**
     * 
     * @return
     *     The outward
     */
    @JsonProperty("outward")
    public String getOutward() {
        return outward;
    }

    /**
     * 
     * @param outward
     *     The outward
     */
    @JsonProperty("outward")
    public void setOutward(String outward) {
        this.outward = outward;
    }

    /**
     * 
     * @return
     *     The self
     */
    @JsonProperty("self")
    public String getSelf() {
        return self;
    }

    /**
     * 
     * @param self
     *     The self
     */
    @JsonProperty("self")
    public void setSelf(String self) {
        this.self = self;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}