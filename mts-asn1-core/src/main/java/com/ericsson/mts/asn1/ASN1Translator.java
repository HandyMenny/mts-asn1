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

import com.ericsson.mts.asn1.factory.AbstractTranslatorFactory;
import com.ericsson.mts.asn1.factory.FormatReader;
import com.ericsson.mts.asn1.factory.FormatWriter;
import com.ericsson.mts.asn1.registry.MainRegistry;
import com.ericsson.mts.asn1.visitor.TopLevelVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ASN1Translator {
    private final Logger logger = LoggerFactory.getLogger(ASN1Translator.class.getSimpleName());
    private final MainRegistry registry;

    public ASN1Translator(AbstractTranslatorFactory factory, List<InputStream> stream) throws IOException {
        registry = new MainRegistry(factory);
        for (InputStream inputStream : stream) {
            ParseTree tree = parseTreeFromStream(inputStream);
            beginVisit(tree);
        }
    }

    private ASN1Parser.ModuleDefinitionContext parseTreeFromStream(InputStream stream) throws IOException {
        CharStream inputStream = CharStreams.fromStream(stream);
        ASN1Lexer asn1Lexer = new ASN1Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(asn1Lexer);
        ASN1Parser asn1Parser = new ASN1Parser(commonTokenStream);
        asn1Parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        return asn1Parser.moduleDefinition();
    }

    public void encode(String string, BitArray bitArray, FormatReader formatReader) throws Exception {
        registry.getTranslatorFromName(string).encode(string, bitArray, formatReader, null);
    }

    public void decode(String str, InputStream stream, FormatWriter formatWriter) throws Exception {
        registry.getTranslatorFromName(str).decode(str, new BitInputStream(stream), formatWriter, null);
    }

    private void beginVisit(ParseTree tree) {
        new TopLevelVisitor(registry).visit(tree);
    }

    public ASN1Translator(AbstractTranslatorFactory factory, ParseTree tree) {
        registry = new MainRegistry(factory);
        beginVisit(tree);
    }

    public void parseTranslators() {
        registry.parseConstants();
        registry.parseTranslators();
        registry.parseClassHandler();
        registry.parseClassObject();
        registry.parseClassObjectSet();
        if (!registry.checkIndexingRegistry()) {
            //Use debugger : registry -> indexingRegistry to see what's left
            throw new RuntimeException("IndexingRegistry isn't empty !");
        }
    }
}
