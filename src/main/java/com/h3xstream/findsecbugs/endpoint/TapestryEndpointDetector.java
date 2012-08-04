package com.h3xstream.findsecbugs.endpoint;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.ba.ClassContext;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;

/**
 * Identify endpoints using the web framework Tapestry.
 * <p>
 * <a href="http://tapestry.apache.org/">Official Website</a>
 */
public class TapestryEndpointDetector implements Detector {

    private static final String TAPESTRY_ENDPOINT_TYPE = "TAPESTRY_ENDPOINT";

    private BugReporter bugReporter;

    public TapestryEndpointDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visitClassContext(ClassContext classContext) {

        JavaClass javaClass = classContext.getJavaClass();

        if(!javaClass.getPackageName().contains(".pages")) {
            return;
        }

        //The following some false positive
        if(javaClass.getPackageName().endsWith(".pages")) {
            bugReporter.reportBug(new BugInstance(this, TAPESTRY_ENDPOINT_TYPE, LOW_PRIORITY) //
                    .addClass(javaClass));
        }

        //The package contains ".pages" and has some references to tapestry
        // then it must be an endpoint.
        //The constants pool contains all references that are reused in the bytecode
        // including full class name and interface name.
        if(javaClass.getPackageName().contains(".pages")) {
            ConstantPool constants = javaClass.getConstantPool();
            for(Constant c : constants.getConstantPool()) {
                if(c instanceof ConstantUtf8) {
                    ConstantUtf8 utf8 = (ConstantUtf8) c;
                    String constantValue = new String(utf8.getBytes());
                    if(constantValue.startsWith("Lorg/apache/tapestry5/annotations")) {
                        bugReporter.reportBug(new BugInstance(this, TAPESTRY_ENDPOINT_TYPE, LOW_PRIORITY) //
                                .addClass(javaClass));
                        return;
                    }
                }
            }
        }

    }

    @Override
    public void report() {

    }

}