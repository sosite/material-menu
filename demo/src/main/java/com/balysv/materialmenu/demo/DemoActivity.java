package com.balysv.materialmenu.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;

import java.util.Random;

import static com.balysv.materialmenu.MaterialMenuDrawable.Stroke;


public class DemoActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Toolbar          toolbar;
    private MaterialMenuView materialMenuView;
    private IconState        materialMenuState;
    private DrawerLayout     drawerLayout;
    private boolean          direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup
        setContentView(R.layout.demo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialMenuDrawable materialMenu = new MaterialMenuDrawable(this, Color.WHITE, Stroke.REGULAR);
        toolbar.setNavigationIcon(materialMenu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // random state
                setMainState();
            }
        });

        // Demo view initialization
        initViews();
    }

    private void initViews() {
        materialMenuView = (MaterialMenuView) findViewById(R.id.material_menu_button);
        materialMenuView.setOnClickListener(this);

        drawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));
        drawerLayout.setScrimColor(Color.parseColor("#66000000"));
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                getMaterialMenu().setTransformationOffset(
                    MaterialMenuDrawable.AnimationState.BURGER_ARROW,
                    direction ? 2 - slideOffset : slideOffset
                );
            }

            @Override
            public void onDrawerOpened(android.view.View drawerView) {
                direction = true;
            }

            @Override
            public void onDrawerClosed(android.view.View drawerView) {
                direction = false;
            }
        });

        SeekBar duration = (SeekBar) findViewById(R.id.item_animation_duration);
        duration.setMax(4600);
        duration.setProgress(MaterialMenuDrawable.DEFAULT_TRANSFORM_DURATION);
        duration.setOnSeekBarChangeListener(this);

        findViewById(R.id.switch_item_arrow).setOnClickListener(this);
        findViewById(R.id.switch_item_menu).setOnClickListener(this);
        findViewById(R.id.switch_item_x).setOnClickListener(this);
        findViewById(R.id.switch_item_check).setOnClickListener(this);
        findViewById(R.id.switch_item_show).setOnClickListener(this);
        findViewById(R.id.switch_item_hide).setOnClickListener(this);
        findViewById(R.id.animate_item_arrow).setOnClickListener(this);
        findViewById(R.id.animate_item_menu).setOnClickListener(this);
        findViewById(R.id.animate_item_x).setOnClickListener(this);
        findViewById(R.id.animate_item_check).setOnClickListener(this);
        findViewById(R.id.animate_item_hide).setOnClickListener(this);
    }

    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        refreshDrawerState();
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.animate_item_menu:
                animateIconState(IconState.BURGER);
                break;
            case R.id.animate_item_arrow:
                animateIconState(IconState.ARROW);
                break;
            case R.id.animate_item_x:
                animateIconState(IconState.X);
                break;
            case R.id.animate_item_check:
                animateIconState(IconState.CHECK);
                break;
            case R.id.animate_item_hide:
                animateIconState(IconState.HIDE);
                break;
            case R.id.switch_item_menu:
                setIconState(IconState.BURGER);
                break;
            case R.id.switch_item_arrow:
                setIconState(IconState.ARROW);
                break;
            case R.id.switch_item_x:
                setIconState(IconState.X);
                break;
            case R.id.switch_item_check:
                setIconState(IconState.CHECK);
                break;
            case R.id.switch_item_show:
                materialMenuView.setVisible(true);
                break;
            case R.id.switch_item_hide:
                materialMenuView.setVisible(false);
                break;
            case R.id.material_menu_button:
                setMainState();
                break;
        }
    }

    private void setMainState() {
        materialMenuState = generateNewState(materialMenuState);
        animateIconState(materialMenuState);
    }

    private void refreshDrawerState() {
        this.direction = drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        materialMenuView.setTransformationDuration(400 + progress);
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private MaterialMenuDrawable getMaterialMenu() {
        return (MaterialMenuDrawable) toolbar.getNavigationIcon();
    }

    private void animateIconState(IconState iconState) {
        getMaterialMenu().animateIconState(iconState);
        materialMenuView.animateIconState(iconState);
    }

    private void setIconState(IconState iconState) {
        getMaterialMenu().setIconState(iconState);
        materialMenuView.setIconState(iconState);
    }

    private static IconState generateNewState(IconState previous) {
        IconState generated = intToState(new Random().nextInt(5));
        return generated != previous ? generated : generateNewState(previous);
    }

    private static MaterialMenuDrawable.IconState intToState(int state) {
        switch (state) {
            case 0:
                return MaterialMenuDrawable.IconState.BURGER;
            case 1:
                return MaterialMenuDrawable.IconState.ARROW;
            case 2:
                return MaterialMenuDrawable.IconState.X;
            case 3:
                return MaterialMenuDrawable.IconState.CHECK;
            case 4:
                return MaterialMenuDrawable.IconState.HIDE;
        }
        throw new IllegalArgumentException("Must be a number [0,4)");
    }
}
