package org.openefms.explainableai.messaging;

public interface ExplanationMessageSink {
    public void accept(ExplanationMessage message);
}
