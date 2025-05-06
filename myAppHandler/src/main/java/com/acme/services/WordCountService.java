package com.acme.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class WordCountService {
    Logger logger = LoggerFactory.getLogger(WordCountService.class);

    protected static final Set<String> STOP_WORDS_SET = new HashSet<>();

    @PostConstruct
    protected void loadStopWordsForLanguage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String fileName = "stop_words_en.txt";
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
        InputStream inputStream = new FileInputStream(file);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null;) {
            STOP_WORDS_SET.add(line);
        }
        logger.info("Stop Words Loaded {}", STOP_WORDS_SET.size());
    }

    public void wordsCount(Map<String, Integer> wordsCountMap, TreeMap<Integer, List<String>> sortedCountsMap, List<String> commentsToAnalyze) {

        for (String comment : commentsToAnalyze) {
            String[] wordsSplit = comment.split("\\W+");
            for (String word : wordsSplit) {
                String cleanWord = word.trim().toLowerCase();

                if (!STOP_WORDS_SET.contains(cleanWord)) {
                    int oldCount = wordsCountMap.getOrDefault(cleanWord, 0);
                    int newCount = oldCount + 1;

                    wordsCountMap.put(cleanWord, newCount);

                    if (oldCount > 0) {
                        List<String> wordsList = sortedCountsMap.get(oldCount);
                        if (wordsList != null) {
                            wordsList.remove(cleanWord);
                            if (wordsList.isEmpty()) {
                                sortedCountsMap.remove(oldCount);
                            }
                        }
                    }
                    sortedCountsMap.computeIfAbsent(newCount, k -> new ArrayList<>()).add(cleanWord);
                }
            }
        }
    }
}
