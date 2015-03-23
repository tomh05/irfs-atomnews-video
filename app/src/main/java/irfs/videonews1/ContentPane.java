package irfs.videonews1;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

/**
 * Created by tomh on 18/03/15.
 */
public class ContentPane extends Fragment implements SurfaceHolder.Callback {


    //private VideoView mainVideo;
    //private MediaController mediaControls;


    // media player
    private MediaPlayer mp;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    boolean m_isVisible = false;

    private Handler videoTimerHandler = new Handler();

    ViewGroup rootView;
    Uri videoSource;

    Chapter chapter;
    TextView captionView;

    public interface UpdatePercentListener {
        public void updatePercent(float percent);
    }
    UpdatePercentListener mUpdatePercentListener;


    public static ContentPane newInstance(int _position, String _uriString, Chapter _chapter) {
        ContentPane contentPane = new ContentPane();
        Bundle args = new Bundle();
        args.putInt("position",_position);

        args.putString("uriString", _uriString);
        args.putSerializable("chapter",_chapter);
        contentPane.setArguments(args);
        Log.d("newInstance", "set args" + _position);
        return contentPane;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        mUpdatePercentListener = (UpdatePercentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdatePercentListener");
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        Log.d("contentpane "+args.getInt("position"),"create");


    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();

        Log.d("contentpane "+args.getInt("position"),"create view ");


        rootView = (ViewGroup) inflater.inflate(R.layout.content_pane,container,false);

        captionView = (TextView) rootView.findViewById(R.id.captionView);
        chapter = (Chapter) args.getSerializable("chapter");
        captionView.setText(chapter.captions.get(0).body);


        mSurfaceView = (SurfaceView)rootView.findViewById(R.id.videoSurfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(this);

        videoSource = Uri.parse(args.getString("uriString"));

        mp = new MediaPlayer();

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("contentpane ","prepared");
                if (m_isVisible) {
                    play();
                }

            }
        });

        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.d("MPlayer", "I just got asked to seek to " + mp.getCurrentPosition());

            }
        });

        try {
            mp.setDataSource(getActivity(), videoSource);

            mp.prepareAsync();
        } catch ( IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





        /*
        mediaControls = new MediaController(getActivity());
        mainVideo = (VideoView) rootView.findViewById(R.id.mainVideo);
        mainVideo.setMediaController(mediaControls);
        mainVideo.setVideoURI(Uri.parse(args.getString("uri")));
        //mainVideo.requestFocus();
        //mainVideo.start();
        Log.d("ContentPane","creating page "+args.getInt("position"));
        */
        return rootView;
    }

    private void captionTimeout() {

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Bundle args = getArguments();

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && ! m_isVisible) {
            // it transitioned invisible -> visible
            Log.d("contentpane "+args.getInt("position"),"became visible");

            if (mp != null) {
                play();
            }

        } else if (!isVisibleToUser && m_isVisible) {
            // it transitioned visible -> invisible
            Log.d("contentpane "+args.getInt("position"),"became invisible");
                    if (mp.isPlaying()) {

                        mp.pause();
                        mp.seekTo(0);
                    }
        }


        m_isVisible = isVisibleToUser;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder ) {
        mp.setDisplay(holder);
        //play();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void play() {
        mp.start();
        videoTimerHandler.postDelayed(UpdateCaptions, 100);
    }

    private Runnable UpdateCaptions = new Runnable() {
        @Override
        public void run() {

            float percent = 100.0f * (float) mp.getCurrentPosition()/ (float )mp.getDuration();
            mUpdatePercentListener.updatePercent(percent);

            int pos = mp.getCurrentPosition() / 1000;
            int i;
            for (i=0;i<chapter.captions.size();i++) {
                if (pos >= chapter.captions.get(i).start && pos < chapter.captions.get(i).end) {
                    captionView.setText(mp.isPlaying() +" @ " + mp.getCurrentPosition() + " :" + chapter.captions.get(i).body);

                    break;
                }
            }
            if (mp.isPlaying()) {
                videoTimerHandler.postDelayed(this, 100);
            }
        }
    };


    @Override
    public void onDestroyView() {
        Bundle args = getArguments();
        Log.d("ContentPane","destroying page "+args.getInt("position"));

        mp.release();
        super.onDestroyView();
    }
}
