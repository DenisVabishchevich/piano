package com.example.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONTokener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoApplication {

    private static final int KEYBOARD_MIN_OCTAVE = -3;
    private static final int KEYBOARD_MAX_OCTAVE = 5;
    private static final int NOTES_PER_OCTAVE = 12;

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java DemoApplication <input_file> <semitones_to_transpose>");
        }

        String inputFile = args[0];
        int semitonesToTranspose = Integer.parseInt(args[1]);

        List<int[]> notes = readNotesFromFile(inputFile);
        List<int[]> transposedNotes = transposeNotes(notes, semitonesToTranspose);

        printToConsole(transposedNotes);
        log.info("Transposition completed successfully. Output written to output.json");
    }

    private static List<int[]> readNotesFromFile(String filename) {
        try (InputStream inputStream = new FileInputStream(filename)) {
            JSONTokener tokener = new JSONTokener(inputStream);
            JSONArray jsonArray = new JSONArray(tokener);
            List<int[]> notes = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray noteArray = jsonArray.getJSONArray(i);
                int[] note = new int[]{noteArray.getInt(0), noteArray.getInt(1)};
                notes.add(note);
            }

            return notes;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Error reading notes", e);
        }
    }

    private static List<int[]> transposeNotes(List<int[]> notes, int semitonesToTranspose) {
        List<int[]> transposedNotes = new ArrayList<>();

        for (int[] note : notes) {
            int octave = note[0];
            int noteNumber = note[1];

            // Transpose note
            int transposedNoteNumber = noteNumber + semitonesToTranspose;
            int transposedOctave = octave;

            // Adjust octave and note number if noteNumber becomes negative
            while (transposedNoteNumber < 0) {
                transposedNoteNumber += NOTES_PER_OCTAVE;
                transposedOctave--;
            }

            // Check if note falls out of keyboard range
            if (isLessThenMinimum(transposedOctave, transposedNoteNumber)
                || isMoreThemMax(transposedOctave, transposedNoteNumber)) {
                throw new IllegalArgumentException("Out of range [" + transposedOctave + "," + transposedNoteNumber + "]");
            }

            transposedNotes.add(new int[]{transposedOctave, transposedNoteNumber % NOTES_PER_OCTAVE});
        }

        return transposedNotes;
    }

    private static boolean isMoreThemMax(int transposedOctave, int transposedNoteNumber) {
        return transposedOctave > KEYBOARD_MAX_OCTAVE - 1 && transposedNoteNumber > 1;
    }

    private static boolean isLessThenMinimum(int transposedOctave, int transposedNoteNumber) {
        return transposedOctave < KEYBOARD_MIN_OCTAVE + 1 && transposedNoteNumber < 10;
    }

    private static void printToConsole(List<int[]> notes) {
        String result = notes.stream().map(Arrays::toString).collect(Collectors.joining(","));
        log.info(result);
    }
}