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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/*

java -XX:-TieredCompilation -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*ProbablePrimes.*"

 */

@State
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ProbablePrimes {

    private static int N = Integer.getInteger("benchmark.n", 100);

    private static int BIT_LENGTH = Integer.getInteger("benchmark.bitLength", 128);

    @GenerateMicroBenchmark
    public List<BigInteger> loop() {
        List<BigInteger> pps = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            BigInteger pp = BigInteger.probablePrime(BIT_LENGTH, ThreadLocalRandom.current());
            pps.add(pp);
        }
        return pps;
    }

    @GenerateMicroBenchmark
    public List<BigInteger> parallelRange() {
        return IntStream.range(0, N)
                .parallel()
                .mapToObj(i -> BigInteger.probablePrime(BIT_LENGTH, ThreadLocalRandom.current()))
                .collect(toList());
    }

    @GenerateMicroBenchmark
    public List<BigInteger> sequentialRange() {
        return IntStream.range(0, N)
                .mapToObj(i -> BigInteger.probablePrime(BIT_LENGTH, ThreadLocalRandom.current()))
                .collect(toList());
    }

    @GenerateMicroBenchmark
    public List<BigInteger> parallelGenerate() {
        return Stream.generate(() -> BigInteger.probablePrime(BIT_LENGTH, ThreadLocalRandom.current()))
                .parallel()
                .limit(N)
                .collect(toList());
    }

    @GenerateMicroBenchmark
    public List<BigInteger> sequentialGenerate() {
        return Stream.generate(() -> BigInteger.probablePrime(BIT_LENGTH, ThreadLocalRandom.current()))
                .limit(N)
                .collect(toList());
    }

}
