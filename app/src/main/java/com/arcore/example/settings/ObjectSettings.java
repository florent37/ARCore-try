package com.arcore.example.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.arcore.example.R;
import com.arcore.example.arcoremanager.ArCoreManager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ObjectSettings extends FrameLayout {

    @BindView(R.id.config_object_scale)
    RadioButton mConfigScale;
    @BindView(R.id.config_object_rotate)
    RadioButton mConfigRotate;
    @BindView(R.id.config_object_translate)
    RadioButton mConfigTranslate;
    @BindView(R.id.clear_screen_image_view)
    ImageView mClearScreen;

    public ObjectSettings(@NonNull Context context, @NonNull Listener listener) {
        super(context);
        inflate(context, R.layout.config_objects, this);
        ButterKnife.bind(this);

        mConfigScale.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.SCALE);
            }
        });
        mConfigRotate.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.ROTATE);
            }
        });
        mConfigTranslate.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                uncheckedOthers(compoundButton);
                listener.onTouchModeChanged(ArCoreManager.ObjectTouchMode.MOVE);
            }
        });

        mClearScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClearScreen();
            }
        });

        mConfigScale.setChecked(true);
    }

    private void uncheckedOthers(CompoundButton exception) {
        mConfigScale.setChecked(false);
        mConfigTranslate.setChecked(false);
        mConfigRotate.setChecked(false);
        exception.setChecked(true);
    }

    public interface Listener {
        void onTouchModeChanged(ArCoreManager.ObjectTouchMode objectTouchMode);
        void onClearScreen();
    }
}
