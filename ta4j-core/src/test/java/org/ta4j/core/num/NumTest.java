/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.num;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.TestUtils.assertNumNotEquals;
import static org.ta4j.core.num.NaN.NaN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Properties;

import org.junit.Test;
import org.ta4j.core.indicators.AbstractIndicatorTest;

public class NumTest extends AbstractIndicatorTest<Object, Num> {

    public static final int HIGH_PRECISION = 128;

    public NumTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Test
    public void testZero() {
        var anyNaNNum = NaN;
        var anyDecimalNum = DecimalNum.valueOf(3);
        var anyDoubleNum = DoubleNum.valueOf(3);
        assertNumEquals(NaN, anyNaNNum.getNumFactory().zero());
        assertNumEquals(0, numOf(3).getNumFactory().zero());
        assertNumEquals(0, anyDecimalNum.getNumFactory().zero());
        assertNumEquals(0, anyDoubleNum.getNumFactory().zero());
    }

    @Test
    public void testOne() {
        var anyNaNNum = NaN;
        var anyDecimalNum = DecimalNum.valueOf(3);
        var anyDoubleNum = DoubleNum.valueOf(3);
        assertNumEquals(NaN, anyNaNNum.getNumFactory().one());
        assertNumEquals(1, numOf(3).getNumFactory().one());
        assertNumEquals(1, anyDecimalNum.getNumFactory().one());
        assertNumEquals(1, anyDoubleNum.getNumFactory().one());
    }

    @Test
    public void testHundred() {
        var anyNaNNum = NaN;
        var anyDecimalNum = DecimalNum.valueOf(3);
        var anyDoubleNum = DoubleNum.valueOf(3);
        assertNumEquals(NaN, anyNaNNum.getNumFactory().hundred());
        assertNumEquals(100, numOf(3).getNumFactory().hundred());
        assertNumEquals(100, anyDecimalNum.getNumFactory().hundred());
        assertNumEquals(100, anyDoubleNum.getNumFactory().hundred());
    }

    @Test(expected = AssertionError.class)
    public void testStringNumFail() {
        assertNumEquals("1.234", numOf(4.321));
    }

    @Test
    public void testStringNumPass() {
        assertNumEquals("1.234", numOf(1.234));
    }

    @Test
    public void testDecimalNumPrecision() {
        var highPrecisionString = "1.928749238479283749238472398472936872364823749823749238749238749283749238472983749238749832749274";
        var num = numOf(highPrecisionString, HIGH_PRECISION);
        var highPrecisionNum = DecimalNum.valueOf(highPrecisionString, HIGH_PRECISION);
        assertTrue(((DecimalNum) highPrecisionNum).matches(num, 17));
        var fromNum = new BigDecimal(num.toString());
        if (num.getClass().equals(DoubleNum.class)) {
            assertEquals(17, fromNum.precision());
            assertTrue(((DecimalNum) highPrecisionNum).matches(num, 17));
            assertFalse(((DecimalNum) highPrecisionNum).matches(num, 18));
        }
        if (num.getClass().equals(DecimalNum.class)) {
            assertEquals(97, fromNum.precision());
            // since precisions are the same, will match to any precision
            assertTrue(((DecimalNum) highPrecisionNum).matches(num, 10000));
        }
    }

    private Num numOf(String string, int precision) {
        return DecimalNumFactory.getInstance(precision).numOf(string);
    }

    @Test
    public void testDecimalNumOffset() {
        var highPrecisionString = "1.928749238479283749238472398472936872364823749823749238749238749283749238472983749238749832749274";
        var num = numOf(highPrecisionString, HIGH_PRECISION);
        // upconvert num to PrecisionNum so that we don't throw ClassCastException in
        // minus() from
        // PrecisionNum.matches()
        var lowerPrecisionNum = DecimalNum.valueOf(num.toString(), 128);
        var highPrecisionNum = DecimalNum.valueOf(highPrecisionString, 128);
        // use HIGH_PRECISION PrecisionNums for delta because they are so small
        assertTrue(((DecimalNum) highPrecisionNum).matches(lowerPrecisionNum,
                highPrecisionNum.getNumFactory().numOf("0.0000000000000001")));
        if (num.getClass().equals(DoubleNum.class)) {
            assertTrue(((DecimalNum) highPrecisionNum).matches(lowerPrecisionNum,
                    highPrecisionNum.getNumFactory().numOf("0.0000000000000001")));
            assertFalse(((DecimalNum) highPrecisionNum).matches(lowerPrecisionNum,
                    highPrecisionNum.getNumFactory().numOf("0.00000000000000001")));
        }
        if (num.getClass().equals(DecimalNum.class)) {
            // since precisions are the same, will match to any precision
            assertTrue(((DecimalNum) highPrecisionNum).matches(lowerPrecisionNum, highPrecisionNum.getNumFactory()
                    .numOf("0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000001")));
        }
    }

