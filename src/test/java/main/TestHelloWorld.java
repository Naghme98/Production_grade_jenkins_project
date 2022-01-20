package main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHelloWorld {

    @Test
    void myMethod() throws Exception {
        HelloWorld obj = new HelloWorld();
        String s = obj.myMethod();
        assertEquals(HelloWorld.TEXT, s);
    }
}
