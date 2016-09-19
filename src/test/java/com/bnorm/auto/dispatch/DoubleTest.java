package com.bnorm.auto.dispatch;

import org.junit.Test;

import com.bnorm.auto.dispatch.internal.AutoDispatchProcessor;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubject;

import junit.framework.TestCase;

public class DoubleTest extends TestCase {

    @Test
    public void test() throws Exception {
        JavaSourcesSubject.assertThat(JavaFileObjects.forResource("good/DoubleMethod.java"),
                                      JavaFileObjects.forResource("good/DescriptionMulti.java"),
                                      JavaFileObjects.forResource("good/HeightMulti.java"),
                                      JavaFileObjects.forResource("support/Person.java"),
                                      JavaFileObjects.forResource("support/Height.java"),
                                      JavaFileObjects.forResource("support/Age.java"))
                          .processedWith(new AutoDispatchProcessor())
                          .compilesWithoutError()
                          .and()
                          .generatesSources(JavaFileObjects.forResource("expected/AutoDispatch_DoubleMethod.java"));
    }
}
