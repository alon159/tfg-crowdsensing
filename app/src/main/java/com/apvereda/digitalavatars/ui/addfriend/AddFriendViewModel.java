package com.apvereda.digitalavatars.ui.addfriend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddFriendViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AddFriendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}