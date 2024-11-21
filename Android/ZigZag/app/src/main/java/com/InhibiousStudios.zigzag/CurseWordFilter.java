package com.InhibiousStudios.zigzag;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CurseWordFilter {

    private Context context;

    // Constructor to accept a Context
    public CurseWordFilter(Context context) {
        this.context = context;
    }

    // Method to load curse words from the CSV file in assets
    public List<String> loadCurseWords() {
        List<String> curseWords = new ArrayList<>();

        try {
            // Get the AssetManager using the provided context
            AssetManager assetManager = context.getAssets();

            // Open the CSV file
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(assetManager.open("profanity_en.csv"))
            );

            String line;
            // To skip the header row
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    // Skip the header row
                    firstLine = false;
                    continue;
                }

                // Split the line by commas
                String[] columns = line.split(",");

                if (columns.length > 0) {
                    // The Text column
                    String curseWord = columns[0].trim();
                    if (!curseWord.isEmpty()) {
                        // Add the curse word to the list to be censored
                        curseWords.add(curseWord);
                    }
                }
            }
            // Don't forget to close the reader
            reader.close();
        } catch (IOException e) {
            Log.e("CurseWordFilter", "Error reading curse words file", e);
        }

        return curseWords;
    }

    // Method to clean user input by replacing curse words
    public String cleanInput(String input, List<String> curseWords) {
        for (String curseWord : curseWords) {
            String replacement = new String(new char[curseWord.length()]).replace("\0", "*");
            input = input.replaceAll("(?i)\\b" + curseWord + "\\b", replacement);
        }
        return input;
    }
}
