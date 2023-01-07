package com.github.zavier.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import spock.lang.Specification

import java.lang.reflect.Type

class GsonConverterSpec extends Specification {

    def gsonConverter = new GsonConverter()

    def "ToJson"() {
        given:
        Type typeOfT = new TypeToken<Map<String, Object>>() {
        }.getType();
        def data = new Gson().fromJson(srcData, typeOfT)
        when:
        def json = gsonConverter.toJson(JsonParser.parseString(path), data)
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
        map[1]['result'] = """{"name":"zhangsan","age":10.0,"sex":"男"}"""


        return map
    }

}
