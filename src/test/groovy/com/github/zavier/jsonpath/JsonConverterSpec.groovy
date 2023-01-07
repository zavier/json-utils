package com.github.zavier.jsonpath

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import spock.lang.Specification

import java.lang.reflect.Type

class JsonConverterSpec extends Specification {


    def "ToJsonUseGson"() {
        given:
        Type typeOfT = new TypeToken<Map<String, Object>>() {
        }.getType();
        def data = new Gson().fromJson(srcData, typeOfT)
        when:
        def json = new GsonConverter().toJson(JsonParser.parseString(path), data)
        then:
        json == JsonParser.parseString(result.toString())
        where:
        srcData                     | path                  | result
        paramMap()[1]['srcDataStr'] | paramMap()[1]['path'] | paramMap()[1]['result']
    }

    def "ToJsonUseJackson"() {
        given:
        def objectMapper = new ObjectMapper()
        when:
        def json = new JacksonConverter().toJson(objectMapper.readTree(path), srcData)
        then:
        json.toString() == result
        where:
        srcData                     | path                  | result
        paramMap()[1]['srcDataStr'] | paramMap()[1]['path'] | paramMap()[1]['result']
    }


    def paramMap() {
        def map = [:]
        map[1] = [:]
        map[1]['srcDataStr'] = """{"user":{"age":10},"name":"zhangsan","aa":"b","address":["address1", "address2"]}"""
        map[1]['path'] = """{"name":"\$.name", "age":"\$.user.age", "sex":"男"}"""
        map[1]['result'] = """{"name":"zhangsan","age":10,"sex":"男"}"""


        return map
    }

}
