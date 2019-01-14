package com.wslclds.castn.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.wslclds.castn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SlideFragment extends Fragment {

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.background)
    LinearLayout background;

    public static SlideFragment newInstance(String title, String description, String image, int background, int textColor) {
        Bundle args = new Bundle();
        args.putString("title",title);
        args.putString("description",description);
        args.putString("image",image);
        args.putInt("background",background);
        args.putInt("textColor",textColor);
        SlideFragment fragment = new SlideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SlideFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slide, container, false);
        ButterKnife.bind(this,view);

        title.setText(getArguments().getString("title"));
        description.setText(getArguments().getString("description"));
        image.setImageDrawable(new IconicsDrawable(getContext(), getArguments().getString("image")));
        background.setBackgroundColor(getArguments().getInt("background"));

        title.setTextColor(getArguments().getInt("textColor"));
        description.setTextColor(getArguments().getInt("textColor"));
        //image.setColorFilter(getArguments().getInt("textColor"));
        return view;
    }

}
