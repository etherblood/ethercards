package com.etherblood.a.library.builder.ai;

import com.etherblood.a.templates.RawLibraryTemplate;
import java.nio.file.Path;
import java.util.Arrays;

public class LibraryAgent {

    public final Path filePath;
    public final int[] scores;
    public RawLibraryTemplate library;

    public LibraryAgent(Path filePath, int agentCount) {
        this.filePath = filePath;
        scores = new int[agentCount];
    }

    public int score() {
        return Arrays.stream(scores).sum();
    }
}
