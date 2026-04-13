package com.redhat.tintin.tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TintinFactsTool {

    private static final Map<String, Integer> PUBLICATION_YEARS = Map.ofEntries(
            Map.entry("Tintin in the Land of the Soviets", 1930),
            Map.entry("Tintin in the Congo", 1931),
            Map.entry("Tintin in America", 1932),
            Map.entry("Cigars of the Pharaoh", 1934),
            Map.entry("The Blue Lotus", 1936),
            Map.entry("The Broken Ear", 1937),
            Map.entry("The Black Island", 1938),
            Map.entry("King Ottokar's Sceptre", 1939),
            Map.entry("The Crab with the Golden Claws", 1941),
            Map.entry("The Shooting Star", 1942),
            Map.entry("The Secret of the Unicorn", 1943),
            Map.entry("Red Rackham's Treasure", 1944),
            Map.entry("The Seven Crystal Balls", 1948),
            Map.entry("Prisoners of the Sun", 1949),
            Map.entry("Land of Black Gold", 1950),
            Map.entry("Destination Moon", 1953),
            Map.entry("Explorers on the Moon", 1954),
            Map.entry("The Calculus Affair", 1956),
            Map.entry("The Red Sea Sharks", 1958),
            Map.entry("Tintin in Tibet", 1960),
            Map.entry("The Castafiore Emerald", 1963),
            Map.entry("Flight 714 to Sydney", 1968),
            Map.entry("Tintin and the Picaros", 1976),
            Map.entry("Tintin and Alph-Art", 1986)
    );

    private static final Map<String, List<String>> CHARACTERS_PER_ADVENTURE = Map.of(
            "Tintin in the Land of the Soviets", List.of("Tintin", "Snowy"),
            "The Crab with the Golden Claws", List.of("Tintin", "Snowy", "Captain Haddock", "Thomson and Thompson"),
            "The Secret of the Unicorn", List.of("Tintin", "Snowy", "Captain Haddock", "Thomson and Thompson"),
            "Red Rackham's Treasure", List.of("Tintin", "Snowy", "Captain Haddock", "Professor Calculus", "Thomson and Thompson"),
            "Destination Moon", List.of("Tintin", "Snowy", "Captain Haddock", "Professor Calculus", "Thomson and Thompson"),
            "Tintin in Tibet", List.of("Tintin", "Snowy", "Captain Haddock", "Chang Chong-Chen")
    );

    @Tool("Look up the publication year of a specific Tintin adventure by title")
    public String getPublicationYear(String adventureTitle) {
        Integer year = PUBLICATION_YEARS.get(adventureTitle);
        if (year != null) {
            return adventureTitle + " was published in " + year;
        }
        return "Unknown adventure: " + adventureTitle + ". Known adventures include: " +
                String.join(", ", PUBLICATION_YEARS.keySet());
    }

    @Tool("Get a list of all Tintin album titles in order of publication")
    public List<String> getAllAdventures() {
        return PUBLICATION_YEARS.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .toList();
    }

    @Tool("Look up which characters appear in a specific Tintin adventure")
    public String getCharactersInAdventure(String adventureTitle) {
        List<String> characters = CHARACTERS_PER_ADVENTURE.get(adventureTitle);
        if (characters != null) {
            return "Characters in " + adventureTitle + ": " + String.join(", ", characters);
        }
        return "Character data not available for: " + adventureTitle;
    }
}
