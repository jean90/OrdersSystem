package com.amazingco.core.usecase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandTest {

    private static final class TestCommand extends Command {
        private TestCommand(String traceId) {
            super(traceId);
        }
    }

    @Test
    void exposesTraceId() {
        TestCommand command = new TestCommand("trace-123");

        assertEquals("trace-123", command.traceId());
    }

    @Test
    void rejectsNullTraceId() {
        assertThrows(IllegalArgumentException.class, () -> new TestCommand(null));
    }

    @Test
    void rejectsBlankTraceId() {
        assertThrows(IllegalArgumentException.class, () -> new TestCommand("   "));
    }
}
