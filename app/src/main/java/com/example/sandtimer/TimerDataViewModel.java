package com.example.sandtimer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TimerDataViewModel extends ViewModel {
    private final int codeLength;

    private TimerData timerData = null;
    public void setTimerData(TimerData val) {timerData = val;}
    public TimerData getTimerData() { return timerData; }

    public TimerDataViewModel(int codeLength)
    {
        super();
        this.codeLength = codeLength;
    }
    private String setCode = null;
    public String getSetCode() {
        if(setCode == null)
            createRandomCodes();
        return setCode;
    }
    private String pauseCode = null;
    public String getPauseCode(){
        if(pauseCode == null)
            createRandomCodes();
        return pauseCode;
    }
    private String codeDigits = null;
    public String getCodeDigits(){
        if(codeDigits == null)
            createRandomCodes();
        return codeDigits;
    }

    private void createRandomCodes() {
        RandomCode randomCode = new RandomCode(codeLength);
        codeDigits = randomCode.getCodeDigits();
        setCode = randomCode.getRandomCode();
        pauseCode = setCode;
        while(pauseCode.equals(setCode))
            pauseCode = randomCode.getRandomCode();
    }

    private String set_code_partial = "";
    private String pause_code_partial = "";

    public static ViewModelProvider.Factory getFactory(final int codeLength) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new TimerDataViewModel(codeLength);
            }
        };
    }

    public String match_code_digit(String digit) {

        set_code_partial += digit;
        if (set_code_partial.equals(getSetCode())) {
            set_code_partial = "";
            pause_code_partial = "";
            return setCode;
        }
        if(!set_code_partial.equals(getSetCode().substring(0, set_code_partial.length())))
            set_code_partial = "";

        pause_code_partial += digit;
        if (getPauseCode().equals(pause_code_partial)) {
            set_code_partial = "";
            pause_code_partial = "";
            return pauseCode;
        }
        if (!pause_code_partial.equals(getPauseCode().substring(0, pause_code_partial.length())))
            pause_code_partial = "";

        return "";
    }

    static String KEY_TIMER_DATA = "timer_data";
    static String KEY_SET_CODE = "set_code";
    static String KEY_PAUSE_CODE = "pause_code";
    static String KEY_CODE_DIGITS = "code_digits";

    public void saveInstanceState(Bundle b) {
        b.putParcelable(KEY_TIMER_DATA, timerData);
        b.putString(KEY_SET_CODE, setCode);
        b.putString(KEY_PAUSE_CODE, pauseCode);
        b.putString(KEY_CODE_DIGITS, codeDigits);
    }

        public void loadInstanceState(Bundle b) {
        if(b == null) return;
        timerData = b.getParcelable(KEY_TIMER_DATA);
        setCode = b.getString(KEY_SET_CODE);
        pauseCode = b.getString(KEY_PAUSE_CODE);
        codeDigits = b.getString(KEY_CODE_DIGITS);
        set_code_partial = "";
        pause_code_partial = "";
    }

    public int getSetCodeMatchLength() {
        return set_code_partial.length();
    }

    public int getPauseCodeMatchLength() {
        return pause_code_partial.length();
    }
}
