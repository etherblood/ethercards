package com.etherblood.a.library.builder.ai;

import com.etherblood.a.templates.RawLibraryTemplate;
import java.util.Arrays;

public class LibraryAgent {

    public RawLibraryTemplate library;
    public final int[] scores;

    public LibraryAgent(int agentCount) {
        scores = new int[agentCount];
    }

    public int score() {
        return Arrays.stream(scores).sum();
    }
}
