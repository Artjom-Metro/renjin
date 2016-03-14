package org.renjin.methods;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MethodsTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        session = new SessionBuilder().build();
        eval("library(methods)");
    }

    private SEXP eval(String source) throws IOException {
        ExpressionVector sexp = RParser.parseAllSource(new StringReader(source + "\n"));
        try {
            return session.getTopLevelContext().evaluate(sexp);
        } catch (EvalException e) {
            e.printRStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void hadley() throws IOException {
        eval("setClass('Person', representation(name = 'character', age = 'numeric'))");
        eval("setClass('Employee', representation(boss = 'Person'), contains = 'Person')");
        eval("hadley <- new(\"Person\", name = \"Hadley\", age = 31)");
        
        eval("print(hadley@age)");
    }
    
    @Test
    public void failure() throws IOException {
        eval("setClass('M', contains = 'matrix', representation(fuzz = 'numeric'))");
        eval("m <- new('M', 1:12, ncol = 3, fuzz = c(1.2,3.2,3.3))");
        eval("m2 <- as(m, 'matrix')");
        eval("print(m2)");
        
        IntVector dim = (IntVector) eval("dim(m2)");
        
        assertThat(dim.getElementAsInt(0), equalTo(12));
        assertThat(dim.getElementAsInt(1), equalTo(1));
    }
    
    @Test
    public void overloadDollar() throws IOException {
        eval("setClass('A', representation('numeric'))");
        eval("setMethod('$', 'A', function(x, name) name)");
        eval("a <- new('A')");
        
        assertThat(eval("a$foo"), equalTo((SEXP)new StringArrayVector("foo")));
    }
}