    @Test
    public void testValueOf() {
        assertNumEquals(0.33333333333333333332, numOf(0.33333333333333333332));
        assertNumEquals(1, numOf(1d));
        assertNumEquals(2.54, numOf(new BigDecimal("2.54")));

        assertNumEquals(0.33, numOf(0.33));
        assertNumEquals(1, numOf(1));
        assertNumEquals(2.54, numOf(new BigDecimal(2.54)));
    }

    @Test
    public void testMultiplicationSymmetrically() {
        var decimalFromString = numOf(new BigDecimal("0.33"));
        var decimalFromDouble = numOf(45.33);
        assertEquals(decimalFromString.multipliedBy(decimalFromDouble),
                decimalFromDouble.multipliedBy(decimalFromString));

        var doubleNumFromString = numOf(new BigDecimal("0.33"));
        var doubleNumFromDouble = numOf(10.33);
        assertNumEquals(doubleNumFromString.multipliedBy(doubleNumFromDouble),
                doubleNumFromDouble.multipliedBy(doubleNumFromString));
    }

    @Test(expected = java.lang.ClassCastException.class)
    public void testFailDifferentNumsAdd() {
        var a = DecimalNum.valueOf(12);
        var b = DoubleNum.valueOf(12);
        a.plus(b);
    }

