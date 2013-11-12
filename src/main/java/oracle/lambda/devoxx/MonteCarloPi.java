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
import org.openjdk.jmh.annotations.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/*

java -XX:-TieredCompilation -jar target/microbenchmarks.jar -wi 5 -w 50ms -r 50ms -i 20 -f 1 ".*MonteCarloPi.*"

 */

@State
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MonteCarloPi {

    private static long N = Long.getLong("benchmark.n", 1L << 24);

    private static double R = 1.0;

    static final class SplittableRandoms {

        private static final class SplittableRandomSpliterator implements Spliterator<SplittableRandom> {

            final SplittableRandom rng;
            long index;
            final long fence;

            SplittableRandomSpliterator(SplittableRandom rng, long index, long fence) {
                this.rng = rng;
                this.index = index;
                this.fence = fence;
            }

            public SplittableRandomSpliterator trySplit() {
                long i = index, m = (i + fence) >>> 1;
                return (m <= i) ? null :
                       new SplittableRandomSpliterator(rng.split(), i, index = m);
            }

            public long estimateSize() {
                return fence - index;
            }

            public int characteristics() {
                return (Spliterator.SIZED | Spliterator.SUBSIZED |
                        Spliterator.NONNULL | Spliterator.IMMUTABLE);
            }

            public boolean tryAdvance(Consumer<? super SplittableRandom> consumer) {
                if (consumer == null) throw new NullPointerException();
                long i = index, f = fence;
                if (i < f) {
                    consumer.accept(rng);
                    index = i + 1;
                    return true;
                }
                return false;
            }

            public void forEachRemaining(Consumer<? super SplittableRandom> consumer) {
                if (consumer == null) throw new NullPointerException();
                long i = index, f = fence;
                if (i < f) {
                    index = f;
                    SplittableRandom r = rng;
                    do {
                        consumer.accept(r);
                    }
                    while (++i < f);
                }
            }
        }

        public static Stream<SplittableRandom> generators() {
            return generators(new SplittableRandom(), Long.MAX_VALUE);
        }

        public static Stream<SplittableRandom> generators(long streamSize) {
            return generators(new SplittableRandom(), streamSize);
        }

        public static Stream<SplittableRandom> generators(SplittableRandom sr, long streamSize) {
            if (streamSize < 0L)
                throw new IllegalArgumentException("size must be non-negative");
            return StreamSupport.stream(new SplittableRandomSpliterator(sr, 0L, streamSize), false);
        }
    }

    /*
       Area of square = 4 * r^2
       Area of circle = pi * r^2
       N / M = 4 / pi
       pi = 4 * M / N
     */

    @GenerateMicroBenchmark
    public double loop() {
        long m = 0L;
        SplittableRandom sr = new SplittableRandom();
        for (long i = 0; i < N; i++) {
            double x = sr.nextDouble(-1, 1);
            double y = sr.nextDouble(-1, 1);

            if (x * x + y * y < R * R) {
                m++;
            }
        }

        double pi = (4.0 * m) / N;
        return pi;
    }

    @GenerateMicroBenchmark
    public double parallel() {
        long m = SplittableRandoms.generators(N)
                .parallel()
                .filter(sr -> {
                    double x = sr.nextDouble(-1, 1);
                    double y = sr.nextDouble(-1, 1);

                    return x * x + y * y < R * R;
                })
                .count();

        double pi = (4.0 * m) / N;
        return pi;
    }

    @GenerateMicroBenchmark
    public double sequential() {
        long m = SplittableRandoms.generators(N)
                .filter(sr -> {
                    double x = sr.nextDouble(-1, 1);
                    double y = sr.nextDouble(-1, 1);

                    return x * x + y * y < R * R;
                })
                .count();

        double pi = (4.0 * m) / N;
        return pi;
    }
}