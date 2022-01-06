package com.example.nannyapp.main.ui.profile.parent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ParentProfileViewModel extends ViewModel {
    private MutableLiveData<String> firstName;
    private MutableLiveData<String> lastName;
    private MutableLiveData<String> email;
    private MutableLiveData<String> phoneNumber;
    private MutableLiveData<String> noChildren;
    private MutableLiveData<String> address;
    private MutableLiveData<String> description;

    public ParentProfileViewModel() {
        firstName = new MutableLiveData<>();
        lastName = new MutableLiveData<>();
        email = new MutableLiveData<>();
        phoneNumber = new MutableLiveData<>();
        noChildren = new MutableLiveData<>();
        address = new MutableLiveData<>();
        description = new MutableLiveData<>();
    }

    public LiveData<String> getFirstName() {
        return firstName;
    }

    public LiveData<String> getLastName() {
        return lastName;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public LiveData<String> getNoChildren() {
        return noChildren;
    }

    public LiveData<String> getAddress() {
        return address;
    }

    public LiveData<String> getDescription() {
        return description;
    }

    public void setFirstName(String firstName) {
        this.firstName.setValue(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName.setValue(lastName);
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.setValue(phoneNumber);
    }

    public void setNoChildren(String noChildren) {
        this.noChildren.setValue(noChildren);
    }

    public void setAddress(String address) {
        this.address.setValue(address);
    }

    public void setDescription(String description) {
        this.description.setValue(description);
    }
    // TODO: Implement the ViewModel
}