package irfs.videonews1;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomh on 20/03/15.
 */
public class Chapter implements Serializable {
    int start;
    int end;
    String title;
    boolean hasAudio;
    List<Caption> captions;
    List<Integer> links;

    static class Caption implements Serializable {
        int start;
        int end;
        String body;

    }

}
