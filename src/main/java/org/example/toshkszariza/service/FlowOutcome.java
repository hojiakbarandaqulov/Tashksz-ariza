package org.example.toshkszariza.service;

import org.example.toshkszariza.service.flow.StepResult;

public record FlowOutcome(StepResult result, DraftView draft) {
}
