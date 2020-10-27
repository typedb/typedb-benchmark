package grakn.simulation.benchmark;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;

public class ReportJava {

    public static void main(String[] args) {
        GroovyClassLoader gcl = new GroovyClassLoader();
        Class<?> groovyClass;
        try {
            groovyClass = gcl.parseClass(new File("benchmark/ReportTemplate.groovy"));
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            Object output = groovyObject.invokeMethod("render", null);
            System.out.println(output);
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
