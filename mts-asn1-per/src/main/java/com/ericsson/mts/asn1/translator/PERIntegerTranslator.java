/*
 * Copyright 2019 Ericsson, https://www.ericsson.com/en
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.ericsson.mts.asn1.translator;

import com.ericsson.mts.asn1.BitArray;
import com.ericsson.mts.asn1.BitInputStream;
import com.ericsson.mts.asn1.PERTranscoder;
import com.ericsson.mts.asn1.exception.NotHandledCaseException;

import java.io.IOException;
import java.math.BigInteger;

import static java.math.BigInteger.ONE;

public class PERIntegerTranslator extends AbstractIntegerTranslator {
    private PERTranscoder perTranscoder;

    public PERIntegerTranslator(PERTranscoder perTranscoder) {
        this.perTranscoder = perTranscoder;
    }

    @Override
    public void doEncode(BitArray s, BigInteger value) throws IOException {
        logger.trace("Enter {}", this);
        BigInteger lb = null, ub = null;
        if (constraints.hasSingleValueConstraints()) {
            return;
        }

        if (constraints.hasValueRangeConstraint()) {
            lb = constraints.getLowerRange();
            ub = constraints.getUpperRange();
        }

        if (constraints.isExtensible()) {
            if ((lb != null && value.compareTo(lb) < 0) || (ub != null && value.compareTo(ub) > 0)) {
                //WARNING : Look at 13.2.6 b) before removing this exception !
                throw new NotHandledCaseException();
            } else {
                s.writeBit(0);
            }
        }

        if (!constraints.hasValueRangeConstraint() || lb == null) {
            //13.2.4
            throw new NotHandledCaseException();
        } else if (ub == null) {
            //13.2.3
            throw new NotHandledCaseException();
        } else if (lb.equals(ub)) {
            //13.2.1
            throw new NotHandledCaseException();
        } else {
            //13.2.2
            if (ub.subtract(lb).add(ONE).compareTo(BigInteger.valueOf(65536)) <= 0) {
                //13.2.5
                //constrained whole number
                perTranscoder.encodeConstrainedWholeNumber(s, value, lb, ub);
            } else {
                //13.2.6
                //indefinite length case
                perTranscoder.encodeConstrainedLengthDeterminant(s,
                        BigInteger.valueOf(perTranscoder.toByteCount(value.bitLength()))
                        , BigInteger.ONE,
                        BigInteger.valueOf(perTranscoder.toByteCount(ub.subtract(lb).bitLength())));
                perTranscoder.skipAlignedBits(s);
                perTranscoder.encodeConstrainedWholeNumber(s, value.subtract(lb), lb, ub);
            }
        }
    }

    @Override
    public BigInteger doDecode(BitInputStream s) throws NotHandledCaseException, IOException {
        logger.trace("Enter {}", this);
        boolean isExtendedInteger = false;
        BigInteger number;

        if (constraints.hasSingleValueConstraints()) {
           return constraints.getSingleValueConstraint();
        }

        if (constraints.isExtensible()) {
            isExtendedInteger = (1 == s.readBit());
        }

        if (isExtendedInteger) {
            throw new NotHandledCaseException();
        } else {
            if (constraints.getLowerRange() != null && constraints.getUpperRange() != null) {
                logger.trace("Decode ConstrainedNumber");
                number = perTranscoder.decodeConstrainedNumber(constraints.getLowerRange(), constraints.getUpperRange(), s);
            } else {
                throw new RuntimeException();
            }
            return number;
        }
    }
}
