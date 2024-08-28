package com.sociallibparser.singelton;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperSingelton {
    private static ObjectMapperSingelton INSTANCE;
    private final ObjectMapper mapper;

    public ObjectMapperSingelton() {
        mapper = new ObjectMapper();
    }

    public synchronized static ObjectMapperSingelton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapperSingelton();
        }
        return INSTANCE;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
