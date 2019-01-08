package com.wslclds.castn.items;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class SearchSuggestionItem implements SearchSuggestion {

    String suggestion;

    public SearchSuggestionItem(String suggestion){
        this.suggestion = suggestion.toLowerCase();
    }

    public SearchSuggestionItem(Parcel source) {
        this.suggestion = source.readString();
    }

    @Override
    public String getBody() {
        return this.suggestion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.suggestion);
    }

    public static final Creator<SearchSuggestionItem> CREATOR = new Creator<SearchSuggestionItem>() {
        @Override
        public SearchSuggestionItem createFromParcel(Parcel parcel) {
            return new SearchSuggestionItem(parcel);
        }

        @Override
        public SearchSuggestionItem[] newArray(int i) {
            return new SearchSuggestionItem[i];
        }
    };
}
