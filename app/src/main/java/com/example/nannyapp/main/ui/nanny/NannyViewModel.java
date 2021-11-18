package com.example.nannyapp.main.ui.nanny;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NannyViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NannyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is nanny fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}