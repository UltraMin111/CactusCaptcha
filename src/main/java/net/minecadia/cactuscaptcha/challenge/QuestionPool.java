package net.minecadia.cactuscaptcha.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Pool of text-logic captcha questions designed to be immune to OCR/GPT solvers.
 * Contains various types of questions including math, spelling, logic, and pattern recognition.
 */
public class QuestionPool {
    
    private static final List<CaptchaQuestion> QUESTIONS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    
    static {
        initializeQuestions();
    }
    
    /**
     * Initialize the question pool with challenging questions.
     */
    private static void initializeQuestions() {
        // Math questions
        QUESTIONS.add(new CaptchaQuestion("What is 2 + 3?", Arrays.asList("4", "5", "6", "3"), 1));
        QUESTIONS.add(new CaptchaQuestion("What is 7 × 8?", Arrays.asList("54", "56", "58", "52"), 1));
        QUESTIONS.add(new CaptchaQuestion("What is 15 - 9?", Arrays.asList("5", "6", "7", "8"), 1));
        QUESTIONS.add(new CaptchaQuestion("What is 64 ÷ 8?", Arrays.asList("6", "7", "8", "9"), 2));
        QUESTIONS.add(new CaptchaQuestion("What is 3²?", Arrays.asList("6", "8", "9", "12"), 2));
        
        // Spelling questions
        QUESTIONS.add(new CaptchaQuestion("Which is spelled correctly?", Arrays.asList("definately", "definitely", "definitley", "defaniatly"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is spelled correctly?", Arrays.asList("recieve", "receive", "receve", "receave"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is spelled correctly?", Arrays.asList("seperate", "separate", "seperete", "separete"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is spelled correctly?", Arrays.asList("occured", "occurred", "ocurred", "occureed"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is spelled correctly?", Arrays.asList("necesary", "neccessary", "necessary", "neccesary"), 2));
        
        // Color/Logic questions
        QUESTIONS.add(new CaptchaQuestion("Click the color NOT listed: red, blue, green", Arrays.asList("yellow", "red", "blue", "green"), 0));
        QUESTIONS.add(new CaptchaQuestion("Click the color NOT listed: orange, purple, pink", Arrays.asList("orange", "purple", "black", "pink"), 2));
        QUESTIONS.add(new CaptchaQuestion("Which is NOT a primary color?", Arrays.asList("red", "green", "blue", "yellow"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is NOT a fruit?", Arrays.asList("apple", "carrot", "banana", "orange"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which is NOT an animal?", Arrays.asList("dog", "cat", "tree", "bird"), 2));
        
        // Pattern/Sequence questions
        QUESTIONS.add(new CaptchaQuestion("What comes next: 2, 4, 6, ?", Arrays.asList("7", "8", "9", "10"), 1));
        QUESTIONS.add(new CaptchaQuestion("What comes next: A, C, E, ?", Arrays.asList("F", "G", "H", "I"), 1));
        QUESTIONS.add(new CaptchaQuestion("Complete: Mon, Tue, Wed, ?", Arrays.asList("Thu", "Fri", "Sat", "Sun"), 0));
        QUESTIONS.add(new CaptchaQuestion("Complete: Jan, Feb, Mar, ?", Arrays.asList("Apr", "May", "Jun", "Jul"), 0));
        QUESTIONS.add(new CaptchaQuestion("What comes next: 1, 1, 2, 3, 5, ?", Arrays.asList("6", "7", "8", "9"), 2));
        
        // Logic/Reasoning questions
        QUESTIONS.add(new CaptchaQuestion("How many sides does a triangle have?", Arrays.asList("2", "3", "4", "5"), 1));
        QUESTIONS.add(new CaptchaQuestion("How many days in a week?", Arrays.asList("6", "7", "8", "9"), 1));
        QUESTIONS.add(new CaptchaQuestion("How many months in a year?", Arrays.asList("10", "11", "12", "13"), 2));
        QUESTIONS.add(new CaptchaQuestion("Which is the largest?", Arrays.asList("10", "100", "1000", "50"), 2));
        QUESTIONS.add(new CaptchaQuestion("Which is the smallest?", Arrays.asList("5", "2", "8", "9"), 1));
        
        // Word/Language questions
        QUESTIONS.add(new CaptchaQuestion("What is the opposite of 'hot'?", Arrays.asList("warm", "cold", "cool", "mild"), 1));
        QUESTIONS.add(new CaptchaQuestion("What is the opposite of 'up'?", Arrays.asList("left", "right", "down", "side"), 2));
        QUESTIONS.add(new CaptchaQuestion("Which word rhymes with 'cat'?", Arrays.asList("dog", "bat", "pig", "cow"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which word rhymes with 'tree'?", Arrays.asList("leaf", "bee", "bark", "root"), 1));
        QUESTIONS.add(new CaptchaQuestion("How many letters in 'HELLO'?", Arrays.asList("4", "5", "6", "7"), 1));
        
        // Simple knowledge questions
        QUESTIONS.add(new CaptchaQuestion("What color is grass?", Arrays.asList("blue", "green", "red", "yellow"), 1));
        QUESTIONS.add(new CaptchaQuestion("What color is the sky?", Arrays.asList("green", "red", "blue", "purple"), 2));
        QUESTIONS.add(new CaptchaQuestion("Which animal says 'moo'?", Arrays.asList("pig", "cow", "dog", "cat"), 1));
        QUESTIONS.add(new CaptchaQuestion("Which animal says 'woof'?", Arrays.asList("cat", "pig", "dog", "cow"), 2));
        QUESTIONS.add(new CaptchaQuestion("What do bees make?", Arrays.asList("milk", "honey", "cheese", "butter"), 1));
    }
    
    /**
     * Gets a random question from the pool.
     * 
     * @return A randomly selected CaptchaQuestion
     */
    public static CaptchaQuestion getRandomQuestion() {
        if (QUESTIONS.isEmpty()) {
            throw new IllegalStateException("Question pool is empty");
        }
        return QUESTIONS.get(RANDOM.nextInt(QUESTIONS.size()));
    }
    
    /**
     * Gets the total number of questions in the pool.
     * 
     * @return The number of questions available
     */
    public static int getQuestionCount() {
        return QUESTIONS.size();
    }
    
    /**
     * Gets all questions in the pool (for testing purposes).
     * 
     * @return A copy of the questions list
     */
    public static List<CaptchaQuestion> getAllQuestions() {
        return new ArrayList<>(QUESTIONS);
    }
}