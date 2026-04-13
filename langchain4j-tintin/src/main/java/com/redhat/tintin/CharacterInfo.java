package com.redhat.tintin;

import java.util.List;

public record CharacterInfo(
    String name,
    String description,
    String firstAppearance,
    String notableTraits,
    List<String> associatedAdventures
) {
}