    @Test(expected = java.lang.ClassCastException.class)
    public void testFailDifferentNumsCompare() {
        var a = DecimalNum.valueOf(12);
        var b = DoubleNum.valueOf(13);
        a.isEqual(b);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailNaNtoInt() {
        NaN.intValue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailNaNtoLong() {
        NaN.longValue();
    }

    @Test
    public void testNaN() {
        var a = NaN;
        var eleven = DecimalNum.valueOf(11);

        var mustBeNaN = a.plus(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.minus(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.dividedBy(a);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.multipliedBy(NaN);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.max(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = eleven.min(a);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.pow(12);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.pow(a);
        assertNumEquals(mustBeNaN, NaN);

        var nanDouble = a.doubleValue();
        assertEquals(Double.NaN, nanDouble);

        var nanFloat = a.floatValue();
        assertEquals(Float.NaN, nanFloat);

        assertTrue(NaN.equals(a)); // NaN == NaN -> true

    }

    @Test
    public void testArithmetic() {
        var ten = numOf(10);
        var million = numOf(1000000);
        assertNumEquals(10, ten);
        assertNumEquals("1000000.0", million);

        var zero = ten.minus(ten);
        assertNumEquals(0, zero);

        var hundred = ten.multipliedBy(ten);
        assertNumEquals(100, hundred);

        var hundredMillion = hundred.multipliedBy(million);
        assertNumEquals(100000000, hundredMillion);

        assertNumEquals(hundredMillion.dividedBy(hundred), million);
        assertNumEquals(0, hundredMillion.remainder(hundred));

        var five = ten.getNumFactory().numOf(5); // generate new value with NumFunction
        var zeroDotTwo = ten.getNumFactory().numOf(0.2); // generate new value with NumFunction
        var fiveHundred54 = ten.getNumFactory().numOf(554); // generate new value with NumFunction
        assertNumEquals(0, hundredMillion.remainder(five));

        assertNumEquals(0.00032, zeroDotTwo.pow(5));
        assertNumEquals(0.7247796636776955, zeroDotTwo.pow(zeroDotTwo));
        assertNumEquals(1.37972966146, zeroDotTwo.pow(numOf(-0.2)));
        assertNumEquals(554, fiveHundred54.max(five));
        assertNumEquals(5, fiveHundred54.min(five));
        assertTrue(fiveHundred54.isGreaterThan(five));
        assertFalse(five.isGreaterThan(five.getNumFactory().numOf(5)));
        assertFalse(five.isGreaterThanOrEqual(fiveHundred54));
        assertFalse(five.isGreaterThanOrEqual(five.getNumFactory().numOf(6)));
        assertTrue(five.isGreaterThanOrEqual(five.getNumFactory().numOf(5)));

        assertTrue(five.equals(five.getNumFactory().numOf(5)));
        assertTrue(five.equals(five.getNumFactory().numOf(5.0)));
        assertTrue(five.equals(five.getNumFactory().numOf((float) 5)));
        assertTrue(five.equals(five.getNumFactory().numOf((short) 5)));

        assertFalse(five.equals(five.getNumFactory().numOf(4.9)));
        assertFalse(five.equals(five.getNumFactory().numOf(6)));
        assertFalse(five.equals(five.getNumFactory().numOf((float) 15)));
        assertFalse(five.equals(five.getNumFactory().numOf((short) 45)));
    }

    @Test
    public void sqrtOfBigInteger() {
        var sqrtOfTwo = "1.4142135623730950488016887242096980785696718753769480731"
                + "766797379907324784621070388503875343276415727350138462309122970249248360"
                + "558507372126441214970999358314132226659275055927557999505011527820605715";

        int precision = 200;
        assertNumEquals(sqrtOfTwo, numOf(2).sqrt(precision));
    }

    @Test
    public void sqrtOfBigDouble() {
        var sqrtOfOnePointTwo = "1.095445115010332226913939565601604267905489389995966508453788899464986554245445467601716872327741252";

        int precision = 100;
        assertNumEquals(sqrtOfOnePointTwo, numOf(1.2).sqrt(precision));
    }

    @Test
    public void sqrtOfNegativeDouble() {
        assertTrue(numOf(-1.2).sqrt(12).isNaN());
        assertTrue(numOf(-1.2).sqrt().isNaN());
    }

    @Test
    public void sqrtOfZero() {
        assertNumEquals(0, numOf(0).sqrt(12));
        assertNumEquals(0, numOf(0).sqrt());
    }

    @Test
    public void sqrtLudicrousPrecision() {
        var numBD = BigDecimal.valueOf(Double.MAX_VALUE)
                .multiply(BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.ONE));
        if (numOf(0).getClass().equals(DoubleNum.class)) {
            var sqrt = DoubleNumFactory.getInstance().numOf(numBD).sqrt(100000);
            assertEquals("Infinity", sqrt.toString());
        } else if (numOf(0).getClass().equals(DecimalNum.class)) {
            var sqrt = DecimalNumFactory.getInstance(100000).numOf(numBD).sqrt(100000);
            var props = new Properties();
            try (InputStream is = getClass().getResourceAsStream("numTest.properties")) {
                props.load(is);
                assertNumEquals(props.getProperty("sqrtCorrect100000"), sqrt);
                assertNumNotEquals(props.getProperty("sqrtCorrect99999"), sqrt);
                assertNumEquals(Double.MAX_VALUE, sqrt);
                assertNumNotEquals(numOf(Double.MAX_VALUE), sqrt);
                var sqrtBD = new BigDecimal(sqrt.toString());
                assertNumEquals(numOf(numBD),
                        numOf(sqrtBD.multiply(sqrtBD, new MathContext(99999, RoundingMode.HALF_UP))));
                assertNumNotEquals(numOf(numBD), sqrt.multipliedBy(sqrt));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Test
    public void sqrtOddExponent() {
        var numBD = BigDecimal.valueOf(Double.valueOf("3E11"));
        var sqrt = numOf(numBD).sqrt();
        assertNumEquals("547722.55750516611345696978280080", sqrt);
    }

    @Test
    public void testSerialization() throws Exception {
        var numVal = numFactory.numOf(1.3);
        serializeDeserialize(numVal);
    }

    private static void serializeDeserialize(Num o) throws IOException, ClassNotFoundException {
        byte[] array;
        try (var baos = new ByteArrayOutputStream()) {
            try (var out = new ObjectOutputStream(baos)) {
                out.writeObject(o);
                array = baos.toByteArray();
            }
        }
        try (var baos = new ByteArrayInputStream(array)) {
            try (var out = new ObjectInputStream(baos)) {
                var deserialized = (Num) out.readObject();
                assertNotSame(o, deserialized);
                assertEquals(deserialized.doubleValue(), o.doubleValue());
            }
        }
    }

}
