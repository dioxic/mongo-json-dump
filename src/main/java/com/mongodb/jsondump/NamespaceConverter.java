package com.mongodb.jsondump;

import com.mongodb.MongoNamespace;
import picocli.CommandLine.ITypeConverter;

class NamespaceConverter implements ITypeConverter<MongoNamespace> {
    public MongoNamespace convert(String value) throws Exception {
        return new MongoNamespace(value);
    }
}