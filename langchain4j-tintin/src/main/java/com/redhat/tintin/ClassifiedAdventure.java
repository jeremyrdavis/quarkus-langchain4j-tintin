package com.redhat.tintin;

public record ClassifiedAdventure(
    AdventureCategory category,
    String reasoning,
    int confidencePercent
) {
}
