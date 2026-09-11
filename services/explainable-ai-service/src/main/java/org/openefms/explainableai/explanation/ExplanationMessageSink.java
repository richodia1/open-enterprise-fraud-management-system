package org.openefms.explainableai.explanation;

public interface ExplanationMessageSink {
    void accept(ExplanationMessage message);
}
