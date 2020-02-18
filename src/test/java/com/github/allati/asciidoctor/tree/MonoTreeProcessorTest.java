package com.github.allati.asciidoctor.tree;

import com.uniqueck.asciidoctorj.extension.support.AbstractAsciidoctorjExtensionSupportTestHelper;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonoTreeProcessorTest extends AbstractAsciidoctorjExtensionSupportTestHelper {

    @BeforeEach
    void before() {
        getAsciidoctor().registerLogHandler(new LogHandler() {
            @Override
            public void log(LogRecord logRecord) {
                Assertions.fail(logRecord.getMessage());
            }
        });
    }



    @Test
    @DisplayName("default")
    void testDefault() {
        String actualConverted = convert(
                joinStringsWithLineSeparator(
            "[monotree]",
                    "----",
                    "> A",
                    ">> B",
                    ">>> C",
                    ">> D",
                    ">>> E",
                    ">>> F",
                    "----"
                ));
        assertNotNull(actualConverted);
        assertEquals(joinStringsWithLineSeparator(
        "<div class=\"listingblock\">",
                "<div class=\"content\">",
                "<pre>A",
                "├── B",
                "│   └── C",
                "└── D",
                "    ├── E",
                "    └── F</pre>",
                "</div>",
                "</div>"
        ), actualConverted);
    }



    @Test
    @DisplayName("narrow")
    void testNarrow() {
        String actualConverted = convert(
                joinStringsWithLineSeparator(
                        "[monotree, e=\"   \", p=\"│  \", j=\"├─ \", t=\"└─ \"]",
                        "----",
                        "> A",
                        ">> B",
                        ">>> C",
                        ">> D",
                        ">>> E",
                        ">>> F",
                        "----"
                ));
        assertNotNull(actualConverted);
        assertEquals(joinStringsWithLineSeparator(
                "<div class=\"listingblock\">",
                "<div class=\"content\">",
                "<pre>A",
                "├─ B",
                "│  └─ C",
                "└─ D",
                "   ├─ E",
                "   └─ F</pre>",
                "</div>",
                "</div>"
        ), actualConverted);
    }

    @Test
    @DisplayName("\"Simple\" symbol set")
    void testSimpleSymbolSet() {
        String actualConverted = convert(
                joinStringsWithLineSeparator(
                        "[monotree, symbols=\"simple\"]",
                        "----",
                        "> A",
                        ">> B",
                        ">>> C",
                        ">> D",
                        ">>> E",
                        ">>> F",
                        "----"
                ));
        assertNotNull(actualConverted);
        assertEquals(joinStringsWithLineSeparator(
                "<div class=\"listingblock\">",
                "<div class=\"content\">",
                "<pre>A",
                "+-- B",
                "|    `-- C",
                "`-- D",
                "    +-- E",
                "    `-- F</pre>",
                "</div>",
                "</div>"
        ), actualConverted);
    }

    @Test
    @DisplayName("\"Simple\" symbol set with one element overridden")
    void testSimpleSymbolSetWithOneElementOverridden() {
        String actualConverted = convert(
                joinStringsWithLineSeparator(
                        "[monotree, symbols=\"simple\", t=\"\\-- \"]",
                        "----",
                        "> A",
                        ">> B",
                        ">>> C",
                        ">> D",
                        ">>> E",
                        ">>> F",
                        "----"
                ));
        assertNotNull(actualConverted);
        assertEquals(joinStringsWithLineSeparator(
                "<div class=\"listingblock\">",
                "<div class=\"content\">",
                "<pre>A",
                "+-- B",
                "|    \\-- C",
                "\\-- D",
                "    +-- E",
                "    \\-- F</pre>",
                "</div>",
                "</div>"
        ), actualConverted);
    }


    String joinStringsWithLineSeparator(String... content) {
        return String.join(System.lineSeparator(), content);
    }



    @Test
    @DisplayName("Empty root")
    void testEmptyRoot() {
        String actualConverted = convert(
                joinStringsWithLineSeparator(
                        "[monotree]",
                        "----",
                        ">> A",
                        ">> B",
                        ">>> C",
                        ">> D",
                        ">>> E",
                        ">>> F",
                        "----"
                ));
        assertNotNull(actualConverted);
        assertEquals(joinStringsWithLineSeparator(
                "<div class=\"listingblock\">",
                "<div class=\"content\">",
                "<pre>├── A",
                "├── B",
                "│   └── C",
                "└── D",
                "    ├── E",
                "    └── F</pre>",
                "</div>",
                "</div>"
        ), actualConverted);
    }


}