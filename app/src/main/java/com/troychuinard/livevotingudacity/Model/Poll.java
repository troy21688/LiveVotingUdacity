package com.troychuinard.livevotingudacity.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Poll {

    private String question;
    private String image_URL;
    private String user_id;
    private String display_name;
    private int vote_count;
    private ArrayList<String> answers;

    public Poll() {
    }

    public Poll(String Question, String Image_URL, ArrayList<String> answers, int vote_count, String UserID, String DisplayName) {
        this.question = Question;
        this.image_URL = Image_URL;
        this.answers = answers;
        this.vote_count = vote_count;
        this.user_id = UserID;
        this.display_name = DisplayName;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getImage_URL() {
        return image_URL;
    }


    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setImage_URL(String image_URL) {
        this.image_URL = image_URL;
    }

    public Integer getVote_count() {
        return vote_count;
    }

    public void setVote_count(Integer vote_count) {
        this.vote_count = vote_count;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("image_URL", image_URL);
        result.put("vote_count", 0);
        result.put("user_ID", user_id);
        result.put("display_name", display_name);
        return result;
    }

    @Exclude
    public Map<String, Object> answerConvert(ArrayList<String> answers, int index){
        HashMap<String, Object> result = new HashMap<>();
        result.put("answer", answers.get(index));
        result.put("vote_count", 0);
        return result;
    }

}
