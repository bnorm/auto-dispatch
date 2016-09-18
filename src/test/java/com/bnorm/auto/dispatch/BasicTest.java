package com.bnorm.auto.dispatch;

import org.junit.Test;

import com.bnorm.auto.dispatch.internal.AutoDispatchProcessor;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubject;

import junit.framework.TestCase;

public class BasicTest extends TestCase {

    @Test
    public void test() throws Exception {
        JavaSourcesSubject.assertThat(JavaFileObjects.forResource("good/BasicMethod.java"),
                                      JavaFileObjects.forResource("good/DescriptionMulti.java"),
                                      JavaFileObjects.forResource("support/Person.java"),
                                      JavaFileObjects.forResource("support/Age.java"))
                          .processedWith(new AutoDispatchProcessor())
                          .compilesWithoutError();
        //                          .and()
        //                          .generatesSources(JavaFileObjects.forResource("expected/AutoDispatch_BasicMethod.java"));
    }
}
