/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class WakelockDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WakelockDetector();
    }

    public void test1() throws Exception {
        assertEquals(
            "WakelockActivity1.java:15: Warning: Found a wakelock acquire() but no release() calls anywhere",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity1.java.txt=>src/test/pkg/WakelockActivity1.java",
                "bytecode/WakelockActivity1.class.data=>bin/classes/test/pkg/WakelockActivity1.class"
                ));
    }

    public void test2() throws Exception {
        assertEquals(
            "WakelockActivity2.java:13: Warning: Wakelocks should be released in onPause, not onDestroy",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity2.java.txt=>src/test/pkg/WakelockActivity2.java",
                "bytecode/WakelockActivity2.class.data=>bin/classes/test/pkg/WakelockActivity2.class"
                ));
    }

    public void test3() throws Exception {
        assertEquals(
            "WakelockActivity3.java:13: Warning: The release() call is not always reached",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity3.java.txt=>src/test/pkg/WakelockActivity3.java",
                "bytecode/WakelockActivity3.class.data=>bin/classes/test/pkg/WakelockActivity3.class"
                ));
    }

    public void test4() throws Exception {
        assertEquals(
            "WakelockActivity4.java:10: Warning: The release() call is not always reached",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity4.java.txt=>src/test/pkg/WakelockActivity4.java",
                "bytecode/WakelockActivity4.class.data=>bin/classes/test/pkg/WakelockActivity4.class"
                ));
    }

    public void test5() throws Exception {
        assertEquals(
            // Missing 13
            "WakelockActivity5.java:13: Warning: The release() call is not always reached",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity5.java.txt=>src/test/pkg/WakelockActivity5.java",
                "bytecode/WakelockActivity5.class.data=>bin/classes/test/pkg/WakelockActivity5.class"
                ));
    }

    public void test6() throws Exception {
        assertEquals(
            "WakelockActivity6.java:19: Warning: The release() call is not always reached\n" +
            "WakelockActivity6.java:28: Warning: The release() call is not always reached\n" +
            "WakelockActivity6.java:65: Warning: The release() call is not always reached",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "res/layout/onclick.xml=>res/layout/onclick.xml",
                "bytecode/WakelockActivity6.java.txt=>src/test/pkg/WakelockActivity6.java",
                "bytecode/WakelockActivity6.class.data=>bin/classes/test/pkg/WakelockActivity6.class"
                ));
    }
}
