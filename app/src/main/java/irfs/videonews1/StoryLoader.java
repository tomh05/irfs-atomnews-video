package irfs.videonews1;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomh on 20/03/15.
 * Loads JSON into Chapters
 */
public class StoryLoader {

    Context context;

    public StoryLoader(Context _context) {
        context = _context;
    }

    public Story loadStoryFromLocalJSON() {

    InputStream is = this.context.getResources().openRawResource(R.raw.content);
        byte[] buffer;
        String jsonString = "";

        try {
            int size = is.available();
            buffer = new byte[size];

            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e("StoryLoader","IOError: " + e.getMessage());
        }

        Story story = new Story();

        try {
            JSONObject content = new JSONObject(jsonString);

            story.title = content.getString("title");
            story.chapters = new ArrayList<Chapter>();

            JSONArray chaptersArrayJSON = content.getJSONArray("chapters");
            for (int i=0; i<chaptersArrayJSON.length(); i++) {
                Log.d("JSON","chapter  "+i + " of " + chaptersArrayJSON.length());
                JSONObject chapterJSON = chaptersArrayJSON.getJSONObject(i);
                // make a new chapter
                Chapter chapter = new Chapter();
                chapter.start = chapterJSON.getInt("start");
                chapter.end = chapterJSON.getInt("end");
                chapter.title = chapterJSON.getString("title");

                JSONArray captionsJSON = chapterJSON.getJSONArray("captions");
                chapter.captions = new ArrayList<Chapter.Caption>();

                for (int j=0; j<captionsJSON.length(); j++) {
                    Log.d("JSON","caption  "+j + " of " + captionsJSON.length());
                    JSONObject captionJSON = captionsJSON.getJSONObject(j);
                    Chapter.Caption caption = new Chapter.Caption();
                    caption.start = captionJSON.getInt("start");
                    caption.end = captionJSON.getInt("end");
                    caption.body = captionJSON.getString("body");
                    Log.d("JSON",caption.body);
                    chapter.captions.add(caption);
                }

                story.chapters.add(chapter);
            }

        } catch (JSONException e) {
            Log.e("StoryLoader", "JSON Error: " + e.getMessage());
        }
        return story;

}


}

   // JSONObject(string);
