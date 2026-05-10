package com.bytedance.android.aabresguard.debug;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ProtoApkInspectorTest {

    @Test
    public void inspectProtoApkFromProperty() throws Exception {
        String protoApkPath = System.getProperty("protoApkPath");
        if (protoApkPath == null || protoApkPath.trim().isEmpty()) {
            protoApkPath = System.getenv("PROTO_APK_PATH");
        }
        assertTrue("Missing -DprotoApkPath", protoApkPath != null && !protoApkPath.trim().isEmpty());

        List<String> issues = ProtoApkInspector.inspect(Paths.get(protoApkPath));
        if (!issues.isEmpty()) {
            System.out.println("Proto issues in " + protoApkPath + ":");
            issues.forEach(System.out::println);
        }
        assertTrue("Proto issues found: " + issues.size(), issues.isEmpty());
    }
}
