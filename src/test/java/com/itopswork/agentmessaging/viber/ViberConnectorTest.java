package com.itopswork.agentmessaging.viber;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.itopswork.agentmessaging.messaging.OutgoingMessage;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ViberConnectorTest {
    private final ViberApi api = mock(ViberApi.class);
    private final ViberConnector connector = new ViberConnector(api);

    @Test
    void splitsLongTextWithoutBreakingSurrogatePair() {
        String first = "a".repeat(2999);
        String text = first + "😀tail";

        connector.send(new OutgoingMessage("viber", "user-123", text));

        InOrder calls = inOrder(api);
        calls.verify(api).sendMessage("user-123", first);
        calls.verify(api).sendMessage("user-123", "😀tail");
        calls.verifyNoMoreInteractions();
    }

    @Test
    void rejectsAnotherPlatform() {
        assertThrows(IllegalArgumentException.class,
                () -> connector.send(new OutgoingMessage("telegram", "123", "hello")));
    }
}
