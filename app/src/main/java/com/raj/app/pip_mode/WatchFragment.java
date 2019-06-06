package com.raj.app.pip_mode;

import android.app.Fragment;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.raj.app.pip_mode.widget.MovieView;

/**
 * Demonstrates watching a video within fragment that can enter PIP mode.
 *
 * @author Rajpalsinh
 * @version 1
 * @since 2019
 */
public class WatchFragment extends Fragment {

    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();

    private MovieView mMovieView;

    private ScrollView mScrollView;

    private final View.OnClickListener mOnClickListener =
            view -> {
                switch (view.getId()) {
                    case R.id.pip:
                        minimize();
                        break;
                }
            };

    private MovieView.MovieListener mMovieListener =
            new MovieView.MovieListener() {

                @Override
                public void onMovieStarted() {
                    // Not implemented
                }

                @Override
                public void onMovieStopped() {
                    // Not implemented
                }

                @Override
                public void onMovieMinimized() {
                    // The MovieView wants us to minimize it. We enter Picture-in-Picture mode now.
                    minimize();
                }
            };

    public static WatchFragment newInstance() {
        return new WatchFragment();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_watcher, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMovieView = view.findViewById(R.id.movie);
        mScrollView = view.findViewById(R.id.scroll);

        // Set up the video; it automatically starts.
        mMovieView.setMovieListener(mMovieListener);
        view.findViewById(R.id.pip).setOnClickListener(mOnClickListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // On entering Picture-in-Picture mode, onPause is called, but not onStop.
        // For this reason, this is the place where we should pause the video playback.
        mMovieView.pause();
    }

    public void minimize() {
        if (mMovieView == null) {
            return;
        }
        // Hide the controls in picture-in-picture mode.
        mScrollView.setVisibility(View.GONE);
        mMovieView.hideControls();
        // Calculate the aspect ratio of the PiP screen.
        Rational aspectRatio = new Rational(mMovieView.getWidth(), mMovieView.getHeight());
        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
        if (getActivity() != null) {
            getActivity().enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(
            boolean isInPictureInPictureMode, Configuration configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration);
        Log.d("PIP", "is pip: " + isInPictureInPictureMode);
        if (!isInPictureInPictureMode) {
            // Show the video controls if the video is not playing
            if (mMovieView != null && !mMovieView.isPlaying()) {
                mScrollView.setVisibility(View.VISIBLE);
                mMovieView.showControls();
            }
        } else {
            if (mMovieView != null && !mMovieView.isPlaying()) {
                mScrollView.setVisibility(View.GONE);
                mMovieView.hideControls();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final View decorView = getActivity().getWindow().getDecorView();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mScrollView.setVisibility(View.GONE);
            mMovieView.setAdjustViewBounds(false);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            mScrollView.setVisibility(View.VISIBLE);
            mMovieView.setAdjustViewBounds(true);
        }
    }
}
