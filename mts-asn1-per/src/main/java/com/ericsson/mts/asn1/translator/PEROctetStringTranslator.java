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
import com.ericsson.mts.asn1.CoderUtils;
import com.ericsson.mts.asn1.PERTranscoder;
import com.ericsson.mts.asn1.exception.NotHandledCaseException;
import com.ericsson.mts.asn1.factory.FormatReader;
import com.ericsson.mts.asn1.factory.FormatWriter;

import java.io.IOException;
import java.math.BigInteger;

public class PEROctetStringTranslator extends AbstractOctetStringTranslator {
    private PERTranscoder perTranscoder;

    public PEROctetStringTranslator(PERTranscoder perTranscoder) {
        this.perTranscoder = perTranscoder;
    }

    @Override
    public void doEncode(BitArray s, FormatReader reader, String value) throws IOException {
        logger.trace("Enter {} encoder, name {}", this.getClass().getSimpleName(), this.name);
        BigInteger ub, lb;

        if (constraints.hasSingleValueConstraints()) {
            ub = lb = constraints.getSingleValueConstraint();
        } else if (!constraints.hasSizeConstraint()) {
            ub = null;
            lb = BigInteger.ZERO;
        } else {
            lb = constraints.getLowerBound();
            ub = constraints.getUpperBound();
            if (ub == null) {
                ub = lb;
            }
            if (constraints.isExtensible()) {
                if (BigInteger.valueOf(value.length()).compareTo(lb) < 0 || BigInteger.valueOf(value.length()).compareTo(ub) > 0) {
                    //17.3
                    throw new NotHandledCaseException();
                } else {
                    perTranscoder.writePreambleBit(s, 0);
                }
            }
        }

        value = value.trim();

        //17.5
        if (!BigInteger.ZERO.equals(ub)) {
            if (lb.equals(ub) && lb.compareTo(BigInteger.valueOf(2)) <= 0) {
                //17.6
                perTranscoder.encodeBitField(s, value, value.length() * 4);
            } else if (lb.equals(ub) && new BigInteger("65536").compareTo(ub) > 0) {
                //17.7
                perTranscoder.skipAlignedBits(s);
                perTranscoder.encodeBitField(s, value, value.length() * 4);
            } else {
                //17.8
                if (ub != null) {
                    throw new NotHandledCaseException();
                } else {
                    perTranscoder.encodeLengthDeterminant(s, BigInteger.valueOf(value.length() / 2L + value.length() % 2).subtract(lb));
                    if (perTranscoder.isAligned())
                        s.skipAlignedBits();
                    perTranscoder.encodeBitField(s, value, value.length() * 4);
                }
            }
        }
    }

    @Override
    public byte[] doDecode(BitInputStream s, FormatWriter writer) throws IOException {
        logger.trace("Enter {} : {} translator", this.name, this);
        byte[] octetstring;
        boolean isExtendedOctetString = false;
        BigInteger ub, lb;

        if (constraints.hasSingleValueConstraints()) {
            ub = lb = constraints.getSingleValueConstraint();
        } else if (!constraints.hasSizeConstraint()) {
            ub = null;
            lb = BigInteger.ZERO;
        } else {
            lb = constraints.getLowerBound();
            ub = constraints.getUpperBound();
            if (ub == null) {
                ub = lb;
            }
            if (constraints.isExtensible()) {
                isExtendedOctetString = (1 == s.readBit());
            }
        }

        if (isExtendedOctetString) {
            //16.3
            throw new NotHandledCaseException();
        }

        if (BigInteger.ZERO.equals(ub)) {
            //16.5
            return null;
        } else if (lb.equals(ub) && BigInteger.valueOf(16).compareTo(lb) >= 0) {
            //16.6
            octetstring = perTranscoder.decodeOctetString(s, lb);
        } else if (lb.equals(ub) && BigInteger.valueOf(65536).compareTo(ub) >= 0) {
            //16.7
            throw new NotHandledCaseException();
        } else {
            //16.8
            BigInteger length;
            if (ub != null) {
                length = perTranscoder.decodeConstrainedNumber(lb, ub, s);
            } else {
                length = BigInteger.valueOf(perTranscoder.decodeLengthDeterminant(s)).add(lb);
            }
            octetstring = perTranscoder.decodeOctetString(s, length);
        }
        logger.trace("Result " + CoderUtils.bytesToHex(octetstring));
        return octetstring;
    }
}
