package irfs.videonews1;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Created by tomh on 18/03/15.
 */
public class ContentPane extends Fragment {


    private VideoView mainVideo;
    private MediaController mediaControls;

    boolean m_isVisible;
    Chapter chapter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_pane,container,false);

        View capView = rootView.findViewById(R.id.captionView);
        chapter = (Chapter) args.getSerializable("chapter");
        ((TextView) capView).setText(chapter.title);


        mediaControls = new MediaController(getActivity());
        mainVideo = (VideoView) rootView.findViewById(R.id.mainVideo);
        mainVideo.setMediaController(mediaControls);
        mainVideo.setVideoURI(Uri.parse(args.getString("uri")));
        //mainVideo.requestFocus();
        //mainVideo.start();

        Log.d("ContentPane","creating page "+args.getInt("position"));

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        Bundle args = getArguments();

        super.setUserVisibleHint(isVisibleToUser);
        m_isVisible = isVisibleToUser;

        Log.d("contentpane "+args.getInt("position"),"visibility is " + m_isVisible);
        if (mainVideo != null) {
            if (m_isVisible) {
                Log.d("ContentPane "+args.getInt("position"),"starting");
                //mainVideo.start();

            } else {
                Log.d("ContentPane "+args.getInt("position"),"stopping playback");

                mainVideo.pause();
                mainVideo.seekTo(0);

            }
        }
    }

    @Override
    public void onDestroyView() {
        Bundle args = getArguments();
        Log.d("ContentPane","destroying page "+args.getInt("position"));

        super.onDestroyView();
    }
}
