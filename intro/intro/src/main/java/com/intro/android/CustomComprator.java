package com.intro.android;

import com.intro.model.ChatResponse;

import java.util.Comparator;

/**
 * Created by vinove on 22/12/15.
 */
public class CustomComprator implements Comparator<ChatResponse> {

    @Override
    public int compare(ChatResponse lhs, ChatResponse rhs) {
        return lhs.getTime().compareTo(rhs.getTime());
    }
}
