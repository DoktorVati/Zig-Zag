package com.zigzag;

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
            boolean firstLine = true; // To skip the header row

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;  // Skip the header row
                    continue;
                }

                // Split the line by commas
                String[] columns = line.split(",");

                if (columns.length > 0) {
                    String curseWord = columns[0].trim();  // The 'Text' column
                    if (!curseWord.isEmpty()) {
                        curseWords.add(curseWord);  // Add the curse word to the list
                    }
                }
            }

            reader.close();  // Don't forget to close the reader
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
