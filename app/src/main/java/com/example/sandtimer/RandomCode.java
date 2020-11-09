package com.example.sandtimer;

import java.util.ArrayList;

public class RandomCode {
    private ArrayList<String> chosen_digits;
    private int code_length;
    public RandomCode(int code_length)
    {
        this.code_length = code_length;

        ArrayList<String> digits;
        digits = new java.util.ArrayList<String>();
        for(int d = 0; d < 10; d++)
            digits.add(String.valueOf(d));
        java.util.Collections.shuffle(digits);

        chosen_digits = new ArrayList<String>();
        for(int i = 0; i < code_length; i++)
            chosen_digits.add(digits.get(i));
    }

    public String getRandomCode() {
        java.util.Collections.shuffle(chosen_digits);
        return getCodeDigits();
    }

    public String getCodeDigits() {
        String code_digits = "";
        for(int i = 0; i < code_length; i++)
            code_digits += chosen_digits.get(i);
        return code_digits;
    }
}
