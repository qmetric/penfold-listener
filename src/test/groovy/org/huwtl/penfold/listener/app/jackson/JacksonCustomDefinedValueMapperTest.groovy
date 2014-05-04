package org.huwtl.penfold.listener.app.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.reflect.TypeToken
import spock.lang.Specification

class JacksonCustomDefinedValueMapperTest extends Specification {
    final customDefinedValueMapper = new JacksonCustomDefinedValueMapper(new ObjectMapper())

    def "should deserialize payload for event using custom specific object mapper"() {
        expect:
        final json = """{"stuff" : "something", "nested" : {"inner" : true}}"""
        customDefinedValueMapper.map(json, TypeToken.of(Map.class)) == [nested: [inner: true], stuff: "something"]
    }
}
