/*
 * Copyright 2019 Ericsson, https://www.ericsson.com/en
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.ericsson.mts.asn1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class S1APAuthWSyncFailureTests extends AbstractTests {

  @BeforeAll
  static void init() {
    try {
      asn1Translator = new ASN1Translator(new PERTranslatorFactoryKT(true), Collections.singletonList(S1APAuthWSyncFailureTests.class.getResourceAsStream("/grammar/S1AP/S1AP.asn")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void testInitialUEMessage() throws Exception {
    test("S1AP-PDU", "/data/S1AP/auth_w_sync_failure/InitialUEMessage/InitialUEMessage.bin", "/data/S1AP/auth_w_sync_failure/InitialUEMessage/InitialUEMessage.json", "/data/S1AP/auth_w_sync_failure/InitialUEMessage/InitialUEMessage.xml");
  }

  @Test
  void testDownlinkNASTransport() throws Exception {
    test("S1AP-PDU", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport/DownlinkNASTransport.bin", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport/DownlinkNASTransport.json", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport/DownlinkNASTransport.xml");
  }

  @Test
  void testUplinkNASTransport() throws Exception {
    test("S1AP-PDU", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport/UplinkNASTransport.bin", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport/UplinkNASTransport.json", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport/UplinkNASTransport.xml");
  }

  @Test
  void testDownlinkNASTransport2() throws Exception {
    test("S1AP-PDU", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport2/DownlinkNASTransport2.bin", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport2/DownlinkNASTransport2.json", "/data/S1AP/auth_w_sync_failure/DownlinkNASTransport2/DownlinkNASTransport2.xml");
  }

  @Test
  void testUplinkNASTransport2() throws Exception {
    test("S1AP-PDU", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport2/UplinkNASTransport2.bin", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport2/UplinkNASTransport2.json", "/data/S1AP/auth_w_sync_failure/UplinkNASTransport2/UplinkNASTransport2.xml");
  }
}
