package com.github.zavier.jsonpath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JacksonConverter {

    private static final Logger logger = LoggerFactory.getLogger(JacksonConverter.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode toJson(JsonNode node, String jsonStr) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            final Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                final Map.Entry<String, JsonNode> field = fields.next();
                final String fieldName = field.getKey();
                final JsonNode jsonNode = field.getValue();
                final JsonNode result = toJson(jsonNode, jsonStr);
                objectNode.set(fieldName, result);
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                final JsonNode jsonNode = arrayNode.get(i);
                final JsonNode result = toJson(jsonNode, jsonStr);
                arrayNode.set(i, result);
            }
        } else {
            if (node.isTextual()) {
                // jsonPath处理
                final String strData = node.asText();
                if (strData.startsWith("$")) {
                    final Object read;
                    try {
                        read = JsonPath.read(jsonStr, strData);
                        return objectMapper.readTree(objectMapper.writeValueAsString(read));
                    } catch (Exception e) {
                        logger.warn("jsonpath获取失败", e);
                        return null;
                    }
                }
            }
        }

        return node;
    }

    public void generateJavaFile(String json) {
        try {
            convertJsonToJavaClass(json, new File("."), "com.github.zavier", "DemoJson");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void convertJsonToJavaClass(String json, File outputJavaClassDirectory, String packageName, String javaClassName)
            throws IOException {
        JCodeModel jcodeModel = new JCodeModel();

        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() {
                return true;
            }

            @Override
            public SourceType getSourceType() {
                return SourceType.JSON;
            }
        };

        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
        mapper.generate(jcodeModel, javaClassName, packageName, json);

        jcodeModel.build(outputJavaClassDirectory);
    }
}
