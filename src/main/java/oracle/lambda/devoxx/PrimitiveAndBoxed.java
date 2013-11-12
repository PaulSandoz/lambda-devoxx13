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

java -XX:-TieredCompilation -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*PrimitiveAndBoxed.*"

 */

@State
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PrimitiveAndBoxed {

    private static int N = Integer.getInteger("benchmark.n", 100000);

    public int[] primitiveArray;
    public Integer[] boxedArray;
    public int length = N;

    @Setup
    public void setUp() {
        primitiveArray = new int[length];
        boxedArray = new Integer[length];
        for(int i = 0; i < length; i++) {
            primitiveArray[i] = 3 * i;
            boxedArray[i] = 3 * i;
        }
    }

    @GenerateMicroBenchmark
    public int loopPrimitive() {
        int r = 0;
        for (int i : primitiveArray) {
            r += i * 5;
        }
        return r;
    }

    @GenerateMicroBenchmark
    public Integer loopBoxed() {
        Integer r = 0;
        for (Integer i : boxedArray) {
            r += i * 5;
        }
        return r;
    }

    @GenerateMicroBenchmark
    public int parallelPrimitive() {
        return Arrays.stream(primitiveArray).parallel().map(e -> e * 5).sum();
    }

    @GenerateMicroBenchmark
    public int sequentialPrimitive() {
        return Arrays.stream(primitiveArray).map(e -> e * 5).sum();
    }

    @GenerateMicroBenchmark
    public Integer parallelBoxed() {
        return Arrays.stream(boxedArray).parallel().map(e -> e * 5).reduce(0, Integer::sum);
    }

    @GenerateMicroBenchmark
    public Integer sequentialBoxed() {
        return Arrays.stream(boxedArray).map(e -> e * 5).reduce(0, Integer::sum);
    }
}
