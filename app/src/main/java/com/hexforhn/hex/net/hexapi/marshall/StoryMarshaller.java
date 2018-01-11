package com.hexforhn.hex.net.hexapi.marshall;

import com.hexforhn.hex.model.Comment;
import com.hexforhn.hex.model.Story;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoryMarshaller {

    public static Story marshall(JSONObject rawStory) {
        try {
            String id = rawStory.getString("id");
            String title = rawStory.getString("title");
            String url = rawStory.getString("url");
            String commentsUrl = rawStory.getString("commentsUrl");
            String author = rawStory.getString("author");
            String domain = rawStory.getString("domain");
            int score = rawStory.getInt("score");
            int commentCount = rawStory.getInt("commentCount");
            Date date = new DateTime(rawStory.getString("time")).toDate();

            JSONArray rawComments = rawStory.has("comments") ? rawStory.getJSONArray("comments") : null;
            List<Comment> comments =  new ArrayList<>();
            if (rawComments != null) {
                comments = marshallComments(rawComments);
            }

            return new Story(id, title, url, commentsUrl, author, domain, score, commentCount, date, comments);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Comment> marshallComments(JSONArray rawComments) {
        List<Comment> comments = new ArrayList<>();

        for (int i = 0; i < rawComments.length(); i++) {
            try {
                JSONObject rawComment = rawComments.getJSONObject(i);
                List<Comment> childComments = new ArrayList<>();
                JSONArray rawChildComments = rawComment.getJSONArray("comments");

                if (rawChildComments != null) {
                    childComments = marshallComments(rawComment.getJSONArray("comments"));
                }

                String text = rawComment.getString("text");
                String author = rawComment.getString("author");
                int commentCount = rawComment.getInt("commentCount");
                Date date = new DateTime(rawComment.getString("time")).toDate();
                comments.add(new Comment(text, author, childComments, commentCount, date));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return comments;
    }
}
