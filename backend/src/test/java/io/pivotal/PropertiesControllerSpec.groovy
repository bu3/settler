package io.pivotal

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Ignore
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PropertiesControllerSpec extends Specification {

    MockMvc mockMvc
    PropertiesController propertiesController

    void setup() {
        propertiesController = new PropertiesController(Mock(PropertyProvider))
        mockMvc = MockMvcBuilders.standaloneSetup(propertiesController).build()
    }

    def "should return properties"() {
        when:
        def response = mockMvc.perform(get("/properties"))

        then:
        1 * propertiesController.propertyProvider.find() >> {
            [new Property(id: 1, address: "Address 1"), new Property(id: 2, address: "Address 2")]
        }

        response.andExpect(status().isOk()).andExpect(jsonPath('$', hasSize(2))).andExpect(jsonPath('$[0].address', equalTo("Address 1")))
    }

    def "should return a single property"() {

        when:
        def response = mockMvc.perform(get("/properties/1"))

        then:
        1 * propertiesController.propertyProvider.findOne(1L) >> {
            new Property(id: 1, address: "Address 1")
        }

        response.andExpect(status().isOk()).andExpect(jsonPath('$.id', equalTo(1))).andExpect(jsonPath('$.address', equalTo('Address 1')))
    }

    def "should return an error when property does not exist"() {
        when:
        def response = mockMvc.perform(get("/properties/500"))

        then:
        1 * propertiesController.propertyProvider.findOne(500) >> {
            null
        }

        response.andExpect(status().isNotFound())
    }

    def "should create a new property"() {

        given:
        def content = '{"address": "Address 2"}';

        when:
        def response = mockMvc.perform(post("/properties").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))

        then:
        1 * propertiesController.propertyProvider.save(_ as Property) >> { Property input  ->
            input
        }

        response.andExpect(status().isCreated()).andExpect(jsonPath('$.address', equalTo('Address 2')))

    }


    def "should fail if trying to provide an id"() {

        given:
        def content = '{"id": 1,  "address": "Test address"}';

        when:
        def response = mockMvc.perform(post("/properties").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))

        then:
        0 * propertiesController.propertyProvider.save(_)

        response.andExpect(status().isBadRequest())


    }

}