/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package oracle.lambda.devoxx;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*

java -XX:-TieredCompilation -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*IntStreamSum.*"

java -XX:-TieredCompilation -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*IntStreamSum.seq.*"

java -XX:-TieredCompilation -XX:CompileCommandFile=hotspot_compiler -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*IntStreamSum.seq.*"

java -XX:-TieredCompilation -XX:MaxInlineLevel=11 -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*IntStreamSum.*"

 */

@State
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class IntStreamSum {

    private static int N = Integer.getInteger("benchmark.n", 100000);

    public int[] array;
    public int length = N;

    @Setup
    public void setUp() {
        array = new int[length];
        for(int i = 0; i < length; i++) {
            array[i] = 3 * i;
        }
    }

    @GenerateMicroBenchmark
    public int loop() {
        int r = 0;
        for (int i : array) {
            r += i * 5;
        }
        return r;
    }

    @GenerateMicroBenchmark
    public int parallel() {
        return Arrays.stream(array).parallel().map(e -> e * 5).sum();
    }

    @GenerateMicroBenchmark
    public int sequential() {
        return Arrays.stream(array).map(e -> e * 5).sum();
    }
}
